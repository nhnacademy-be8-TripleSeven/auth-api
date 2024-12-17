package com.example.msaauthapi.common.jwt;

import com.example.msaauthapi.adaptor.MemberAdapter;
import com.example.msaauthapi.dto.MemberDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@Slf4j
public class JwtProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String JWT_KEY_PREFIX = "jwt:";
    private final Key key;
    private final RedisTemplate<String, String> redisTemplate;
    private final MemberAdapter memberAdapter;
    private final int accessExpirationTime;
    private final int refreshExpirationTime;

    public JwtProvider(@Value("${jwt.secret}") String secretKey,
                       @Value("${jwt.access-expiration-time}") int accessExpirationTime,
                       @Value("${jwt.refresh-expiration-time}") int refreshExpirationTime,
                       RedisTemplate<String, String> redisTemplate,
                       MemberAdapter memberAdapter) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessExpirationTime = accessExpirationTime;
        this.refreshExpirationTime = refreshExpirationTime;
        this.redisTemplate = redisTemplate;
        this.memberAdapter = memberAdapter;
    }

    public TokenInfo generateToken(MemberDto memberDto, HttpServletResponse response) {
        String accessToken = generateAccessToken(memberDto);
        String refreshToken = generateRefreshToken(memberDto);
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setMaxAge(refreshExpirationTime);
        cookie.setPath("/");
        //cookie.setSecure(true); //로컬환경에서는 Secure 설정을 꺼놔야 함. secure 켜지면 https 환경에서만 쿠키가 전달됨.
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        redisTemplate.opsForValue().set(
                JWT_KEY_PREFIX + memberDto.getId(),
                refreshToken,
                refreshExpirationTime
        );

        return TokenInfo.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken("httpOnly")
                .build();
    }

    public TokenInfo reissueToken(String reqRefreshToken, HttpServletResponse response) throws RuntimeException{
        Claims claims = parseClaims(reqRefreshToken);
        String refreshToken = redisTemplate.opsForValue().get(JWT_KEY_PREFIX + claims.getSubject()).toString();

        // refresh토큰이 불일치 시 401에러
        if(!refreshToken.equals(reqRefreshToken)){
            throw new RuntimeException();
        }

        String memberId = parseClaims(refreshToken).getSubject();
        MemberDto member = memberAdapter.getMember(memberId);
        return generateToken(member, response);
    }

    private String generateAccessToken(MemberDto memberDto) {
        String authorities = "ROLE_MEMBER"; // 권한 추가되면 변경하기. 현재는 1개 역할 고정
        Date now = new Date();
        Date accessTokenExpiresIn = new Date(now.getTime() + accessExpirationTime);
        return Jwts.builder()
                .setSubject(memberDto.getId().toString())
                .claim(AUTHORITIES_KEY, authorities)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateRefreshToken(MemberDto memberDto) {
        Claims claims = Jwts.claims().setSubject(memberDto.getId().toString());
        Date now = new Date();
        Date refreshTokenExpiresIn = new Date(now.getTime() + refreshExpirationTime);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(refreshTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            log.info(e.getMessage());
            return e.getClaims();
        }
    }
}
