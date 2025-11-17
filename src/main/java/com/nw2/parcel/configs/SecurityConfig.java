package com.nw2.parcel.configs;

import com.nw2.parcel.repositories.UsersRepository;
import com.nw2.parcel.security.FirebaseAuthenticationFilter;
import com.nw2.parcel.services.FirebaseService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final FirebaseService firebaseService;
    private final UsersRepository usersRepository;

    public SecurityConfig(FirebaseService firebaseService,
                          UsersRepository usersRepository) {
        this.firebaseService = firebaseService;
        this.usersRepository = usersRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/dorms/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        // 🟦 /api/residents/** ให้เฉพาะ STAFF
                        .requestMatchers("/api/residents/**").hasAuthority("STAFF")

                        // 🔒 ที่เหลือต้องแค่ login ก็พอ authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new FirebaseAuthenticationFilter(firebaseService, usersRepository),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}


//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    private final FirebaseService firebaseService;
//
//    public SecurityConfig(FirebaseService firebaseService) {
//        this.firebaseService = firebaseService;
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//                                .requestMatchers("/api/auth/signup", "/api/auth/login","/api/dorms/**").permitAll()
////                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/dorms/**").permitAll()
//                                // ✅ อนุญาต preflight (ถ้ามี CORS)
//                                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
//                                // 🔒 ที่เหลือต้อง authenticated
//                                .anyRequest().authenticated()
//                )
//                .addFilterBefore(
//                        new com.nw2.parcel.configs.FirebaseAuthenticationFilter(firebaseService),
//                        UsernamePasswordAuthenticationFilter.class
//                );
//
//        return http.build();
//    }
//}

