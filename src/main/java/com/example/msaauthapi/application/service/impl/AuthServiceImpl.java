package com.example.msaauthapi.application.service.impl;

import com.example.msaauthapi.adaptor.MemberAdapter;
import com.example.msaauthapi.application.error.CustomException;
import com.example.msaauthapi.application.error.ErrorCode;
import com.example.msaauthapi.application.service.AuthService;
import com.example.msaauthapi.common.jwt.JwtProvider;
import com.example.msaauthapi.common.jwt.TokenInfo;
import com.example.msaauthapi.dto.MemberDto;
import com.example.msaauthapi.dto.request.MemberLoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${member.auth.key}")
    private String memberAuthKey;

    private final MemberAdapter memberAdapter;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    public TokenInfo login(MemberLoginRequest loginRequest) {
        MemberDto member = memberAdapter.getMember(loginRequest.getLoginId(), memberAuthKey);

        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getMemberAccount().getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCHED);
        }

        if (hasRole(member.getRoles(), "INACTIVE")) {
            throw new CustomException(ErrorCode.INACTIVE_ACCOUNT);
        }

        if (hasRole(member.getRoles(), "QUIT")) {
            throw new CustomException(ErrorCode.QUIT_ACCOUNT);
        }

        memberAdapter.updateLastLoggedInAt(member.getId());
        return jwtProvider.generateToken(member);
    }

    @Override
    public TokenInfo reIssueJwt(String refreshToken) {
        return jwtProvider.reissueToken(refreshToken);
    }

    @Override
    public TokenInfo adminLogin(MemberLoginRequest loginRequest) {
        MemberDto member = memberAdapter.getMember(loginRequest.getLoginId(), memberAuthKey
        );

        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getMemberAccount().getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCHED);
        }

        if (!hasRole(member.getRoles(), "ADMIN")) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return jwtProvider.generateToken(member);
    }

    @Override
    public void logout(Long userId) {
        jwtProvider.deleteRefreshToken(userId);
    }

    @Override
    public TokenInfo paycoLogin(String clientId) {
        MemberDto member = memberAdapter.getMember(clientId, memberAuthKey);
        return jwtProvider.generateToken(member);
    }

    private boolean hasRole(List<String> roles, String roleValue) {

        for (String role : roles) {
            if (role.contains(roleValue)) {
                return true;
            }
        }

        return false;
    }
}
