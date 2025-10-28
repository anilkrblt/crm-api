package com.anil.crm.web.controllers;

import com.anil.crm.domain.User;
import com.anil.crm.services.JwtService;
import com.anil.crm.web.models.AuthenticationRequest;
import com.anil.crm.web.models.AuthenticationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;


    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request
    ) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword())
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        Map<String, Object> extraClaims = new HashMap<>();


        if (userDetails instanceof User user) {
            extraClaims.put("userId", user.getId());
            extraClaims.put("firstName", user.getFirstName());
            extraClaims.put("lastName", user.getLastName());

            var roles = user.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            extraClaims.put("roles", roles);
        }

        final String jwt = jwtService.generateToken(extraClaims, userDetails);

        return ResponseEntity.ok(AuthenticationResponse.builder().token(jwt).build());
    }
}