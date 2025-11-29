package com.example.calendarreminderapp;

import java.time.LocalDateTime;

public class Reminder {

    private String id;           // Firestore document ID
    private String title;
    private String description;
    private LocalDateTime dateTime;

    public Reminder() {
        // no-arg constructor for Firebase if needed
    }

    public Reminder(String id, String title, String description, LocalDateTime dateTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public String toString() {
        // What will show in the ListView
        return dateTime.toString() + " - " + title;
    }
}