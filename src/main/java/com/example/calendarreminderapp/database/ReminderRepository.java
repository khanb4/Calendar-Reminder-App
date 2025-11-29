package com.example.calendarreminderapp;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReminderRepository {

    private final Firestore db;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public ReminderRepository(Firestore db) {
        this.db = db;
    }

    public List<Reminder> getAllRemindersForUser(String userId) throws Exception {
        List<Reminder> list = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = db.collection("Users")
                .document(userId)
                .collection("Reminders")
                .get();

        List<QueryDocumentSnapshot> docs = future.get().getDocuments();

        for (QueryDocumentSnapshot doc : docs) {
            String id = doc.getId();
            String title = doc.getString("title");
            String description = doc.getString("description");
            String date = doc.getString("date"); // "yyyy-MM-dd"
            String time = doc.getString("time"); // "HH:mm"

            if (date == null || time == null) continue;

            LocalDate d = LocalDate.parse(date, dateFormatter);
            LocalTime t = LocalTime.parse(time, timeFormatter);
            LocalDateTime dt = LocalDateTime.of(d, t);

            list.add(new Reminder(id, title, description, dt));
        }

        return list;
    }

    public List<Reminder> getUpcomingReminders(String userId, int daysAhead) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limit = now.plusDays(daysAhead);

        List<Reminder> all = getAllRemindersForUser(userId);
        List<Reminder> upcoming = new ArrayList<>();

        for (Reminder r : all) {
            if (!r.getDateTime().isBefore(now) && !r.getDateTime().isAfter(limit)) {
                upcoming.add(r);
            }
        }

        upcoming.sort((a, b) -> a.getDateTime().compareTo(b.getDateTime()));
        return upcoming;
    }
}
