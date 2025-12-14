package com.calendarreminderapp.database;


public class Reminder {

    private String id;
    private String username;
    private String date;
    private String title;
    private String description;
    private String time;


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


    // Getters & Setters


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
        return time + " — " + title;
    }
}