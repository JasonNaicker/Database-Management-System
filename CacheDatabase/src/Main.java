import database.Database;
import java.util.Random;
import model.User;
import model.UserFileManager;

public class Main implements Runnable {

    private final Database db = new Database();
    private  final UserFileManager manager;
    private final Random random = new Random();
    private final String filePath;
    private final String[] names = {"Alice", "Bob", "Charlie", "David", "Eve", "Fay", "George", "Hannah"};

    public Main(String filePath) {
        this.filePath = filePath;
        manager = new UserFileManager(db, filePath, true);
    }

    @Override
    public void run() {
        db.clearIDList();
        db.clearNameList();
        // Add some initial users
        db.addUser(new User("Jason", 19), new User("Bob", 50), new User("Sarah", 22));
        // Infinite loop to add random users periodically
        while (true) {
            try {
                Thread.sleep(1); // Add a new user every 5 seconds

                String name = names[random.nextInt(names.length)] + "-" + random.nextInt(1000);
                int age = 10 + random.nextInt(89); // Random age between 10 and 90
                User newUser = new User(name, age);

                db.addUser(newUser);
                System.out.printf("Added new user: %s, Age: %d, ID: %s, Size: %d\n", name, age, newUser.getID(), db.getSize());
            } catch (Exception e) {
                System.err.println("Error in user generation thread: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        String fileName = "C:\\Users\\apoll\\Downloads\\Test.json";
        Main task = new Main(fileName);
        Thread thread = new Thread(task);
        task.db.clearIDList();
        task.db.clearNameList();
        thread.start();
        /* 
        try{
            task.manager.loadFromFile(fileName);
        } catch (Exception e) {
            System.err.println("Cannot load data from file: " + e.getMessage());
        }
        /* */
    }
}
