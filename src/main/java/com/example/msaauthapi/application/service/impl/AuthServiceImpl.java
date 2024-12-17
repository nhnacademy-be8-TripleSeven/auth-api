package com.example.msaauthapi.application.service.impl;

import com.example.msaauthapi.adaptor.MemberAdapter;
import com.example.msaauthapi.application.service.AuthService;
import com.example.msaauthapi.common.jwt.JwtProvider;
import com.example.msaauthapi.common.jwt.TokenInfo;
import com.example.msaauthapi.dto.MemberDto;
import com.example.msaauthapi.dto.request.MemberLoginRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


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
}
