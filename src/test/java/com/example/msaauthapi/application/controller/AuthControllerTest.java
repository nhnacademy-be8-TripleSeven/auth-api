package com.example.msaauthapi.application.controller;

import com.example.msaauthapi.application.service.AuthService;
import com.example.msaauthapi.common.jwt.TokenInfo;
import com.example.msaauthapi.common.utils.CookieUtil;
import com.example.msaauthapi.dto.request.MemberLoginRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CookieUtil cookieUtil;

    @Test
    @DisplayName("로그인 성공 테스트")
    void login_Success() throws Exception {
        // Given
        MemberLoginRequest request = new MemberLoginRequest("testUser", "password");
        TokenInfo tokenInfo = new TokenInfo("Bearer", "accessToken", "refreshToken");

        when(authService.login(any(MemberLoginRequest.class))).thenReturn(tokenInfo);
        Mockito.doNothing().when(cookieUtil).setAuthCookies(eq(tokenInfo), any());

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "loginId": "testUser",
                          "password": "password"
                        }
                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"));
    }

    @Test
    @DisplayName("로그아웃 성공 테스트")
    void logout_Success() throws Exception {
        // Given
        Long userId = 1L;
        doNothing().when(authService).logout(any());
        when(cookieUtil.removeAccessTokenCookie()).thenReturn(new Cookie("accessToken", ""));
        when(cookieUtil.removeRefreshTokenCookie()).thenReturn(new Cookie("refreshToken", ""));

        // When & Then
        mockMvc.perform(post("/auth/logout")
                        .header("X-USER", "1")
                )
                .andExpect(status().isOk())
                .andExpect(cookie().value("accessToken", ""))
                .andExpect(cookie().value("refreshToken", ""));
    }

    @Test
    @DisplayName("Payco 로그인 성공 테스트")
    void paycoLogin_Success() throws Exception {
        // Given
        String clientId = "clientId123";
        TokenInfo tokenInfo = new TokenInfo("Bearer", "accessToken", "refreshToken");

        when(authService.paycoLogin(eq(clientId))).thenReturn(tokenInfo);
        Mockito.doNothing().when(cookieUtil).setAuthCookies(eq(tokenInfo), any());

        // When & Then
        mockMvc.perform(post("/auth/payco/login")
                        .param("clientId", clientId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"));
    }

    @Test
    @DisplayName("Access Token 재발급 성공 테스트")
    void reIssueAccessToken_Success() throws Exception {
        // Given
        String refreshToken = "refreshToken123";
        TokenInfo tokenInfo = new TokenInfo("Bearer", "newAccessToken", "refreshToken");

        when(authService.reIssueJwt(eq(refreshToken))).thenReturn(tokenInfo);
        Mockito.doNothing().when(cookieUtil).setAuthCookies(eq(tokenInfo), any());

        // When & Then
        mockMvc.perform(get("/auth/refresh/token")
                        .header("refresh-token", refreshToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccessToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"));
    }
}
