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
                        // Public endpoints
                        .requestMatchers("/api/auth/signup", "/api/auth/login").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/companies/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ADMIN endpoints
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                        // STAFF endpoints - ใช้ hasAuthority แทน hasRole
                        .requestMatchers("/api/staff/**").hasAuthority("ROLE_STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/dorms/**").hasAuthority("ROLE_STAFF")
                        .requestMatchers(HttpMethod.POST, "/api/parcels/**").hasAuthority("ROLE_STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/parcels/**").hasAuthority("ROLE_STAFF")
                        .requestMatchers(HttpMethod.DELETE, "/api/parcels/**").hasAuthority("ROLE_STAFF")
                        .requestMatchers("/api/trash/**").hasAuthority("ROLE_STAFF")

                        // RESIDENT endpoints
                        .requestMatchers("/api/OwnerParcels/**").hasAuthority("ROLE_RESIDENT")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new FirebaseAuthenticationFilter(firebaseService, usersRepository),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}

