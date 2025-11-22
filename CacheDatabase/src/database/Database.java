package database;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import model.User;
/**
 * A simple in-memory database for storing {@link User} objects. Users can be added, retrieved,
 * and removed by either their unique ID or their name. The database maintains two HashMaps to
 * allow for efficient lookups by both ID and name.
 * 
 * <p>Note: This implementation assumes that user names are unique. If duplicate names are added,
 * the behavior is undefined.</p>
 * 
 * @author Jason 
 * @since 11-16-2025
 * @version 1.0
 */
public class Database {
     /**
     * Thread safe HashMap to store users by their unique ID. The key is the user's UUID and the value is the
     * {@link User} object.
     */
    private final ConcurrentSkipListMap<UUID, User> usersByID = new ConcurrentSkipListMap<>();

     /**
     * Thread safe HashMap to store users by their name. The key is the user's name and the value is the
     * {@link User} object. This assumes that user names are unique; if they are not, this could
     * lead to unexpected behavior.
     */
    private final ConcurrentSkipListMap<String, User> usersByName = new ConcurrentSkipListMap<>();

    /**
     * A lock object to synchronize write operations to the database. This ensures that adding or removing users is thread-safe.
     */
    private final Object WRITE_LOCK = new Object();
    /**
     * Adds a user to the database.
     *
     * @param user The {@link User} object to add. Cannot be null. Takes variable number of inputs.
     * @throws IllegalArgumentException if the user is null or if a user with the same ID already exists.
     */
    public void addUser(User...user) {
        synchronized (WRITE_LOCK) {
            for(User u : user) {
                if (u == null) throw new IllegalArgumentException("User cannot be null");
                if (usersByID.containsKey(u.getID())) throw new IllegalArgumentException("User with this ID already exists");
            }
            for(User u : user) {
                usersByID.put(u.getID(), u);
                usersByName.put(u.getName(), u);
            }
        }
    }

    /**
     * Retrieves a user from the database by their unique ID.
     *
     * @param id The {@link UUID} of the user to retrieve.
     * @return The {@link User} object with the given ID, or {@code null} if no user exists with that ID.
     */
    public User getUser(UUID id) {
        return usersByID.get(id);
    }
    
    /**
     * Retrieves a user from the database by their name.
     *
     * @param name The name of the user to retrieve.
     * @return The {@link User} object with the given name, or {@code null} if no user exists with that name.
     */
    public User getUser(String name) {
        return usersByName.get(name);
    }

    /**
     * Removes a user from the database by their unique ID.
     *
     * @param id The {@link UUID} of the user to remove.
     * @return {@code true} if the user was successfully removed, {@code false} if no user existed with that ID.
     */
    public boolean removeUser(UUID... ids) {
        boolean removedAny = false; // track if at least one user was removed
        synchronized (WRITE_LOCK) {
            for (UUID _id : ids) {
                User removed = usersByID.remove(_id);
                if (removed != null) {
                    usersByName.remove(removed.getName());
                    removedAny = true;
                }
            }
        }
        return removedAny;
    }

    /**
     * Removes a user from the database by their name.
     *
     * @param name The name of the user to remove.
     * @return {@code true} if the user was successfully removed, {@code false} if no user existed with that name.
     */
    public boolean removeUser(String... name) {
        boolean removedAny = false; // track if at least one user was removed
        synchronized (WRITE_LOCK) {
            for (String _name : name) {
                User removed = usersByName.remove(_name);
                if(removed != null) {
                    usersByID.remove(removed.getID());
                    removedAny = true;
                }
            }
        }
        return removedAny;
    }
    
    /**
     * Prints information about a user with the given unique ID to the console.
     *
     * @param id The {@link UUID} of the user to print.
     */
    public void printUser(UUID...id) {
        for(UUID _id : id) {
            User user = usersByID.get(_id);
            if (user != null) {
                System.out.printf("ID: %s\nName: %s\nAge: %d\n", user.getID().toString(), user.getName(), user.getAge());
            } 
            else
            {
                System.out.printf("No user found with ID: %s\n", _id.toString());
            }
        }
    }

    /**
     * Prints information about a user with the given name to the console.
     *
     * @param name The name of the user to print.
     */
    public void printUser(String...name) {
          for(String _name : name) {
            User user = usersByName.get(_name);
            if (user != null) {
                System.out.printf("ID: %s\nName: %s\nAge: %d\n", user.getID().toString(), user.getName(), user.getAge());
            }   
            else
            {
                System.out.printf("No user found with Name: %s\n", _name);
            }
        }
    }

    /**
     * Clears the ID list
     */
    public void clearIDList() {
        usersByID.clear();
    }

    /**
     *  Clears the name list
     */
    public void clearNameList() {
        usersByName.clear();
    }
    /**
     * Returns a read-only collection of all users currently stored in the database.
     *
     * @return An unmodifiable {@link Collection} of {@link User} objects.
     */
    public Collection<User> getAllUsers() {return Collections.unmodifiableCollection(usersByID.values());}

    public int getSize() {
        return usersByID.size();
    }
}
