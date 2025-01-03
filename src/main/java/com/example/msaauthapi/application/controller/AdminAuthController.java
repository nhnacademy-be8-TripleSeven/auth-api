package com.example.msaauthapi.application.controller;

import com.example.msaauthapi.application.service.AuthService;
import com.example.msaauthapi.common.jwt.TokenInfo;
import com.example.msaauthapi.common.utils.CookieUtil;
import com.example.msaauthapi.dto.request.MemberLoginRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final CookieUtil cookieUtil;
    private final AuthService authService;

    @PostMapping("/admin/login")
    public TokenInfo adminLogin(@Valid @RequestBody MemberLoginRequest memberLoginRequest, HttpServletResponse response) {
        TokenInfo tokenInfo = authService.adminLogin(memberLoginRequest);
        cookieUtil.setAuthCookies(tokenInfo, response);
        return tokenInfo;
    }
}
