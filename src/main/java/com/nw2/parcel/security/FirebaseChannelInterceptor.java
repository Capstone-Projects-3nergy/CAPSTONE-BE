package com.nw2.parcel.security;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.nw2.parcel.services.FirebaseService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;

import java.security.Principal;

public class FirebaseChannelInterceptor implements ChannelInterceptor {

    private final FirebaseService firebaseService;

    public FirebaseChannelInterceptor(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String token = accessor.getFirstNativeHeader("Authorization");

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    FirebaseToken decoded = firebaseService.verifyIdToken(token);

                    Principal user = () -> decoded.getUid();
                    accessor.setUser(user);

                } catch (FirebaseAuthException e) {
                    throw new RuntimeException("Invalid Firebase token");
                }
            }
        }

        return message;
    }
}
