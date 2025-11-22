package model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
/**
 * A simple class representing a user with a name, age, unique ID and associated creation date-time. This class provides getter and setter methods for the user's attributes,
 * as well as a method to print the user's information in a formatted manner.
 * 
 * @author Jason 
 * @since 11-16-2025
 * @version 1.0
 */
public class User {
    /**
     * The name of the user. This is a string that represents the user's name.  
     */
    @JsonProperty("Name")
    private String name = null;

    /**
     * The age of the user. This is an integer that represents the user's age.
     */

    @JsonProperty("Age")
    private int age = 0;

    /**
     * The unique ID of the user. This is a UUID that uniquely identifies the user in the database. It is generated when the user is created and cannot be changed.
     */
    @JsonProperty("ID")
    private UUID id = null;

    /**
     * The creation date for the user.
     */
    @JsonProperty("Time Created")
    private String timeCreated;
    

    /**
     * Default Constructor for the User class. Initializes the user's name, age, and unique ID.
     */
    public User() {this.id = UUID.randomUUID(); @JsonProperty("Time Created") setDateTime();}

     /**
     * Constructor for the User class. Initializes the user's name, age, and unique ID.
     * @param name
     * @param age
     * @param id
     */
    public User( @JsonProperty("Name") String name, @JsonProperty("Age") int age) {
        this.name = name;
        this.age = age;
        this.id = UUID.randomUUID();
        setDateTime();
    }

    /**
     * Sets the name of the user. This method allows you to change the user's name after the user has been created.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the age of the user. This method allows you to change the user's age after the user has been created.
     * @param age
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * 
     * Sets the creation date of the user. This method does not allow updating the creation time of the user object once set.
     */
    public final void setDateTime() {
        this.timeCreated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Returns the name of the user. This method allows you to retrieve the user's name.
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the age of the user. This method allows you to retrieve the user's age.
     * @return
     */
    public int getAge() {
        return this.age;
    }

    /**
     * Returns the unique ID of the user. This method allows you to retrieve the user's unique ID.
     * @return
     */
    public UUID getID() {
        return this.id;
    }

    /**
     * Returns the unique creation time of the object. This methods allows you to retrieve the creation date/time of the object.
     * @return
     */
    public String getDateTime() {
        return this.timeCreated;
    }

    /**
     * Prints the user's information in a formatted manner. This method displays the user's ID, name, and age in a readable format.
     */
    public void printEntry() {
        System.out.printf("ID: %s\nName: %s\nAge: %d\n",this.id.toString(),this.name,this.age);
    }
}
