package com.example.cleverty.model;

import java.util.ArrayList;
import java.util.List;

public class Library {

    private static Library instance;
    private final List<Subject> subjects = new ArrayList<>();

    public static Library getInstance() {
        if (instance == null) {
            instance = new Library();
        }
        return instance;
    }

    private Library() {
        // Private constructor to enforce the Singleton pattern
    }

    /**
     * Gets the raw, unsorted list of subjects.
     */
    public List<Subject> getSubjects() {
        return subjects;
    }

    public void addSubject(Subject s) {
        subjects.add(s);
    }
}
