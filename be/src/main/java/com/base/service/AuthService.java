package com.base.service;

import com.base.dto.request.LoginRequest;
import com.base.dto.request.RefreshTokenRequest;
import com.base.dto.request.RegisterRequest;
import com.base.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String username);
}
