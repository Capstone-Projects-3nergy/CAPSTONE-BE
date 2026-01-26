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
                        .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/dorms/**","/api/public/parcels/**","/api/public/email/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/companies/**", "/api/residents/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        // เฉพาะ ADMIN เท่านั้นที่เข้า /api/admin/**
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                        // 🟦 /api/residents/** ให้เฉพาะ STAFF
//                        .requestMatchers("/api/residents/**").hasAuthority("STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/dorms/**").hasAuthority("STAFF")
                        .requestMatchers(HttpMethod.POST, "/api/parcels/**").hasAuthority("STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/parcels/**").hasAuthority("STAFF")
                        .requestMatchers(HttpMethod.DELETE, "/api/parcels/**").hasAuthority("STAFF")
                        .requestMatchers("/api/trash/**").hasAuthority("STAFF")
                        .requestMatchers("/api/staff/users/**").hasAuthority("STAFF") //management


                                // 🟢 resident ดูและคอนเฟิร์มพัสดุของตัวเองเท่านั้น
                        .requestMatchers("/api/OwnerParcels/**").hasAuthority("RESIDENT")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new FirebaseAuthenticationFilter(firebaseService, usersRepository),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}

