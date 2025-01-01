package com.example.msaauthapi.common.jwt;

import com.example.msaauthapi.adaptor.MemberAdapter;
import com.example.msaauthapi.application.error.CustomException;
import com.example.msaauthapi.application.error.ErrorCode;
import com.example.msaauthapi.dto.MemberDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
    public TokenInfo generateToken(MemberDto memberDto) {
        String accessToken = generateAccessToken(memberDto);
        String refreshToken = generateRefreshToken(memberDto);
        redisTemplate.opsForValue().set(
                JWT_KEY_PREFIX + memberDto.getId(),
                refreshToken,
                refreshExpirationTime,
                TimeUnit.MILLISECONDS
        );


        return TokenInfo.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken("httpOnly")
                .build();
    }

    public TokenInfo reissueToken(String reqRefreshToken) {
        Claims claims = parseClaims(reqRefreshToken);
        String refreshToken = redisTemplate.opsForValue().get(JWT_KEY_PREFIX + claims.getSubject()).toString();

        // refresh토큰이 불일치 시 401에러
        if(!refreshToken.equals(reqRefreshToken)){
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String memberId = parseClaims(refreshToken).getSubject();
        MemberDto member = memberAdapter.getMember(memberId);
        return generateToken(member);
    }

    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(JWT_KEY_PREFIX + userId);
    }

    private String generateAccessToken(MemberDto memberDto) {
        Date now = new Date();
        Date accessTokenExpiresIn = new Date(now.getTime() + accessExpirationTime);
        return Jwts.builder()
                .setSubject(memberDto.getId().toString())
                .claim(AUTHORITIES_KEY, memberDto.getRoles())
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
