package com.redthread.order.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtUserResolver {
  public String currentUserId() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) throw new IllegalStateException("No auth");
    Object principal = auth.getPrincipal();
    if (principal instanceof Jwt jwt) {
      Object uid = jwt.getClaim("user_id");
      if (uid == null) uid = jwt.getSubject();
      if (uid == null) throw new IllegalStateException("JWT sin user_id/sub");
      return String.valueOf(uid);
    }
    throw new IllegalStateException("Principal no es JWT");
  }
}
