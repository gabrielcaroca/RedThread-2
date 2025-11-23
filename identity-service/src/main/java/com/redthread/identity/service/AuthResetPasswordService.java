package com.redthread.identity.service;

public interface AuthResetPasswordService {

    boolean checkUserExists(String identifier);

    boolean resetPassword(String identifier, String newPassword);
}
