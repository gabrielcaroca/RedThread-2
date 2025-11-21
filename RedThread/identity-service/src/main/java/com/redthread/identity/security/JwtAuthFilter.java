package com.redthread.identity.security;

import com.redthread.identity.model.User;
import com.redthread.identity.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepo;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepo) {
        this.jwtService = jwtService;
        this.userRepo = userRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                var key = jwtService.getKey();
                if (key == null) {
                    System.err.println("JwtService key is null — revisar @PostConstruct o configuración del secret");
                    chain.doFilter(req, res);
                    return;
                }

                var parser = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build();

                var claims = parser.parseClaimsJws(token).getBody();
                Long userId = Long.valueOf(claims.getSubject());

                var user = userRepo.findById(userId).orElse(null);
                if (user != null) {
                    String rolesStr = (String) claims.get("roles");
                    var authorities = Arrays.stream(rolesStr.split(","))
                            .filter(StringUtils::hasText)
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                            .collect(Collectors.toList());

                    var auth = new UsernamePasswordAuthenticationToken(user, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                System.err.println("❌ Error JWT: " + e.getClass().getSimpleName() + " → " + e.getMessage());
            }
        }

        chain.doFilter(req, res);
    }
}
