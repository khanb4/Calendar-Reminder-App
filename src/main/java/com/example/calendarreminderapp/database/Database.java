package com.example.calendarreminderapp.database;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import java.io.IOException;
import java.io.InputStream;

public class Database {

    private static boolean initialized = false;

    /**
     * Initialize Firebase using serviceAccountKey.json in src/main/resources.
     */
    public static void init() throws IOException {
        if (initialized) {
            return;
        }

        try (InputStream serviceAccount =
                     Database.class.getResourceAsStream("/serviceAccountKey.json")) {

            if (serviceAccount == null) {
                throw new IOException("serviceAccountKey.json not found in resources");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            initialized = true;
            System.out.println("✅ Firebase initialized (Firestore ready)");
        }
    }

    /**
     * Get the Firestore instance. Initializes Firebase if needed.
     */
    public static Firestore getFirestore() throws IOException {
        if (!initialized) {
            init();
        }
        return FirestoreClient.getFirestore();
    }

    // Optional quick test
    public static void main(String[] args) {
        try {
            Firestore db = Database.getFirestore();
            System.out.println("✅ Firestore DB acquired: " + db);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
