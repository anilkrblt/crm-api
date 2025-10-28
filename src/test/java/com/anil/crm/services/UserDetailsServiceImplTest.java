package com.anil.crm.services;

import com.anil.crm.domain.Role;
import com.anil.crm.domain.User; // Import your User entity
import com.anil.crm.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Import the exception

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*; // Use BDDMockito for given/when/then

@ExtendWith(MockitoExtension.class) // Enable Mockito integration
@DisplayName("UserDetails Service Implementation Unit Tests")
class UserDetailsServiceImplTest {

    @Mock // Create a mock for the UserRepository
    UserRepository userRepository;

    @InjectMocks // Create an instance of the service and inject the mocks
    UserDetailsServiceImpl userDetailsService;

    User user;
    String userEmail = "test@user.com";
    String nonExistentEmail = "notfound@user.com";

    @BeforeEach
    void setUp() {
        // Create a sample User entity for testing
        user = User.builder()
                .id(1L)
                .email(userEmail)
                .password("hashedPassword")
                .firstName("Test")
                .lastName("User")
                .role(Role.CUSTOMER)
                .build();
    }

    @Test
    @DisplayName("Load User By Username - Success (User Found)")
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        // --- Given ---
        // Define the mock behavior: when findByEmail is called with userEmail, return the sample user
        given(userRepository.findByEmail(userEmail)).willReturn(Optional.of(user));

        // --- When ---
        // Call the method under test
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        // --- Then ---
        // Assert that the returned UserDetails is not null and matches the expected user's email
        assertNotNull(userDetails);
        assertEquals(userEmail, userDetails.getUsername());
        // Verify that the repository method was called exactly once with the correct email
        then(userRepository).should(times(1)).findByEmail(userEmail);
    }

    @Test
    @DisplayName("Load User By Username - Failure (User Not Found)")
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenUserDoesNotExist() {
        // --- Given ---
        // Define the mock behavior: when findByEmail is called with a non-existent email, return empty
        given(userRepository.findByEmail(nonExistentEmail)).willReturn(Optional.empty());

        // --- When & Then ---
        // Assert that calling the method with the non-existent email throws the expected exception
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(nonExistentEmail);
        });

        // Optionally, assert the exception message
        assertTrue(exception.getMessage().contains("User not found with email: " + nonExistentEmail));

        // Verify that the repository method was called exactly once with the correct email
        then(userRepository).should(times(1)).findByEmail(nonExistentEmail);
    }
}