package com.nw2.parcel.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Service;

@Service
public class FirebaseAuthService {

    public UserRecord createUserAndSendResetEmail(String email)
            throws FirebaseAuthException {

        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setEmailVerified(false)
                .setDisabled(false);

        UserRecord user = FirebaseAuth.getInstance().createUser(request);

        FirebaseAuth.getInstance().generatePasswordResetLink(email);

        return user;
    }
}
