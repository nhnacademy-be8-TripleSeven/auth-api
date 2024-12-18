package com.example.msaauthapi.application.controller;

import com.example.msaauthapi.application.service.AuthService;
import com.example.msaauthapi.common.jwt.TokenInfo;
import com.example.msaauthapi.dto.MemberDto;
import com.example.msaauthapi.dto.request.MemberLoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public TokenInfo login(@RequestBody MemberLoginRequest memberLoginRequest) {
        return authService.login(memberLoginRequest);
    }

    @GetMapping("/re-issue")
    public TokenInfo reIssueAccessToken(@CookieValue("refresh-token") String refreshToken) {
        return authService.reIssueJwt(refreshToken);
    }

}
