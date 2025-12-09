package com.calendarreminderapp;

public class Reminder {

    private String id;            // Firestore document ID (optional)
    private String date;          // Stored as yyyy-MM-dd
    private String time;          // Stored as HH:mm AM/PM
    private String title;
    private String description;

    public Reminder() {
        // Required for Firebase deserialization
    }

    public Reminder(String id, String date, String title, String description, String time) {
        this.id = id;
        this.date = date;
        this.title = title;
        this.description = description;
        this.time = time;
    }

    // -------- Getters --------

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    // -------- Display in ListView --------
    @Override
    public String toString() {
        String base = date + "  " + time + " — " + title;
        if (description != null && !description.isBlank()) {
            base += "\n" + description;
        }
        return base;
    }
}