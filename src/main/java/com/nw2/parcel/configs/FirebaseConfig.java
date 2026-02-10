package com.nw2.parcel.configs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;     // << สำคัญ
import java.io.IOException;        // << สำคัญ
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        FirebaseOptions options;

        String credPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        InputStream in;

        if (credPath != null && !credPath.isBlank()) {
            in = new FileInputStream(credPath);
        } else {
            in = new ClassPathResource("firebase/firebase-adminsdk.json").getInputStream();
        }

        options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(in))
                .setStorageBucket("tractify-dpms-capstone-3nergy.appspot.com") // 🔥 สำคัญ
                .build();
        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }
}
