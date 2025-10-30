package com.anil.crm.web.controllers;

import com.anil.crm.config.SecurityConfig;
import com.anil.crm.domain.Role;
import com.anil.crm.domain.User;
import com.anil.crm.services.JwtService;
import com.anil.crm.web.models.AuthenticationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class,
        excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class})
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthenticationManager authenticationManager;
    @MockitoBean
    UserDetailsService userDetailsService;
    @MockitoBean
    JwtService jwtService;

    User sampleUser;
    AuthenticationRequest validLoginRequest;
    AuthenticationRequest invalidLoginRequest;
    String sampleJwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QHVzZXIuY29tIiwicm9sZXMiOlsiQ1VTVE9NRVIiXSwiaWF0IjoxNjE2NjY2NjY2fQ.fakeTokenSignature";

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("test@user.com")
                .password("hashedPassword")
                .firstName("Test")
                .lastName("User")
                .role(Role.CUSTOMER)
                .build();

        validLoginRequest = AuthenticationRequest.builder()
                .email("test@user.com")
                .password("123456")
                .build();

        invalidLoginRequest = AuthenticationRequest.builder()
                .email("test@user.com")
                .password("wrongpassword")
                .build();
    }

    @Test
    void login_Success() throws Exception {
        given(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(null);
        given(userDetailsService.loadUserByUsername(validLoginRequest.getEmail())).willReturn(sampleUser);
        given(jwtService.generateToken(any(Map.class), eq(sampleUser))).willReturn(sampleJwt);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk());

        then(authenticationManager).should(times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        then(userDetailsService).should(times(1)).loadUserByUsername(validLoginRequest.getEmail());
        then(jwtService).should(times(1)).generateToken(any(Map.class), eq(sampleUser));
    }

    @Test
    void login_Failure_BadCredentials() throws Exception {
        given(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoginRequest)))
                .andExpect(status().isUnauthorized());

        then(authenticationManager).should(times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        then(userDetailsService).should(never()).loadUserByUsername(anyString());
        then(jwtService).should(never()).generateToken(any(Map.class), any(UserDetails.class));
    }
}

