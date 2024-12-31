package com.example.msaauthapi.application.service;

import com.example.msaauthapi.common.jwt.TokenInfo;
import com.example.msaauthapi.dto.MemberDto;
import com.example.msaauthapi.dto.request.MemberLoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    TokenInfo login(MemberLoginRequest loginRequest);
    TokenInfo reIssueJwt(String refreshToken);
    TokenInfo adminLogin(MemberLoginRequest loginRequest);
    void logout(Long userId);

    TokenInfo paycoLogin(String clientId);

}
