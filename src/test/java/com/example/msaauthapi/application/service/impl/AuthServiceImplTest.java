package com.example.msaauthapi.application.service.impl;

import com.example.msaauthapi.adaptor.MemberAdapter;
import com.example.msaauthapi.application.error.CustomException;
import com.example.msaauthapi.application.error.ErrorCode;
import com.example.msaauthapi.common.jwt.JwtProvider;
import com.example.msaauthapi.common.jwt.TokenInfo;
import com.example.msaauthapi.dto.MemberDto;
import com.example.msaauthapi.dto.request.MemberLoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private MemberAdapter memberAdapter;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthServiceImpl(memberAdapter, passwordEncoder, jwtProvider);
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // Given
        MemberLoginRequest request = new MemberLoginRequest("testUser", "password");
        MemberDto memberDto = new MemberDto(1L, new MemberDto.MemberAccount("testUser", "password"), Collections.singletonList("USER"));

        TokenInfo tokenInfo = new TokenInfo("Bearer", "accessToken", "refreshToken");

        when(memberAdapter.getMember(anyString(), any())).thenReturn(memberDto);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtProvider.generateToken(memberDto)).thenReturn(tokenInfo);

        // When
        TokenInfo result = authService.login(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("accessToken");
        assertThat(result.getRefreshToken()).isEqualTo("refreshToken");

        verify(passwordEncoder).matches(request.getPassword(), memberDto.getMemberAccount().getPassword());
        verify(jwtProvider).generateToken(memberDto);
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_Fail_PasswordMismatch() {
        // Given
        MemberLoginRequest request = new MemberLoginRequest("testUser", "password");
        MemberDto memberDto = new MemberDto(1L, new MemberDto.MemberAccount("testUser", "password"), Collections.singletonList("USER"));

        when(memberAdapter.getMember(anyString(), any())).thenReturn(memberDto);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class);

    }

    @Test
    @DisplayName("로그인 실패 - 비활성 계정")
    void login_Fail_InactiveAccount() {
        // Given
        MemberLoginRequest request = new MemberLoginRequest("testUser", "password");
        MemberDto memberDto = new MemberDto(1L, new MemberDto.MemberAccount("testUser", "password"), Collections.singletonList("INACTIVE"));


        when(memberAdapter.getMember(anyString(), any())).thenReturn(memberDto);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class);

        verify(passwordEncoder).matches(request.getPassword(), memberDto.getMemberAccount().getPassword());
        verifyNoInteractions(jwtProvider);
    }

    @Test
    @DisplayName("관리자 로그인 성공")
    void adminLogin_Success() {
        // Given
        MemberLoginRequest request = new MemberLoginRequest("adminUser", "password");
        MemberDto memberDto = new MemberDto(1L, new MemberDto.MemberAccount("testUser", "password"), Collections.singletonList("ADMIN"));


        TokenInfo tokenInfo = new TokenInfo("Bearer", "adminAccessToken", "adminRefreshToken");

        when(memberAdapter.getMember(anyString(), any())).thenReturn(memberDto);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtProvider.generateToken(memberDto)).thenReturn(tokenInfo);

        // When
        TokenInfo result = authService.adminLogin(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("adminAccessToken");
        assertThat(result.getRefreshToken()).isEqualTo("adminRefreshToken");

        verify(passwordEncoder).matches(request.getPassword(), memberDto.getMemberAccount().getPassword());
        verify(jwtProvider).generateToken(memberDto);
    }

    @Test
    @DisplayName("관리자 로그인 실패 - 관리자 권한 없음")
    void adminLogin_Fail_NotAdmin() {
        // Given
        MemberLoginRequest request = new MemberLoginRequest("testUser", "password");
        MemberDto memberDto = new MemberDto(1L, new MemberDto.MemberAccount("testUser", "password"), Collections.singletonList("USER"));

        when(memberAdapter.getMember(anyString(), any())).thenReturn(memberDto);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.adminLogin(request))
                .isInstanceOf(CustomException.class);

    }
}