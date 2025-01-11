package com.example.msaauthapi.application.controller;

import com.example.msaauthapi.application.error.CustomException;
import com.example.msaauthapi.application.error.ErrorCode;
import com.example.msaauthapi.application.service.AuthService;
import com.example.msaauthapi.common.jwt.TokenInfo;
import com.example.msaauthapi.common.utils.CookieUtil;
import com.example.msaauthapi.dto.request.MemberLoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AdminAuthController.class)
class AdminAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CookieUtil cookieUtil;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("관리자 계정으로 로그인 성공 테스트")
    void adminLogin_Success() throws Exception {
        // Given
        MemberLoginRequest loginRequest = new MemberLoginRequest("admin", "password");
        TokenInfo tokenInfo = new TokenInfo("Bearer", "access-token", "refresh-token");

        Mockito.when(authService.adminLogin(any(MemberLoginRequest.class))).thenReturn(tokenInfo);

        // When & Then
        mockMvc.perform(post("/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("관리자 계정이 아닌 경우 403 에러")
    void adminLogin_NonAdminAccount() throws Exception {
        // Given
        MemberLoginRequest loginRequest = new MemberLoginRequest("user", "password");

        Mockito.when(authService.adminLogin(any(MemberLoginRequest.class)))
                .thenThrow(new CustomException(ErrorCode.FORBIDDEN));

        // When & Then
        mockMvc.perform(post("/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden());
    }
}