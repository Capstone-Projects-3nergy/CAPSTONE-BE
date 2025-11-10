package com.nw2.parcel.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.nw2.parcel.services.UsersService;
import com.nw2.parcel.entity.Users;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;

public class FirebaseAuthFilter extends OncePerRequestFilter {

    private final UsersService usersService;  // ✅ เปลี่ยนเป็น Service

    public FirebaseAuthFilter(UsersService usersService) {
        this.usersService = usersService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) return true;  // preflight
        String uri = req.getRequestURI();
        // ✅ ข้าม register และ public อื่น ๆ
        return (req.getMethod().equals("POST") && "/api/auth/register".equals(uri))
                || (req.getMethod().equals("GET")  && "/api/dorms".equals(uri));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws jakarta.servlet.ServletException, java.io.IOException {

        String header = req.getHeader("Authorization");

        // ✅ ไม่มี Bearer → ปล่อยผ่าน
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        try {
            FirebaseToken tok = FirebaseAuth.getInstance().verifyIdToken(header.substring(7));
            Users user = usersService.linkFirebaseOnLogin(tok);

            String roleName = "ROLE_" + (user != null && user.getRole()!=null ? user.getRole().name() : "GUEST");
            var auth = new UsernamePasswordAuthenticationToken(
                    user != null ? user.getUserId() : null,
                    null,
                    List.of(new SimpleGrantedAuthority(roleName))
            );
            auth.setDetails(tok);
            SecurityContextHolder.getContext().setAuthentication(auth);
            chain.doFilter(req, res);
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("{\"error\":\"Invalid Firebase token\"}");
        }
    }

}