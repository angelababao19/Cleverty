package com.example.cleverty.model;


import java.util.ArrayList;
import java.util.List;

public class Library {
    private static final Library INSTANCE = new Library();
    private final List<Subject> subjects = new ArrayList<>();

    public static Library getInstance() { return INSTANCE; }
    public List<Subject> getSubjects() { return subjects; }
    public void addSubject(Subject s) { subjects.add(s); }
    public void pinSubject(Subject s) {
        subjects.remove(s);
        subjects.add(0, s);          // move to top
    }
}