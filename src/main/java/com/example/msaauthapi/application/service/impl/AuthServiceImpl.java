package com.example.msaauthapi.application.service.impl;

import com.example.msaauthapi.adaptor.MemberAdapter;
import com.example.msaauthapi.application.service.AuthService;
import com.example.msaauthapi.common.jwt.JwtProvider;
import com.example.msaauthapi.common.jwt.TokenInfo;
import com.example.msaauthapi.dto.MemberDto;
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
    public TokenInfo login(MemberDto memberDto, HttpServletResponse response) {
        MemberDto member = memberAdapter.getMember(memberDto.getId());
        if (!passwordEncoder.matches(memberDto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException();
        }
        return jwtProvider.generateToken(memberDto, response);
    }
}
