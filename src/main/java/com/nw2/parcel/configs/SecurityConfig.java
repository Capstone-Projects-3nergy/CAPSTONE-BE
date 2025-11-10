// com.nw2.parcel.configs.SecurityConfig
package com.nw2.parcel.configs;

import com.nw2.parcel.security.FirebaseAuthFilter;
import com.nw2.parcel.services.UsersService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final UsersService usersService;

    public SecurityConfig(UsersService usersService) {
        this.usersService = usersService;
    }

    // com.nw2.parcel.configs.SecurityConfig
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ เปิดทั้งก้อน /auth/** (ไม่ใส่ /api)
                        .requestMatchers("/auth/**").permitAll()

                        // public GET
                        .requestMatchers(HttpMethod.GET, "/dorms").permitAll()

                        .requestMatchers("/staff/**").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers("/resident/**").hasRole("RESIDENT")

                        // ถ้ามี endpoint อื่นต้องล็อกอิน:
                        .requestMatchers("/**").authenticated()

                        .anyRequest().denyAll()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"Unauthenticated\"}");
                        })
                        .accessDeniedHandler((req, res, ex) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"Access denied by security rules\"}");
                        })
                )
                .addFilterBefore(new FirebaseAuthFilter(usersService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
