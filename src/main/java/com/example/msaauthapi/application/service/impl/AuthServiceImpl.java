package com.example.msaauthapi.application.service.impl;

import com.example.msaauthapi.adaptor.MemberAdapter;
import com.example.msaauthapi.application.service.AuthService;
import com.example.msaauthapi.common.jwt.JwtProvider;
import com.example.msaauthapi.common.jwt.TokenInfo;
import com.example.msaauthapi.dto.MemberDto;
import com.example.msaauthapi.dto.request.MemberLoginRequest;
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
    public TokenInfo login(MemberLoginRequest loginRequest) {
        MemberDto member = memberAdapter.getMember(loginRequest.getLoginId());
        if (!passwordEncoder.matches(member.getMemberAccount().getPassword(), loginRequest.getPassword())) {
            throw new IllegalArgumentException();
        }
        return jwtProvider.generateToken(member);
    }

    @Override
    public TokenInfo reIssueJwt(String refreshToken) {
        return jwtProvider.reissueToken(refreshToken);
    }

    @Override
    public TokenInfo adminLogin(MemberLoginRequest loginRequest) {
        MemberDto member = memberAdapter.getMember(loginRequest.getLoginId());

        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getMemberAccount().getPassword())) {
            throw new IllegalArgumentException(); // 401
        }

        if (!hasAdminRole(member.getRoles())) {
            throw new IllegalArgumentException(); // 403
        }

        return jwtProvider.generateToken(member);
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
