package com.redthread.identity.security;

import com.redthread.identity.model.Role;
import com.redthread.identity.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwt;

    @BeforeEach
    void setUp() {
        jwt = new JwtService();
        ReflectionTestUtils.setField(jwt, "secret", "0123456789012345678901234567890123456789"); // 40 chars
        ReflectionTestUtils.setField(jwt, "issuer", "redthread");
        ReflectionTestUtils.setField(jwt, "expiryMinutes", 120L);
        jwt.init();
    }

    @Test
    void generate_containsRolesClaim() {
        Role r1 = new Role(); r1.setKey("CLIENTE");
        Role r2 = new Role(); r2.setKey("ADMIN");

        User u = new User();
        ReflectionTestUtils.setField(u, "id", 7L);

        u.setEmail("x@mail.com");
        u.setFullName("X");
        u.setRoles(Set.of(r1, r2));

        String token = jwt.generate(u);
        var claims = jwt.parse(token).getBody();

        assertEquals("7", claims.getSubject());
        assertEquals("redthread", claims.getIssuer());
        assertEquals("x@mail.com", claims.get("email"));
        assertTrue(((String) claims.get("roles")).contains("CLIENTE"));
        assertTrue(((String) claims.get("roles")).contains("ADMIN"));
        assertTrue(jwt.isValid(token));
    }

    @Test
    void isValid_false_onBadToken() {
        assertFalse(jwt.isValid("no.es.un.token"));
    }
}
