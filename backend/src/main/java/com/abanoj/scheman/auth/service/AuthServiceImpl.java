package com.abanoj.scheman.auth.service;

import com.abanoj.scheman.auth.dto.*;
import com.abanoj.scheman.auth.entity.RefreshToken;
import com.abanoj.scheman.auth.entity.Role;
import com.abanoj.scheman.auth.entity.User;
import com.abanoj.scheman.auth.repository.RefreshTokenRepository;
import com.abanoj.scheman.auth.repository.UserRepository;
import com.abanoj.scheman.exception.AuthenticationFailedException;
import com.abanoj.scheman.exception.ConflictException;
import com.abanoj.scheman.security.JwtProperties;
import com.abanoj.scheman.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AuthResponseDto login(LoginRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthenticationFailedException("Invalid email or password"));

        refreshTokenRepository.revokeAllByUser(user);
        log.info("User authenticated {}", request.email());
        return generateTokens(user);
    }

    @Override
    @Transactional
    public AuthResponseDto refresh(RefreshTokenRequestDto request) {
        RefreshToken storedToken = refreshTokenRepository.findByTokenAndRevokedFalse(request.refreshToken())
                .orElseThrow(() -> new AuthenticationFailedException("Invalid refresh token"));

        if (storedToken.getExpiryDate().isBefore(Instant.now())) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            log.warn("Refresh token expired for user: {}", storedToken.getUser().getEmail());
            throw new AuthenticationFailedException("Refresh token has expired");
        }

        if (!jwtService.isRefreshToken(storedToken.getToken())) {
            log.warn("Invalid refresh token for user: {}", storedToken.getUser().getEmail());
            throw new AuthenticationFailedException("Invalid refresh token");
        }

        if(!storedToken.getUser().isEnabled()){
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            log.warn("User account is disable for user: {}", storedToken.getUser().getEmail());
            throw new AuthenticationFailedException("User account is disabled");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);
        log.info("Refresh token generated for user: {}", storedToken.getUser().getEmail());
        return generateTokens(storedToken.getUser());
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequestDto request) {
        RefreshToken storedToken = refreshTokenRepository.findByTokenAndRevokedFalse(request.refreshToken())
                .orElseThrow(() -> new AuthenticationFailedException("Invalid refresh token"));

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);
        log.info("Successfully logout for user: {}", storedToken.getUser().getEmail());
    }

    @Override
    @Transactional
    public ManagerResponseDto createManager(ManagerCreateRequestDto managerCreateRequestDto) {
        if(userRepository.existsByEmail(managerCreateRequestDto.email())){
            throw new ConflictException("Email already in use");
        }
        User user = User.builder()
                .firstName(managerCreateRequestDto.firstName())
                .lastName(managerCreateRequestDto.lastName())
                .email(managerCreateRequestDto.email())
                .password(passwordEncoder.encode(managerCreateRequestDto.password()))
                .role(Role.MANAGER)
                .build();
        userRepository.save(user);
        log.info("Create manager: {}", user.getEmail());
        return ManagerResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }

    private AuthResponseDto generateTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenStr = jwtService.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenStr)
                .user(user)
                .expiryDate(Instant.now().plusMillis(jwtProperties.getRefresh().getExpiration()))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .build();
    }
}
