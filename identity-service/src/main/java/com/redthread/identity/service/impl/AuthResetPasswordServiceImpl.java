package com.redthread.identity.service.impl;

import com.redthread.identity.model.User;
import com.redthread.identity.repository.UserRepository;
import com.redthread.identity.service.AuthResetPasswordService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthResetPasswordServiceImpl implements AuthResetPasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthResetPasswordServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean checkUserExists(String identifier) {
        if (identifier == null) return false;
        String email = identifier.trim().toLowerCase();
        if (email.isBlank()) return false;

        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public boolean resetPassword(String identifier, String newPassword) {
        if (identifier == null) return false;
        if (newPassword == null) return false;

        String email = identifier.trim().toLowerCase();
        String pass = newPassword.trim();

        if (email.isBlank()) return false;
        if (pass.isBlank()) return false;

        // regla mínima (puedes ajustarla si quieres)
        if (pass.length() < 6) return false;

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return false;

        user.setPassword(passwordEncoder.encode(pass));
        userRepository.save(user);

        return true;
    }
}
