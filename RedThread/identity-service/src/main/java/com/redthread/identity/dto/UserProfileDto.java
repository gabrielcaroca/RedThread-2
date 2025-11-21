package com.redthread.identity.dto;

import java.util.Set;

public class UserProfileDto {
    private Long id;
    private String fullName;
    private String email;
    private Set<String> roles;
    public UserProfileDto(Long id, String fullName, String email, Set<String> roles) {
        this.id = id; this.fullName = fullName; this.email = email; this.roles = roles;
    }
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Set<String> getRoles() { return roles; }
}
