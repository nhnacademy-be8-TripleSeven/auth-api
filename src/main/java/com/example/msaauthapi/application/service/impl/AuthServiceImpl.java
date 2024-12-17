package com.example.msaauthapi.application.service.impl;

import com.example.msaauthapi.adaptor.MemberAdapter;
import com.example.msaauthapi.application.service.AuthService;
import com.example.msaauthapi.common.jwt.JwtProvider;
import com.example.msaauthapi.common.jwt.TokenInfo;
import com.example.msaauthapi.dto.MemberDto;
import com.example.msaauthapi.dto.request.MemberLoginRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberAdapter memberAdapter;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    public TokenInfo login(MemberLoginRequest memberLoginRequest, HttpServletResponse response) {
        MemberDto member = memberAdapter.getMember(memberLoginRequest.getLoginId());
        if (!passwordEncoder.matches(memberLoginRequest.getPassword(), member.getMemberAccount().getPassword())) {
            throw new IllegalArgumentException();
        }
        return jwtProvider.generateToken(member, response);
    }

    @Override
    public TokenInfo reIssueJwt(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        String refreshToken = "";

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refreshToken")) {
                refreshToken = cookie.getValue();
            }
        }

        return jwtProvider.reissueToken(refreshToken, response);
    }

    @Override
    public TokenInfo adminLogin(MemberLoginRequest loginRequest, HttpServletResponse response) {
        MemberDto member = memberAdapter.getMember(loginRequest.getLoginId());

        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getMemberAccount().getPassword())) {
            throw new IllegalArgumentException(); // 401
        }

        if (!hasAdminRole(member.getRoles())) {
            throw new IllegalArgumentException(); // 403
        }

        return jwtProvider.generateToken(member, response);
    }

    private boolean hasAdminRole(List<String> roles) {

        for (String role : roles) {
            if (role.contains("ADMIN")) {
                return true;
            }
        }

        return false;
    }
}
