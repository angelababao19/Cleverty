package com.example.cleverty;

// Create a new Java class file named User.java
public class User {
    public String name;
    public String email;
    public String profileImageUrl;    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, String email, String profileImageUrl) {
        this.name = name;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
    }
}

