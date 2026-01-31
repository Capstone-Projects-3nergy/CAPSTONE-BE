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

//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String path = request.getServletPath();
//
//        return path.startsWith("/api/dorms")
//                || path.startsWith("/api/auth")
//                || "OPTIONS".equalsIgnoreCase(request.getMethod());
//    }
@Override
protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();

    // allow เฉพาะ signup + login
    if (path.equals("/api/auth/login") || path.equals("/api/auth/signup")) {
        return true;
    }

    return path.startsWith("/api/dorms")
            || "OPTIONS".equalsIgnoreCase(request.getMethod());
}


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.replace("Bearer ", "");
            try {
                FirebaseToken decoded = firebaseService.verifyIdToken(token);

                // 🟦 หา user ใน DB จาก firebase_uid
                Users userEntity = usersRepository.findByFirebaseUid(decoded.getUid())
                        .orElse(null);


//                if (userEntity == null || userEntity.getStatus() != Users.Status.ACTIVE) {
//                    response.sendError(
//                            HttpServletResponse.SC_FORBIDDEN,
//                            "Account not activated"
//                    );
//                    return;
//                }
//
                List<GrantedAuthority> authorities = new ArrayList<>();
//
//                if (userEntity == null) {
//                    response.sendError(
//                            HttpServletResponse.SC_FORBIDDEN,
//                            "User not registered in system"
//                    );
//                    return;
//                }


                if (userEntity == null) {
                    response.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "User not registered in system"
                    );
                    return;
                }

                if (userEntity.getStatus() != Users.Status.ACTIVE) {
                    response.sendError(
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "User is not logged in"
                    );
                    return;
                }


                authorities.add(
                        new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().name())
                );

                User principal = new User(decoded.getUid(), "", authorities);

                var auth = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (FirebaseAuthException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Firebase token");
                return;
            } catch (Exception ex) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Token verification failed");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}