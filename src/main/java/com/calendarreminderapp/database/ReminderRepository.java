package com.calendarreminderapp.database;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ReminderRepository {

    private final CollectionReference remindersRef;

    public ReminderRepository() throws IOException {
        Firestore db = Database.getFirestore();
        this.remindersRef = db.collection("reminders");
    }

    public void addReminder(String username,
                            LocalDate date,
                            String title,
                            String description,
                            String time)
            throws ExecutionException, InterruptedException {

        // Store date as ISO string so Firestore can serialize it
        String dateString = date.toString();   // "2025-12-01"

        Reminder reminder = new Reminder(username, dateString, title, description, time);

        ApiFuture<?> future = remindersRef.add(reminder);
        future.get(); // wait for write to complete (OK for school project)

        System.out.println("✅ Reminder added for " + username + " on " + dateString);
    }

    public List<Reminder> getRemindersForDate(String username, LocalDate date)
            throws ExecutionException, InterruptedException {

        String dateString = date.toString();

        Query query = remindersRef
                .whereEqualTo("username", username)
                .whereEqualTo("date", dateString);

        ApiFuture<QuerySnapshot> future = query.get();
        QuerySnapshot snapshot = future.get();

        List<Reminder> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            Reminder reminder = doc.toObject(Reminder.class);
            result.add(reminder);
        }
        return result;
    }

    // All reminders for the whole month
    public List<Reminder> getRemindersForMonth(String username, YearMonth yearMonth)
            throws ExecutionException, InterruptedException {

        String start = yearMonth.atDay(1).toString();        // e.g. 2025-12-01
        String end   = yearMonth.atEndOfMonth().toString();  // e.g. 2025-12-31

        // Get all reminders for this user, then filter by date here
        Query query = remindersRef.whereEqualTo("username", username);

        ApiFuture<QuerySnapshot> future = query.get();
        QuerySnapshot snapshot = future.get();

        List<Reminder> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            Reminder reminder = doc.toObject(Reminder.class);
            String dateStr = reminder.getDate();
            if (dateStr != null &&
                    dateStr.compareTo(start) >= 0 &&
                    dateStr.compareTo(end)   <= 0) {
                result.add(reminder);
            }
        }
        return result;
    }

    // Upcoming reminders (used for "Upcoming Reminders" list)
    public List<Reminder> getUpcomingReminders(String username)
            throws ExecutionException, InterruptedException {

        ApiFuture<QuerySnapshot> future = remindersRef
                .whereEqualTo("username", username)
                .orderBy("date")
                .orderBy("time")
                .get();

        return future.get().toObjects(Reminder.class);
    }

    // Delete a reminder that matches user + date + time + title
    public void deleteReminder(String username,
                               String dateString,
                               String time,
                               String title)
            throws ExecutionException, InterruptedException {

        Query query = remindersRef
                .whereEqualTo("username", username)
                .whereEqualTo("date", dateString)
                .whereEqualTo("time", time)
                .whereEqualTo("title", title);

        ApiFuture<QuerySnapshot> future = query.get();
        QuerySnapshot snapshot = future.get();

        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            doc.getReference().delete();
        }
    }

    // Update a reminder (find by old values, write new values)
    public void updateReminder(String username,
                               String originalDateString,
                               String originalTime,
                               String originalTitle,
                               LocalDate newDate,
                               String newTitle,
                               String newDescription,
                               String newTime)
            throws ExecutionException, InterruptedException {

        String newDateString = newDate.toString();

        Query query = remindersRef
                .whereEqualTo("username", username)
                .whereEqualTo("date", originalDateString)
                .whereEqualTo("time", originalTime)
                .whereEqualTo("title", originalTitle);

        ApiFuture<QuerySnapshot> future = query.get();
        QuerySnapshot snapshot = future.get();

        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            doc.getReference().update(
                    "date", newDateString,
                    "title", newTitle,
                    "description", newDescription,
                    "time", newTime
            );
        }
    }
}
