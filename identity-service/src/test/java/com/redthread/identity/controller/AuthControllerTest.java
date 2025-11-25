package com.redthread.identity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redthread.identity.dto.JwtResponse;
import com.redthread.identity.dto.LoginRequest;
import com.redthread.identity.dto.RegisterRequest;
import com.redthread.identity.dto.VerifyTokenRequest;
import com.redthread.identity.service.AuthResetPasswordService;
import com.redthread.identity.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean AuthService authService;
    @MockBean AuthResetPasswordService resetService;

    @Test
    void register_returns200() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new JwtResponse("TOKEN", Instant.now().plusSeconds(7200)));

        RegisterRequest req = new RegisterRequest();
        req.setEmail("a@mail.com");
        req.setFullName("A");
        req.setPassword("123456"); // >= 6 para pasar validacion

        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("TOKEN"));
    }

    @Test
    void login_invalid_returns401() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED
                ));

        LoginRequest req = new LoginRequest();
        req.setEmail("a@mail.com");
        req.setPassword("bad");

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void verify_ok_returns200() throws Exception {
        VerifyTokenRequest req = new VerifyTokenRequest("TOKEN");

        mvc.perform(post("/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void verify_invalid_returns401() throws Exception {
        doThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED
        )).when(authService).verify("BAD");

        VerifyTokenRequest req = new VerifyTokenRequest("BAD");

        mvc.perform(post("/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
