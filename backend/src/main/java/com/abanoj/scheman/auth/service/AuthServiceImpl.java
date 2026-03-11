package com.abanoj.scheman.auth.service;

import com.abanoj.scheman.auth.dto.*;
import com.abanoj.scheman.auth.entity.RefreshToken;
import com.abanoj.scheman.auth.entity.Role;
import com.abanoj.scheman.auth.entity.User;
import com.abanoj.scheman.auth.repository.RefreshTokenRepository;
import com.abanoj.scheman.auth.repository.UserRepository;
import com.abanoj.scheman.security.JwtProperties;
import com.abanoj.scheman.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .enabled(true)
                .build();

        userRepository.save(user);

        return generateTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        refreshTokenRepository.revokeAllByUser(user);

        return generateTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByTokenAndRevokedFalse(request.refreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (storedToken.getExpiryDate().isBefore(Instant.now())) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new IllegalArgumentException("Refresh token has expired");
        }

        if (!jwtService.isRefreshToken(storedToken.getToken())) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        return generateTokens(storedToken.getUser());
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByTokenAndRevokedFalse(request.refreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);
    }

    private AuthResponse generateTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenStr = jwtService.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenStr)
                .user(user)
                .expiryDate(Instant.now().plusMillis(jwtProperties.getRefresh().getExpiration()))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .build();
    }
}
