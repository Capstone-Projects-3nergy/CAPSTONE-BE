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

    @Value("${firebase.config.path:}")
    private Resource serviceAccount;

    @PostConstruct
    public void init() throws Exception {
        if (!FirebaseApp.getApps().isEmpty()) return;

        FirebaseOptions.Builder builder = FirebaseOptions.builder();

        try {
            // 🔹 (1) พยายามใช้ ADC ก่อน (สำหรับ container/VM)
            GoogleCredentials adc = GoogleCredentials.getApplicationDefault();
            builder.setCredentials(adc);
            // ✅ ใส่ projectId ให้ตรงกับโปรเจกต์ tractify-dpms-capstone-3nergy
            builder.setProjectId("tractify-dpms-capstone-3nergy");
        } catch (Exception ignored) {
            // 🔹 (2) ถ้าไม่มี ADC ใช้ไฟล์ใน classpath (สำหรับ local dev)
            if (serviceAccount == null || !serviceAccount.exists()) {
                throw new IllegalStateException(
                        "No ADC and no firebase.config.path. " +
                                "Set GOOGLE_APPLICATION_CREDENTIALS or provide firebase.config.path for dev.");
            }
            try (InputStream in = serviceAccount.getInputStream()) {
                builder.setCredentials(GoogleCredentials.fromStream(in));
                // ✅ ใส่ projectId ตรงนี้ด้วย
                builder.setProjectId("tractify-dpms-capstone-3nergy");
            }
        }

        FirebaseApp.initializeApp(builder.build());
        System.out.println("✅ Firebase Admin initialized for project: " +
                FirebaseApp.getInstance().getOptions().getProjectId());
    }
}
