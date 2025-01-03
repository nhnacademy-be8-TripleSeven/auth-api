package com.example.msaauthapi.common.utils;

import com.example.msaauthapi.common.jwt.TokenInfo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    private static final int REFRESH_TOKEN_MAX_AGE = 3 * 24 * 60 * 60; // 3일
    private static final int ACCESS_TOKEN_MAX_AGE = 60 * 60; // 1시간

    @Value("${spring.profiles.active}")
    private String profile;

    public void setAuthCookies(TokenInfo tokenInfo, HttpServletResponse response) {
        Cookie accessTokenCookie = getAccessTokenHttpSecureCookie(tokenInfo.getAccessToken());
        Cookie refreshCookie = getRefreshTokenHttpSecureCookie(tokenInfo.getRefreshToken());
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshCookie);
    }

    // Refresh Token 쿠키 생성
    public Cookie getRefreshTokenHttpSecureCookie(String refreshToken) {
        return createHttpSecureCookie("refresh-token", refreshToken, REFRESH_TOKEN_MAX_AGE);
    }

    // Access Token 쿠키 생성
    public Cookie getAccessTokenHttpSecureCookie(String accessToken) {
        return createHttpSecureCookie("jwt_token", accessToken, ACCESS_TOKEN_MAX_AGE);
    }

    // Refresh Token 쿠키 삭제
    public Cookie removeRefreshTokenCookie() {
        return removeCookie("refresh-token");
    }

    // Access Token 쿠키 삭제
    public Cookie removeAccessTokenCookie() {
        return removeCookie("jwt_token");
    }

    // 쿠키 생성 시 중복 로직을 처리하는 공통 메서드
    private Cookie createHttpSecureCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);   // 클라이언트에서 JavaScript로 쿠키 접근 불가
        cookie.setSecure(!"dev".equals(profile)); // 'dev' 프로파일이 아닌 경우 Secure 활성화
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    // 쿠키 삭제 메서드 (공통화)
    private Cookie removeCookie(String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);  // 만료된 쿠키로 설정
        return cookie;
    }

}
