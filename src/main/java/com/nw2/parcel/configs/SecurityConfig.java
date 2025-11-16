package com.nw2.parcel.configs;

import com.nw2.parcel.services.FirebaseService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final FirebaseService firebaseService;

    public SecurityConfig(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/api/auth/signup", "/api/auth/login","/api/dorms/**").permitAll()
//                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/dorms/**").permitAll()
                                // ✅ อนุญาต preflight (ถ้ามี CORS)
                                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                                // 🔒 ที่เหลือต้อง authenticated
                                .anyRequest().permitAll()
                )
                .addFilterBefore(
                        new com.nw2.parcel.configs.FirebaseAuthenticationFilter(firebaseService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}

