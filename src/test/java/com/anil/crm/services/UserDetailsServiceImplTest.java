package com.anil.crm.services;

import com.anil.crm.domain.Role;
import com.anil.crm.domain.User;
import com.anil.crm.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserDetailsServiceImpl userDetailsService;

    User sampleUser;
    String userEmail = "test@user.com";
    String nonExistentEmail = "notfound@user.com";

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email(userEmail)
                .password("hashedPassword")
                .firstName("Test")
                .lastName("User")
                .role(Role.CUSTOMER)
                .build();
    }

    @Test
    void loadUserByUsername() {
        given(userRepository.findByEmail(userEmail)).willReturn(Optional.of(sampleUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        assertNotNull(userDetails);
        assertEquals(userEmail, userDetails.getUsername());
        assertEquals("hashedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("CUSTOMER")));

        then(userRepository).should(times(1)).findByEmail(userEmail);
    }

    @Test
    void loadUserByUsername_NotFound() {
        given(userRepository.findByEmail(nonExistentEmail)).willReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(nonExistentEmail);
        });

        assertTrue(exception.getMessage().contains("User not found with email: " + nonExistentEmail));

        then(userRepository).should(times(1)).findByEmail(nonExistentEmail);
    }
}
