package com.nw2.parcel.configs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class FirebaseConfig {

    // ทำให้ optional: ถ้าไม่มี property นี้ (prod) จะไม่ fail
    @Value("${firebase.config.path:}")
    private Resource serviceAccount;

    @PostConstruct
    public void init() throws Exception {
        if (!FirebaseApp.getApps().isEmpty()) return;

        FirebaseOptions.Builder builder = FirebaseOptions.builder();

        // 1) ถ้ามี ADC ให้ใช้ก่อน (GOOGLE_APPLICATION_CREDENTIALS หรือ Metadata)
        try {
            GoogleCredentials adc = GoogleCredentials.getApplicationDefault();
            builder.setCredentials(adc);
        } catch (Exception ignored) {
            // 2) ถ้า ADC ใช้ไม่ได้ ค่อย fallback เป็นไฟล์ใน classpath (dev)
            if (serviceAccount == null || !serviceAccount.exists()) {
                throw new IllegalStateException(
                        "No ADC and no firebase.config.path. Set GOOGLE_APPLICATION_CREDENTIALS or provide firebase.config.path for dev.");
            }
            try (InputStream in = serviceAccount.getInputStream()) {
                builder.setCredentials(GoogleCredentials.fromStream(in));
            }
        }

        FirebaseApp.initializeApp(builder.build());
    }
}
