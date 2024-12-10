package com.example.msaauthapi.application.service;

import com.example.msaauthapi.common.jwt.TokenInfo;
import com.example.msaauthapi.dto.MemberDto;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    TokenInfo login(MemberDto memberDto, HttpServletResponse response);
}
