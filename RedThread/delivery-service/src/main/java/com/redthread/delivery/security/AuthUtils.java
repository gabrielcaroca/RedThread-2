package com.redthread.delivery.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthUtils {

    public Long getCurrentUserId(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null)
            return null;
        Object userId = jwt.getClaim("user_id");
        if (userId instanceof Number n)
            return n.longValue();
        String sub = jwt.getSubject();
        try {
            return sub != null ? Long.parseLong(sub) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public boolean hasAdmin(Jwt jwt) {
        if (jwt == null)
            return false;
        Object roles = jwt.getClaim("roles");
        if (roles instanceof String r) {
            return r.contains("ADMIN");
        }
        if (roles instanceof Iterable<?> it) {
            for (Object r : it)
                if (String.valueOf(r).contains("ADMIN"))
                    return true;
        }
        return false;
    }

}
