package com.abanoj.scheman.auth;

import com.abanoj.scheman.auth.dto.*;
import com.abanoj.scheman.auth.entity.RefreshToken;
import com.abanoj.scheman.auth.entity.Role;
import com.abanoj.scheman.auth.entity.User;
import com.abanoj.scheman.auth.repository.RefreshTokenRepository;
import com.abanoj.scheman.auth.repository.UserRepository;
import com.abanoj.scheman.auth.service.AuthServiceImpl;
import com.abanoj.scheman.exception.AuthenticationFailedException;
import com.abanoj.scheman.exception.ConflictException;
import com.abanoj.scheman.exception.ResourceNotFoundException;
import com.abanoj.scheman.security.JwtProperties;
import com.abanoj.scheman.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    UserRepository userRepository;
    @Mock
    RefreshTokenRepository refreshTokenRepository;
    @Mock
    JwtService jwtService;
    @Mock
    JwtProperties jwtProperties;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    AuthServiceImpl authService;

    private User user;
    private UUID userId;
    private RefreshToken refreshToken;
    private String tokenString;

    @Mock
    JwtProperties.Refresh jwtRefresh;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        tokenString = "valid-refresh-token";

        user = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john@doe.com")
                .password("encodedPassword")
                .role(Role.EMPLOYEE)
                .enabled(true)
                .mustChangePassword(false)
                .build();

        refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token(tokenString)
                .user(user)
                .expiryDate(Instant.now().plusMillis(86400000))
                .revoked(false)
                .build();
    }

    @Nested
    @DisplayName("login")
    class Login {
        private LoginRequestDto loginRequestDto;

        @BeforeEach
        void setUp() {
            loginRequestDto = new LoginRequestDto("john@doe.com", "rawPassword");
        }

        @Test
        void shouldLogin_whenCredentialsAreValid() {
            //given
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willReturn(new UsernamePasswordAuthenticationToken(user, null));
            given(userRepository.findByEmail(loginRequestDto.email())).willReturn(Optional.of(user));
            willDoNothing().given(refreshTokenRepository).revokeAllByUser(user);
            given(jwtService.generateAccessToken(user)).willReturn("access-token");
            given(jwtService.generateRefreshToken(user)).willReturn("refresh-token");
            given(jwtProperties.getRefresh()).willReturn(jwtRefresh);
            given(jwtRefresh.getExpiration()).willReturn(86400000L);
            //when
            AuthResponseDto result = authService.login(loginRequestDto);
            //then
            assertThat(result.accessToken()).isEqualTo("access-token");
            assertThat(result.refreshToken()).isEqualTo("refresh-token");
            assertThat(result.mustChangePassword()).isFalse();
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(refreshTokenRepository).revokeAllByUser(user);
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        void shouldThrowAuthenticationFailed_whenUserNotFound() {
            //given
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willReturn(new UsernamePasswordAuthenticationToken(user, null));
            given(userRepository.findByEmail(loginRequestDto.email())).willReturn(Optional.empty());
            //when -> then
            assertThatThrownBy(() -> authService.login(loginRequestDto))
                    .isInstanceOf(AuthenticationFailedException.class)
                    .hasMessageContaining("Invalid email or password");
            verify(refreshTokenRepository, never()).revokeAllByUser(any());
        }

        @Test
        void shouldThrowAuthenticationFailed_whenAuthenticationFails() {
            //given
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willThrow(new BadCredentialsException("Bad credentials"));
            //when -> then
            assertThatThrownBy(() -> authService.login(loginRequestDto))
                    .isInstanceOf(BadCredentialsException.class);
            verify(userRepository, never()).findByEmail(any());
        }
    }

    @Nested
    @DisplayName("refresh")
    class Refresh {
        private RefreshTokenRequestDto refreshRequestDto;

        @BeforeEach
        void setUp() {
            refreshRequestDto = new RefreshTokenRequestDto(tokenString);
        }

        @Test
        void shouldRefresh_whenTokenIsValid() {
            //given
            given(refreshTokenRepository.findByTokenAndRevokedFalse(tokenString))
                    .willReturn(Optional.of(refreshToken));
            given(jwtService.isRefreshToken(tokenString)).willReturn(true);
            given(jwtService.generateAccessToken(user)).willReturn("new-access-token");
            given(jwtService.generateRefreshToken(user)).willReturn("new-refresh-token");
            given(jwtProperties.getRefresh()).willReturn(jwtRefresh);
            given(jwtRefresh.getExpiration()).willReturn(86400000L);
            //when
            AuthResponseDto result = authService.refresh(refreshRequestDto);
            //then
            assertThat(result.accessToken()).isEqualTo("new-access-token");
            assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
            assertThat(refreshToken.isRevoked()).isTrue();
            verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
        }

        @Test
        void shouldThrowAuthenticationFailed_whenTokenNotFound() {
            //given
            given(refreshTokenRepository.findByTokenAndRevokedFalse(tokenString))
                    .willReturn(Optional.empty());
            //when -> then
            assertThatThrownBy(() -> authService.refresh(refreshRequestDto))
                    .isInstanceOf(AuthenticationFailedException.class)
                    .hasMessageContaining("Invalid refresh token");
        }

        @Test
        void shouldThrowAuthenticationFailed_whenTokenIsExpired() {
            //given
            refreshToken.setExpiryDate(Instant.now().minusMillis(86400000));
            given(refreshTokenRepository.findByTokenAndRevokedFalse(tokenString))
                    .willReturn(Optional.of(refreshToken));
            //when -> then
            assertThatThrownBy(() -> authService.refresh(refreshRequestDto))
                    .isInstanceOf(AuthenticationFailedException.class)
                    .hasMessageContaining("Refresh token has expired");
            assertThat(refreshToken.isRevoked()).isTrue();
            verify(refreshTokenRepository).save(refreshToken);
        }

        @Test
        void shouldThrowAuthenticationFailed_whenTokenIsNotRefreshType() {
            //given
            given(refreshTokenRepository.findByTokenAndRevokedFalse(tokenString))
                    .willReturn(Optional.of(refreshToken));
            given(jwtService.isRefreshToken(tokenString)).willReturn(false);
            //when -> then
            assertThatThrownBy(() -> authService.refresh(refreshRequestDto))
                    .isInstanceOf(AuthenticationFailedException.class)
                    .hasMessageContaining("Invalid refresh token");
        }

        @Test
        void shouldThrowAuthenticationFailed_whenUserIsDisabled() {
            //given
            user.setEnabled(false);
            given(refreshTokenRepository.findByTokenAndRevokedFalse(tokenString))
                    .willReturn(Optional.of(refreshToken));
            given(jwtService.isRefreshToken(tokenString)).willReturn(true);
            //when -> then
            assertThatThrownBy(() -> authService.refresh(refreshRequestDto))
                    .isInstanceOf(AuthenticationFailedException.class)
                    .hasMessageContaining("User account is disabled");
            assertThat(refreshToken.isRevoked()).isTrue();
            verify(refreshTokenRepository).save(refreshToken);
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {
        private ChangePasswordRequestDto changePasswordRequestDto;

        @BeforeEach
        void setUp() {
            changePasswordRequestDto = new ChangePasswordRequestDto(
                    "rawPassword", "NewPass1!", "NewPass1!"
            );
        }

        @Test
        void shouldChangePassword_whenOldPasswordMatches_andNewPasswordIsDifferent() {
            //given
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("rawPassword", "encodedPassword")).willReturn(true);
            given(passwordEncoder.matches("NewPass1!", "encodedPassword")).willReturn(false);
            given(passwordEncoder.encode("NewPass1!")).willReturn("newEncodedPassword");
            //when
            authService.changePassword(userId, changePasswordRequestDto);
            //then
            assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
            assertThat(user.isMustChangePassword()).isFalse();
            verify(userRepository).save(user);
        }

        @Test
        void shouldThrowResourceNotFound_whenUserDoesNotExist() {
            //given
            given(userRepository.findById(userId)).willReturn(Optional.empty());
            //when -> then
            assertThatThrownBy(() -> authService.changePassword(userId, changePasswordRequestDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(userId.toString());
            verify(userRepository, never()).save(any());
        }

        @Test
        void shouldThrowAuthenticationFailed_whenOldPasswordDoesNotMatch() {
            //given
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("rawPassword", "encodedPassword")).willReturn(false);
            //when -> then
            assertThatThrownBy(() -> authService.changePassword(userId, changePasswordRequestDto))
                    .isInstanceOf(AuthenticationFailedException.class)
                    .hasMessageContaining("Invalid password");
            verify(userRepository, never()).save(any());
        }

        @Test
        void shouldThrowIllegalArgument_whenNewPasswordIsSameAsOld() {
            //given
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("rawPassword", "encodedPassword")).willReturn(true);
            given(passwordEncoder.matches("NewPass1!", "encodedPassword")).willReturn(true);
            //when -> then
            assertThatThrownBy(() -> authService.changePassword(userId, changePasswordRequestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("New password must be different");
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("logout")
    class Logout {
        private RefreshTokenRequestDto logoutRequestDto;

        @BeforeEach
        void setUp() {
            logoutRequestDto = new RefreshTokenRequestDto(tokenString);
        }

        @Test
        void shouldLogout_whenTokenIsValid() {
            //given
            given(refreshTokenRepository.findByTokenAndRevokedFalse(tokenString))
                    .willReturn(Optional.of(refreshToken));
            //when
            authService.logout(logoutRequestDto);
            //then
            assertThat(refreshToken.isRevoked()).isTrue();
            verify(refreshTokenRepository).save(refreshToken);
        }

        @Test
        void shouldThrowAuthenticationFailed_whenTokenNotFound() {
            //given
            given(refreshTokenRepository.findByTokenAndRevokedFalse(tokenString))
                    .willReturn(Optional.empty());
            //when -> then
            assertThatThrownBy(() -> authService.logout(logoutRequestDto))
                    .isInstanceOf(AuthenticationFailedException.class)
                    .hasMessageContaining("Invalid refresh token");
        }
    }

    @Nested
    @DisplayName("createManager")
    class CreateManager {
        private ManagerCreateRequestDto managerCreateRequestDto;

        @BeforeEach
        void setUp() {
            managerCreateRequestDto = new ManagerCreateRequestDto(
                    "Jane", "Smith", "jane@smith.com", "Manager1!"
            );
        }

        @Test
        void shouldCreateManager_whenEmailIsNotInUse() {
            //given
            given(userRepository.existsByEmail(managerCreateRequestDto.email())).willReturn(false);
            given(passwordEncoder.encode(managerCreateRequestDto.password())).willReturn("encodedManagerPassword");
            //when
            ManagerResponseDto result = authService.createManager(managerCreateRequestDto);
            //then
            assertThat(result.firstName()).isEqualTo("Jane");
            assertThat(result.lastName()).isEqualTo("Smith");
            assertThat(result.email()).isEqualTo("jane@smith.com");
            verify(userRepository).save(any(User.class));
        }

        @Test
        void shouldThrowConflict_whenEmailIsAlreadyInUse() {
            //given
            given(userRepository.existsByEmail(managerCreateRequestDto.email())).willReturn(true);
            //when -> then
            assertThatThrownBy(() -> authService.createManager(managerCreateRequestDto))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Email already in use");
            verify(userRepository, never()).save(any());
        }
    }
}
