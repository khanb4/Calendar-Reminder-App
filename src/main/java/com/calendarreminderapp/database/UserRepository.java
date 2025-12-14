package com.calendarreminderapp.database;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class UserRepository {

    private final CollectionReference usersRef;

    public UserRepository() throws IOException {
        Firestore db = Database.getFirestore();
        this.usersRef = db.collection("users");
    }


    public void createUser(String username, String password)
            throws ExecutionException, InterruptedException {

        User user = new User(username, password);
        DocumentReference docRef = usersRef.document(username);
        ApiFuture<WriteResult> writeResult = docRef.set(user);
        writeResult.get(); // Wait for completion
        System.out.println("✅ User created/updated in Firestore: " + username);
    }


    public boolean validateUser(String username, String password)
            throws ExecutionException, InterruptedException {

        DocumentReference docRef = usersRef.document(username);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot snapshot = future.get();

        if (!snapshot.exists()) {
            System.out.println("❌ User not found: " + username);
            return false;
        }

        User user = snapshot.toObject(User.class);
        if (user == null) {
            return false;
        }

        boolean ok = password.equals(user.getPassword());
        System.out.println("🔍 Validate user '" + username + "': " + ok);
        return ok;
    }
}
