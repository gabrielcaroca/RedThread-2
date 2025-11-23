package com.redthread.identity.service.impl;

import com.redthread.identity.service.AuthResetPasswordService;
import org.springframework.stereotype.Service;

@Service
public class AuthResetPasswordServiceImpl implements AuthResetPasswordService {

    @Override
    public boolean checkUserExists(String identifier) {
        // TODO real: buscar usuario por email/username
        // por ahora simulado:
        return identifier != null && !identifier.isBlank();
    }

    @Override
    public boolean resetPassword(String identifier, String newPassword) {
        // TODO real: validar token + actualizar password encodeado
        // por ahora simulado:
        if (identifier == null || identifier.isBlank()) return false;
        if (newPassword == null || newPassword.isBlank()) return false;
        return true;
    }
}
