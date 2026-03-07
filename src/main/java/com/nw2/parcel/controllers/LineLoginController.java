package com.nw2.parcel.controllers;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.nw2.parcel.services.FirebaseService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://bscit.sit.kmutt.ac.th/capstone25/cp25nw2"
})
@RestController
@RequestMapping("/api/line")
@RequiredArgsConstructor
public class LineLoginController {

    @Value("${line.login.channel-id}")
    private String channelId;

    @Value("${line.login.redirect-uri}")
    private String redirectUri;

    @Value("${line.login.state-secret}")
    private String stateSecret;

    private final FirebaseService firebaseService;

    @GetMapping("/connect")
    public ResponseEntity<String> connect(@RequestParam String firebaseToken) throws FirebaseAuthException {

        // verify firebase token
        FirebaseToken decoded = firebaseService.verifyIdToken(firebaseToken);
        String firebaseUid = decoded.getUid();

        // create JWT state (expire in 5 minutes)
        String state = Jwts.builder()
                .setSubject(firebaseUid)
                .setExpiration(new Date(System.currentTimeMillis() + 5 * 60 * 1000))
                .signWith(SignatureAlgorithm.HS256, stateSecret.getBytes())
                .compact();

        String url = "https://access.line.me/oauth2/v2.1/authorize"
                + "?response_type=code"
                + "&client_id=" + channelId
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&state=" + state
                + "&scope=profile%20openid";

        return ResponseEntity.ok(url);
    }
}