package com.example.msaauthapi.application.controller;

import com.example.msaauthapi.application.service.AuthService;
import com.example.msaauthapi.common.jwt.TokenInfo;
import com.example.msaauthapi.dto.MemberDto;
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
    public TokenInfo login(@RequestBody MemberDto memberDto, HttpServletResponse response) {
        return authService.login(memberDto, response);
    }

    @GetMapping("/re-issue")
    public TokenInfo reIssueAccessToken(HttpServletRequest request, HttpServletResponse response) {
        return authService.reIssueJwt(request, response);
    }

}
