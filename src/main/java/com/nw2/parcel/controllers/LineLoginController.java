package com.nw2.parcel.controllers;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.nw2.parcel.services.FirebaseService;
import com.nw2.parcel.services.LineStateStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

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

    private final FirebaseService firebaseService;
    private final LineStateStore stateStore;

    @GetMapping("/connect")
    public ResponseEntity<String> connect(@RequestParam String firebaseToken) throws FirebaseAuthException {

        // verify firebase token
        FirebaseToken decoded = firebaseService.verifyIdToken(firebaseToken);
        String firebaseUid = decoded.getUid();

        // generate short state
        String state = UUID.randomUUID().toString();

        // store mapping
        stateStore.put(state, firebaseUid);

        String url = "https://access.line.me/oauth2/v2.1/authorize"
                + "?response_type=code"
                + "&client_id=" + channelId
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&state=" + state
                + "&scope=profile%20openid";

        return ResponseEntity.ok(url);
    }
}