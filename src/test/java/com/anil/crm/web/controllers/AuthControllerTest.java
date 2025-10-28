package com.anil.crm.web.controllers;

import com.anil.crm.domain.Role;
import com.anil.crm.domain.User; // Import your User entity
import com.anil.crm.services.JwtService;
import com.anil.crm.web.models.AuthenticationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
// Import necessary Security classes
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections; // For roles/authorities
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AuthController.class) // Target only AuthController
@DisplayName("Auth Controller Unit Tests")
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AuthenticationManager authenticationManager; // Mocked dependency

    @MockitoBean
    UserDetailsService userDetailsService; // Mocked dependency

    @MockitoBean
    JwtService jwtService; // Mocked dependency

    @Autowired
    ObjectMapper objectMapper;

    User sampleUser;
    AuthenticationRequest validLoginRequest;
    AuthenticationRequest invalidLoginRequest;
    String sampleJwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGkudmVsaUBlbWFpbC5jb20ifQ.SIGNATURE"; // Dummy JWT

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .firstName("Ali")
                .lastName("Veli")
                .email("ali.veli@email.com")
                .password("hashedPassword") // Not used directly in test, but good practice
                .role(Role.CUSTOMER)
                .build();

        validLoginRequest = AuthenticationRequest.builder()
                .email("ali.veli@email.com")
                .password("123456")
                .build();

        invalidLoginRequest = AuthenticationRequest.builder()
                .email("ali.veli@email.com")
                .password("wrongPassword")
                .build();
    }

    @Test
    @DisplayName("Login - Success")
    void login_shouldReturnToken_whenCredentialsAreValid() throws Exception {
        // --- Given ---
        // 1. Mock AuthenticationManager: Simulate successful authentication (doesn't throw exception)
        //    (For authenticate, returning null or a mock Authentication object is usually sufficient)
        given(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(null); // Or mock(Authentication.class)

        // 2. Mock UserDetailsService: Return our sample user when requested
        given(userDetailsService.loadUserByUsername(validLoginRequest.getEmail())).willReturn(sampleUser);

        // 3. Mock JwtService: Return a dummy token when generateToken is called
        given(jwtService.generateToken(anyMap(), eq(sampleUser))).willReturn(sampleJwt);

        // --- When & Then ---
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk()) // Expect HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", is(sampleJwt))); // Expect the token in the response

        // --- Verify ---
        // Verify authenticate was called once with the correct credentials
        then(authenticationManager).should(times(1)).authenticate(
                argThat(token -> token.getName().equals(validLoginRequest.getEmail()) &&
                        token.getCredentials().equals(validLoginRequest.getPassword()))
        );
        // Verify userDetailsService was called once
        then(userDetailsService).should(times(1)).loadUserByUsername(validLoginRequest.getEmail());
        // Verify generateToken was called once with the correct user and claims
        then(jwtService).should(times(1)).generateToken(anyMap(), eq(sampleUser));
    }

    @Test
    @DisplayName("Login - Failure (Bad Credentials)")
    void login_shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception {
        // --- Given ---
        // 1. Mock AuthenticationManager: Simulate failed authentication by throwing exception
        given(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new BadCredentialsException("Invalid credentials"));

        // --- When & Then ---
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoginRequest)))
                // Spring Security's ExceptionTranslationFilter usually converts BadCredentialsException
                // to 401 Unauthorized or sometimes 403 Forbidden depending on config.
                // Test for 401 first, adjust if your GlobalExceptionHandler or SecurityConfig behaves differently.
                .andExpect(status().isUnauthorized()); // Expect HTTP 401 Unauthorized or 403 Forbidden

        // --- Verify ---
        // Verify authenticate was called
        then(authenticationManager).should(times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        // Verify that userDetailsService and jwtService were NOT called after authentication failed
        then(userDetailsService).should(never()).loadUserByUsername(anyString());
        then(jwtService).should(never()).generateToken(anyMap(), any(User.class));
    }
}