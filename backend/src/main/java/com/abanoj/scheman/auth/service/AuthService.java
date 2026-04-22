package com.abanoj.scheman.auth.service;

import com.abanoj.scheman.auth.dto.*;

import java.util.UUID;

public interface AuthService {
    AuthResponseDto login(LoginRequestDto request);
    AuthResponseDto refresh(RefreshTokenRequestDto request);
    void logout(RefreshTokenRequestDto request);
    void changePassword(UUID id, ChangePasswordRequestDto request);
    ManagerResponseDto createManager(ManagerCreateRequestDto managerCreateRequestDto);
}
