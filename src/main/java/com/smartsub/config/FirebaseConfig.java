
package com.smartsub.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {

    private static final Logger logger =
            LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${app.firebase.credentials-path:}")
    private String credentialsPath;

    @Value("${app.firebase.project-id:subscription-manager-1d92d}")
    private String projectId;

    private boolean firebaseInitialized = false;

    @PostConstruct
    public void initFirebase() {
        if (!FirebaseApp.getApps().isEmpty()) {
            firebaseInitialized = true;
            logger.info("Firebase Admin SDK is already initialized.");
            return;
        }

        String envPath = System.getenv("FIREBASE_CREDENTIALS_PATH");

        String pathToUse =
                (envPath != null && !envPath.isBlank())
                        ? envPath
                        : credentialsPath;

        try {
            GoogleCredentials credentials;

            if (pathToUse != null && !pathToUse.isBlank()) {
                File file = new File(pathToUse);

                if (!file.isFile() || !file.canRead()) {
                    throw new IllegalStateException(
                            "Firebase credentials file is missing or unreadable."
                    );
                }

                try (InputStream input =
                             new FileInputStream(file)) {
                    credentials =
                            GoogleCredentials.fromStream(input);
                }
            } else {
                // Supports Application Default Credentials,
                // including GOOGLE_APPLICATION_CREDENTIALS.
                credentials =
                        GoogleCredentials.getApplicationDefault();
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId(projectId)
                    .build();

            FirebaseApp.initializeApp(options);
            firebaseInitialized = true;

            logger.info(
                    "Firebase Admin SDK initialized for project {}.",
                    projectId
            );

        } catch (Exception e) {
            firebaseInitialized = false;

            logger.error(
                    "Firebase Admin SDK initialization failed. " +
                    "Configure valid Firebase Admin credentials. " +
                    "Protected Firebase authentication must remain unavailable."
            );

            throw new IllegalStateException(
                    "Cannot initialize Firebase Admin SDK. " +
                    "Check FIREBASE_CREDENTIALS_PATH or " +
                    "Application Default Credentials.",
                    e
            );
        }
    }

    public boolean isFirebaseInitialized() {
        return firebaseInitialized
                && !FirebaseApp.getApps().isEmpty();
    }
}
