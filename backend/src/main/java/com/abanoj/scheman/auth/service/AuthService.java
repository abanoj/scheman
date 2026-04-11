package com.abanoj.scheman.auth.service;

import com.abanoj.scheman.auth.dto.*;

public interface AuthService {
    AuthResponseDto login(LoginRequestDto request);
    AuthResponseDto refresh(RefreshTokenRequestDto request);
    void logout(RefreshTokenRequestDto request);
    ManagerResponseDto createManager(ManagerCreateRequestDto managerCreateRequestDto);
}
