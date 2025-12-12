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

    // ADD REMINDER
    public void addReminder(String username,
                            LocalDate date,
                            String title,
                            String description,
                            String time)
            throws ExecutionException, InterruptedException {

        Reminder reminder = new Reminder(
                username,
                date.toString(),
                title,
                description,
                time
        );

        remindersRef.add(reminder).get();
    }

    // GET REMINDERS FOR DATE
    public List<Reminder> getRemindersForDate(String username, LocalDate date)
            throws ExecutionException, InterruptedException {

        Query query = remindersRef
                .whereEqualTo("username", username)
                .whereEqualTo("date", date.toString());

        QuerySnapshot snapshot = query.get().get();

        List<Reminder> list = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            Reminder r = doc.toObject(Reminder.class);
            r.setId(doc.getId());
            list.add(r);
        }
        return list;
    }
    // GET REMINDERS FOR MONTH (yyyy-MM-dd string range works lexicographically)
    public List<Reminder> getRemindersForMonth(String username, YearMonth ym)
            throws ExecutionException, InterruptedException {

        String start = ym.atDay(1).toString();        // e.g. 2025-12-01
        String end   = ym.atEndOfMonth().toString();  // e.g. 2025-12-31

        Query query = remindersRef
                .whereEqualTo("username", username)
                .whereGreaterThanOrEqualTo("date", start)
                .whereLessThanOrEqualTo("date", end)
                .orderBy("date")
                .orderBy("time");

        QuerySnapshot snapshot = query.get().get();

        List<Reminder> list = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            Reminder r = doc.toObject(Reminder.class);
            r.setId(doc.getId());
            list.add(r);
        }
        return list;
    }

    // GET UPCOMING REMINDERS
    public List<Reminder> getUpcomingReminders(String username)
            throws ExecutionException, InterruptedException {

        QuerySnapshot snapshot = remindersRef
                .whereEqualTo("username", username)
                .orderBy("date")
                .orderBy("time")
                .get()
                .get();

        List<Reminder> list = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            Reminder r = doc.toObject(Reminder.class);
            r.setId(doc.getId());
            list.add(r);
        }
        return list;
    }

    // DELETE REMINDER BY ID
    public void deleteReminder(Reminder reminder)
            throws ExecutionException, InterruptedException {

        if (reminder == null || reminder.getId() == null) return;

        remindersRef.document(reminder.getId()).delete().get();
    }

    // UPDATE REMINDER BY ID
    public void updateReminder(Reminder reminder,
                               LocalDate newDate,
                               String newTitle,
                               String newDescription,
                               String newTime)
            throws ExecutionException, InterruptedException {

        if (reminder == null || reminder.getId() == null) return;

        remindersRef.document(reminder.getId()).update(
                "date", newDate.toString(),
                "title", newTitle,
                "description", newDescription,
                "time", newTime
        ).get();
    }
    // Update a reminder by matching old values (username + date + time + title)
    public void updateReminderByFields(String username,
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