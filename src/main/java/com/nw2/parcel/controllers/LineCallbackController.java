package com.nw2.parcel.controllers;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.repositories.UsersRepository;
import com.nw2.parcel.services.FirebaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://bscit.sit.kmutt.ac.th/capstone25/cp25nw2"
})
@RestController
@RequestMapping("/api/line")
@RequiredArgsConstructor
public class LineCallbackController {

    @Value("${line.login.channel-id}")
    private String channelId;

    @Value("${line.login.channel-secret}")
    private String channelSecret;

    @Value("${line.login.redirect-uri}")
    private String redirectUri;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private final UsersRepository usersRepository;
    private final FirebaseService firebaseService;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/callback")
    public ResponseEntity<String> callback(
            @RequestParam String code,
            @RequestParam String state
    ) {

        try {

            // 1️⃣ verify Firebase token
            FirebaseToken decodedToken = firebaseService.verifyIdToken(state);
            String firebaseUid = decodedToken.getUid();

            // 2️⃣ แลก code เป็น LINE access token
            String tokenUrl = "https://api.line.me/oauth2/v2.1/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String body = "grant_type=authorization_code"
                    + "&code=" + code
                    + "&redirect_uri=" + redirectUri
                    + "&client_id=" + channelId
                    + "&client_secret=" + channelSecret;

            HttpEntity<String> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(tokenUrl, request, Map.class);

            String accessToken = (String) response.getBody().get("access_token");

            // 3️⃣ ดึง LINE profile
            HttpHeaders profileHeaders = new HttpHeaders();
            profileHeaders.setBearerAuth(accessToken);

            HttpEntity<String> profileReq =
                    new HttpEntity<>(profileHeaders);

            ResponseEntity<Map> profileRes =
                    restTemplate.exchange(
                            "https://api.line.me/v2/profile",
                            HttpMethod.GET,
                            profileReq,
                            Map.class
                    );

            String lineUserId =
                    (String) profileRes.getBody().get("userId");

            // 🔐 check ว่า LINE ถูกใช้แล้วหรือยัง
            usersRepository.findByLineUserId(lineUserId)
                    .ifPresent(existingUser -> {
                        if (!existingUser.getFirebaseUid().equals(firebaseUid)) {
                            throw new RuntimeException("This LINE account is already linked to another user.");
                        }
                    });

            // 4️⃣ ผูก LINE กับ user
            Users user = usersRepository.findByFirebaseUid(firebaseUid)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setLineUserId(lineUserId);
            user.setLineConnectedAt(LocalDateTime.now());

            usersRepository.save(user);

            // 5️⃣ redirect กลับ frontend
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", frontendUrl + "/profile?line=success")
                    .build();

        } catch (FirebaseAuthException e) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid Firebase token");

        }
    }
}