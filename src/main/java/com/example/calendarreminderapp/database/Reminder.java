package com.example.calendarreminderapp.database;

/**
 * Firestore-friendly Reminder model.
 * Date is stored as ISO string "yyyy-MM-dd" so Firestore can serialize it.
 */
public class Reminder {

    private String username;
    private String date;        // e.g. "2025-12-01"
    private String title;
    private String description;
    private String time;        // e.g. "03:15 PM"

    // REQUIRED no-arg constructor for Firestore
    public Reminder() {
    }

    public Reminder(String username,
                    String date,
                    String title,
                    String description,
                    String time) {
        this.username = username;
        this.date = date;
        this.title = title;
        this.description = description;
        this.time = time;
    }

    // Getters and setters (needed by Firestore)

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return title;
    }
}
