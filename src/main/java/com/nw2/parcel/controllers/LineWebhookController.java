package com.nw2.parcel.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://bscit.sit.kmutt.ac.th/capstone25/cp25nw2"
})
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class LineWebhookController {

    private final UsersRepository usersRepository;

    @Value("${line.channel.secret}")
    private String channelSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping
    public ResponseEntity<String> webhook(
            @RequestHeader("X-Line-Signature") String signature,
            @RequestBody String rawBody
    ) {

        try {

            // 🔐 1️⃣ Verify Signature (ป้องกัน request ปลอม)
            if (!verifySignature(rawBody, signature)) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("Invalid signature");
            }

            // ✅ 2️⃣ parse JSON หลัง verify
            Map<String, Object> body =
                    objectMapper.readValue(rawBody, Map.class);

            List<Map<String, Object>> events =
                    (List<Map<String, Object>>) body.get("events");

            if (events == null) {
                return ResponseEntity.ok("No events");
            }

            for (Map<String, Object> event : events) {

                String type = (String) event.get("type");

                Map<String, Object> source =
                        (Map<String, Object>) event.get("source");

                String lineUserId =
                        (String) source.get("userId");

                // 🟢 ผู้ใช้ Add Friend
                if ("follow".equals(type)) {

                    usersRepository.findByLineUserId(lineUserId)
                            .ifPresent(user -> {
                                user.setUpdatedAt(LocalDateTime.now());
                                usersRepository.save(user);
                            });
                }

                // 🔴 ผู้ใช้กด Unfollow bot
                if ("unfollow".equals(type)) {

                    usersRepository.findByLineUserId(lineUserId)
                            .ifPresent(user -> {
                                user.setLineUserId(null);
                                user.setLineConnectedAt(null);
                                user.setUpdatedAt(LocalDateTime.now());
                                usersRepository.save(user);
                            });
                }
            }

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Webhook error");
        }
    }

    // 🔐 ตรวจสอบว่า request มาจาก LINE จริงไหม
    private boolean verifySignature(String body, String signature) {
        try {

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey =
                    new SecretKeySpec(channelSecret.getBytes(), "HmacSHA256");

            mac.init(secretKey);

            byte[] hash = mac.doFinal(body.getBytes());
            String encodedHash =
                    Base64.getEncoder().encodeToString(hash);

            return encodedHash.equals(signature);

        } catch (Exception e) {
            return false;
        }
    }
}