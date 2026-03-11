package com.nw2.parcel.security;

import com.nw2.parcel.entity.Users;
import com.nw2.parcel.repositories.UsersRepository;
import com.nw2.parcel.services.FirebaseService;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private final FirebaseService firebaseService;
    private final UsersRepository usersRepository;

    public FirebaseAuthenticationFilter(FirebaseService firebaseService,
                                        UsersRepository usersRepository) {
        this.firebaseService = firebaseService;
        this.usersRepository = usersRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        return path.contains("/api/auth/login")
                || path.contains("/api/auth/signup")
                || path.contains("/api/line/")
                || path.contains("/webhook")
                || "OPTIONS".equalsIgnoreCase(method);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // 🔍 DEBUG LOG
        System.out.println("🔍 URI: " + request.getRequestURI() + " | Auth header: " + (header != null ? "present (length=" + header.length() + ")" : "NULL"));

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7).trim();

            // 🛑 1. ดักกรณี Token ส่งมาเป็นคำว่า "null" หรือ "undefined" หรือว่างเปล่า
            if (token.isEmpty() || token.equalsIgnoreCase("null") || token.equalsIgnoreCase("undefined")) {
                System.out.println("🔍 Token is empty/null/undefined: " + token);
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Token is missing or invalid (Received: " + token + ")"
                );
                return;
            }

            try {
                System.out.println("🔍 Verifying Firebase token...");
                FirebaseToken decoded = firebaseService.verifyIdToken(token);
                System.out.println("🔍 Token verified. UID: " + decoded.getUid());

                // หา user จาก firebase_uid
                Users userEntity = usersRepository
                        .findByFirebaseUid(decoded.getUid())
                        .orElse(null);

                if (userEntity == null) {
                    System.out.println("🔍 User not found for UID: " + decoded.getUid());
                    response.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "User not registered in system"
                    );
                    return;
                }

                System.out.println("🔍 User found: " + userEntity.getFirebaseUid() + " | Role: " + userEntity.getRole() + " | Status: " + userEntity.getStatus());

                if (userEntity.getStatus() != Users.Status.ACTIVE) {
                    System.out.println("🔍 User is not ACTIVE: " + userEntity.getStatus());
                    response.sendError(
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "User is not logged in"
                    );
                    return;
                }

                // 🛑 2. ดักกรณี Role ใน Database เป็นค่า Null
                if (userEntity.getRole() == null) {
                    System.err.println("❌ Security Error: User role is null for Firebase UID: " + userEntity.getFirebaseUid());
                    response.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "User role is not assigned"
                    );
                    return;
                }

                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(
                        new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().name())
                );

                System.out.println("🔍 Setting authentication with authorities: " + authorities);

                User principal = new User(decoded.getUid(), "", authorities);

                var auth = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );

                SecurityContextHolder.getContext().setAuthentication(auth);
                System.out.println("🔍 Authentication set successfully!");

            } catch (FirebaseAuthException e) {
                System.err.println("❌ FirebaseAuthException: " + e.getMessage());
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Invalid Firebase token"
                );
                return;

            } catch (IllegalArgumentException e) {
                System.err.println("❌ IllegalArgumentException in Token Verification: " + e.getMessage());
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Malformed token"
                );
                return;

            } catch (Exception ex) {
                System.err.println("❌ Internal Server Error in Filter: " + ex.getClass().getName() + " - " + ex.getMessage());
                ex.printStackTrace();
                response.sendError(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Token verification failed: " + ex.getMessage()
                );
                return;
            }
        } else {
            System.out.println("🔍 No Bearer token found, proceeding as anonymous");
        }

        chain.doFilter(request, response);
    }
}