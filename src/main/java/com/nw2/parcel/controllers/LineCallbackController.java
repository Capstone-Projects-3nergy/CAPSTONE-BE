package com.nw2.parcel.controllers;

import com.nw2.parcel.entity.Users;
import com.nw2.parcel.repositories.UsersRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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

    @Value("${line.login.state-secret}")
    private String stateSecret;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private final UsersRepository usersRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/callback")
    public ResponseEntity<?> callback(
            @RequestParam String code,
            @RequestParam String state
    ) {

        try {

            // decode JWT state
            Claims claims = Jwts.parser()
                    .setSigningKey(stateSecret.getBytes())
                    .parseClaimsJws(state)
                    .getBody();

            String firebaseUid = claims.getSubject();

            // exchange code for access token
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

            Map tokenBody = response.getBody();

            if (tokenBody == null || tokenBody.get("access_token") == null) {
                throw new RuntimeException("LINE token exchange failed");
            }

            String accessToken = (String) tokenBody.get("access_token");

            // get LINE profile
            HttpHeaders profileHeaders = new HttpHeaders();
            profileHeaders.setBearerAuth(accessToken);

            HttpEntity<String> profileReq = new HttpEntity<>(profileHeaders);

            ResponseEntity<Map> profileRes =
                    restTemplate.exchange(
                            "https://api.line.me/v2/profile",
                            HttpMethod.GET,
                            profileReq,
                            Map.class
                    );

            Map profileBody = profileRes.getBody();

            if (profileBody == null) {
                throw new RuntimeException("LINE profile fetch failed");
            }

            String lineUserId = (String) profileBody.get("userId");

            // check if already linked
            usersRepository.findByLineUserId(lineUserId)
                    .ifPresent(existingUser -> {
                        if (!existingUser.getFirebaseUid().equals(firebaseUid)) {
                            throw new RuntimeException("LINE already linked to another user");
                        }
                    });

            // link account
            Users user = usersRepository.findByFirebaseUid(firebaseUid)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setLineUserId(lineUserId);
            user.setLineConnectedAt(LocalDateTime.now());

            usersRepository.save(user);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", frontendUrl + "/profile?line=success")
                    .build();

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}