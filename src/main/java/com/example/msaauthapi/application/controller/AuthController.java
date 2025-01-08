package com.example.msaauthapi.application.controller;

import com.example.msaauthapi.application.service.AuthService;
import com.example.msaauthapi.common.jwt.TokenInfo;
import com.example.msaauthapi.common.utils.CookieUtil;
import com.example.msaauthapi.dto.MemberDto;
import com.example.msaauthapi.dto.request.MemberLoginRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CookieUtil cookieUtil;
    private final AuthService authService;

    @PostMapping("/login")
    public TokenInfo login(@Valid @RequestBody MemberLoginRequest memberLoginRequest, HttpServletResponse response) {
        TokenInfo tokenInfo = authService.login(memberLoginRequest);
        cookieUtil.setAuthCookies(tokenInfo, response);
        return tokenInfo;
    }

    @PostMapping("/payco/login")
    public TokenInfo paycoLogin(@RequestParam String clientId, HttpServletResponse response) {
        TokenInfo tokenInfo = authService.paycoLogin(clientId);
        cookieUtil.setAuthCookies(tokenInfo, response);
        return tokenInfo;
    }

    @GetMapping("/refresh/token")
    public TokenInfo reIssueAccessToken(@RequestHeader("refresh-token") String refreshToken, HttpServletResponse response) {
        TokenInfo tokenInfo = authService.reIssueJwt(refreshToken);
        cookieUtil.setAuthCookies(tokenInfo, response);
        return tokenInfo;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("X-USER") Long userId, HttpServletResponse response) {
        authService.logout(userId);
        Cookie accessTokenCookie = cookieUtil.removeAccessTokenCookie();
        Cookie refreshTokenCookie = cookieUtil.removeRefreshTokenCookie();
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
        return ResponseEntity.ok().build();
    }

}
