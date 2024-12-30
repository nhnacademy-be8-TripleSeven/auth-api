package com.example.msaauthapi.common.utils;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    @Value("${spring.profiles.active}")
    private String profile;

    // HttpOnly와 Secure 속성을 가진 쿠키 생성
    public Cookie setRefreshTokenHttpSecureCookie(String refreshToken) {
        Cookie cookie = new Cookie("refresh-token", refreshToken);
        cookie.setHttpOnly(true);   // 클라이언트에서 JavaScript로 쿠키 접근 불가

        // 로컬 환경에서는 Secure 속성 비활성화
        if ("dev".equals(profile)) {  // 'prod' 프로파일이 아닌 경우 (로컬 환경에서만 적용)
            cookie.setSecure(false);     // 로컬 개발 환경에서만 Secure 속성 비활성화
        } else {
            cookie.setSecure(true);     // 실제 배포 환경에서는 Secure 속성 활성화
        }
        cookie.setPath("/");
        cookie.setMaxAge(3 * 24 * 60 * 60);
        return cookie;
    }
}
