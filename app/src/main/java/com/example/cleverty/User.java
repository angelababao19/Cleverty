package com.example.cleverty;

/**
 * Represents a user in the application.
 * This class is a "POJO" (Plain Old Java Object) used for storing user data
 * in the Firebase Realtime Database.
 */
public class User {

    // Fields should be private
    private String name;
    private String email;
    private String profileImageUrl;

    /**
     * IMPORTANT: An empty, no-argument constructor is required by Firebase
     * for it to be able to deserialize data from the database into a User object.
     */
    public User() {
    }

    /**
     * Constructor to create a new user object.
     * @param name The user's display name.
     * @param email The user's email address.
     * @param profileImageUrl The URL to the user's profile picture. Can be an empty string.
     */
    public User(String name, String email, String profileImageUrl) {
        this.name = name;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
    }

    // --- GETTERS ---
    // Public getters are needed for Firebase to serialize the object into JSON.

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    // --- SETTERS ---
    // Public setters are needed for flexibility and for Firebase to deserialize.

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
