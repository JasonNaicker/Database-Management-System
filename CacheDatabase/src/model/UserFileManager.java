package model;

import database.Database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;

/**
 * A wrapper class for the {@link User} class
 * 
 * @author Jason 
 * @since 11-16-2025
 * @version 1.0
 */
@SuppressWarnings("unused")
public class UserFileManager {

     /** ObjectMapper for JSON serialization/deserialization with pretty-print enabled */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    /**Scheduled Executor for incremental autosave */
    private ScheduledExecutorService autoSaveExecutor;

    private final Runtime runtime = Runtime.getRuntime();

   /** Backing database instance containing users */
    private final Database database;

    /** Optional input file path reference */
    private Path inputFile;

    /** Optional output file path reference */
    private Path outputFile;

    /**Autosave Interval time */
    private static final short AUTOSAVE_INTERVAL_SECONDS = 1;

    private boolean isAutosaving = false;

    /* 
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timeCreated; 
    */

    private static final String DEFAULT_DIR = "."; 

    /**
     * Constructs a new UserWrapper with the provided {@link Database}.
     * If null, a new Database is created internally.
     *
     * @param database the backing database for users; can be null
     */
    public UserFileManager(Database database, String fileName, boolean autoSave) {
        this.database = (database != null) ? database : new Database();
        //this.timeCreated = LocalDateTime.now();
        setupHooks(fileName);
        if(autoSave) {
            startAutoSave(fileName);
        } else {
            stopAutoSave();
        }
    }

    /**
     * Serializes a single {@link User} object to the specified file.
     *
     * @param user the user to serialize
     * @param fileName the target file path
     * @throws IOException if an I/O error occurs writing the file
     * @throws IllegalArgumentException if the user is null
     */
    public void serializeUser(User user, Path fileName) throws IOException {
        if(user == null) throw new IllegalArgumentException("User object cannot be null");
        OBJECT_MAPPER.writeValue(fileName.toFile(), user);
    }

    /**
     * Deserializes a {@link User} from a file.
     *
     * @param filePath the path to the file containing serialized user JSON
     * @return the deserialized User object
     * @throws IOException if an I/O error occurs reading the file
     * @throws IllegalArgumentException if the file path is null or the file does not exist
     */
    public User deserializeUser(Path filePath) throws IOException {
        if(filePath == null || Files.notExists(filePath)) throw new IllegalArgumentException("File does not exist: " + filePath);
        return OBJECT_MAPPER.readValue(filePath.toFile(), User.class);
    }

    /**
     * Saves all users from the backing database to a file.
     * If the file does not exist, it will be created along with necessary directories.
     * This method is synchronized to prevent concurrent modification issues.
     *
     * @param filePath the path to the file where users will be saved
     * @throws IOException if an I/O error occurs writing the file
     */

    public synchronized void saveToFile(String fileName) throws IOException {
        if(fileName == null || fileName.isBlank()) throw new IllegalArgumentException("File name cannot be null or empty");
        Path _filePath = Path.of(fileName);
        if(Files.notExists(_filePath)) {
            Path parentDir = _filePath.getParent();
            if(parentDir == null) parentDir = Paths.get(DEFAULT_DIR);
            _filePath = createFile(_filePath.getFileName().toString(), parentDir.toString());
        }
        List<User> userList = new ArrayList<>(this.database.getAllUsers());
        OBJECT_MAPPER.writeValue(_filePath.toFile(), userList);
        System.out.println("File has successfully saved...");
    }

    /**
     * Loads all users from a file into the backing database.
     * Existing users in the database will be cleared before loading.
     * This method is synchronized to prevent concurrent modification issues.
     *
     * @param filePath the path to the file containing serialized users
     * @throws IOException if an I/O error occurs reading the file
     * @throws IllegalArgumentException if the file does not exist
     */
    public synchronized void loadFromFile(String fileName) throws IOException {
        if(fileName == null || fileName.isBlank()) throw new IllegalArgumentException("File does not exist: " + fileName);
        Path filePath = Path.of(fileName);
        if(Files.notExists(filePath)) throw new IllegalArgumentException("File does not exist: " + fileName);

            List<User> userList = OBJECT_MAPPER.readValue(filePath.toFile(), new TypeReference<List<User>>() {});

            this.database.clearIDList();
            this.database.clearNameList();

            for(User u : userList) {
                this.database.addUser(u);
            }
    }

        
    /**
     * Ensures that a directory exists at the specified path.
     * Creates the directory if it does not exist.
     *
     * @param directoryName the directory path to ensure
     * @return the normalized Path of the ensured directory
     * @throws IOException if an I/O error occurs creating the directory
     * @throws IllegalStateException if the directoryName is null or empty
     */
    private Path ensureDirectory(String directoryName) throws IOException{
        if(directoryName == null || directoryName.isBlank()) throw new IllegalStateException("Path name cannot be null or empty.");
        Path dirPath = Paths.get(directoryName).normalize();
        if(Files.notExists(dirPath)) {
            try {
                dirPath = Files.createDirectories(dirPath).normalize();
                System.out.println("Ensured Directory: " + dirPath.toAbsolutePath().normalize());
            } catch(IOException e) {
                System.err.println("Failed to create directory");
                dirPath = Paths.get(DEFAULT_DIR);
                throw e;
            }
        }
        return dirPath.normalize();
    }

    /**
     * Creates a file at the given name and directory.
     * If the directory or file does not exist, they will be created.
     *
     * @param fileName the name of the file to create
     * @param directoryName the directory in which to create the file
     * @return the absolute, normalized path of the created file
     * @throws IOException if an I/O error occurs creating the file
     * @throws IllegalArgumentException if fileName or directoryName is null/empty
     */
    private Path createFile(String fileName, String directoryName) throws IOException {
        if((fileName == null || fileName.isBlank()) || (directoryName == null || directoryName.isBlank())) throw new IllegalArgumentException("File name cannot be empty.");
        Path directoryPath = ensureDirectory(directoryName).normalize();
        Path filePath = directoryPath.resolve(fileName).normalize();

        if(Files.notExists(filePath)) {
           try {
                filePath = Files.createFile(filePath).normalize();
                System.out.println("Ensured File: " + filePath.toAbsolutePath().normalize());
           } catch (IOException e) {
                System.err.println("Failed to create path");
                throw e;
           }
        }
        return filePath.toAbsolutePath().normalize();
    }

    /**
     * Placeholder for updating a file.
     * Currently unimplemented.
     */
    public void updateFile() {

    }

    public void startAutoSave(String filePath) {
        if(this.isAutosaving) return;
            this.isAutosaving = true;
            if(this.autoSaveExecutor == null || this.autoSaveExecutor.isShutdown()) {
                this.autoSaveExecutor = Executors.newSingleThreadScheduledExecutor();
            }
            this.autoSaveExecutor.scheduleAtFixedRate(() -> {
                try {
                    System.out.println("[AutoSave] Saving users...");
                    saveToFile(filePath);
                } catch(IOException e) {
                    System.err.println("AutoSave failed: " + e.getMessage());
                }
            }, AUTOSAVE_INTERVAL_SECONDS, AUTOSAVE_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

public void stopAutoSave() {
    if (!this.isAutosaving) return;

    this.isAutosaving = false;

    if (autoSaveExecutor != null) {
        this.autoSaveExecutor.shutdown();
        try {
            if (!this.autoSaveExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                this.autoSaveExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            this.autoSaveExecutor.shutdownNow();
        }
        this.autoSaveExecutor = null;
    }
}

    /**
     * Deletes a file at the specified path if it exists.
     *
     * @param path the path to the file to delete
     * @throws IOException if an I/O error occurs deleting the file
     * @throws IllegalArgumentException if path is null
     */
    public void deleteFile(Path path) throws IOException {
        if(path == null) throw new IllegalArgumentException("Path cannot be null");
        Files.deleteIfExists(path);
        System.out.println(path.toAbsolutePath().normalize().toString() + " has been successfully deleted.");
    }

    private void setupHooks(String fileName) {
        runtime.addShutdownHook(new Thread(() -> {
            try {
                if(database.getSize() > 0) {
                    System.err.println("Saving before shutdown...");
                    saveToFile(fileName);
                }
            } catch (Exception e) {
                System.err.println("[ShutdownHook] Failed to save: " + e.getMessage());
            }
        }));

        Thread.setDefaultUncaughtExceptionHandler((t,e) -> {
             System.err.println("[CrashHandler] Uncaught exception in " + t.getName() + ": " + e);
             try {
                saveToFile(fileName);
                System.err.println("[CrashHandler] User data saved before crash.");
             } catch (IOException c) {
                 System.err.println("[CrashHandler] Failed to save during crash: " + c.getMessage());
             }
        });
    }

}