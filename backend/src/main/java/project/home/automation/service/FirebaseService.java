package project.home.automation.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Service
public class FirebaseService {

    @PostConstruct
    public void initialization() {
        try {
            // Prevent re-initializing FirebaseApp if already initialized
            if (FirebaseApp.getApps().isEmpty()) {
                try (FileInputStream serviceAccount = new FileInputStream("")) {
                    FirebaseOptions options = new FirebaseOptions.Builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .setDatabaseUrl("")
                            .build();

                    FirebaseApp.initializeApp(options);
                    System.out.println("Firebase initialized successfully.");
                }
            } else {
                System.out.println("ℹFirebase already initialized.");
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize Firebase: " + e.getMessage());
            e.printStackTrace();
        }
    }
}