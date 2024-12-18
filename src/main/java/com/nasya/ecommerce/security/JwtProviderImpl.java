package com.nasya.ecommerce.security;

import com.nasya.ecommerce.common.DateUtil;
import com.nasya.ecommerce.config.JwtSecretConfig;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtProviderImpl implements JwtProvider {

    private final JwtSecretConfig jwtSecretConfig;
    private final SecretKey signKey;

    @Override
    public String generateToken(UserInfo userInfo) {
        LocalDateTime expirationTime = LocalDateTime.now().plus(jwtSecretConfig.getJwtExpirationTime());
        Date expirationDate = DateUtil.convertLocalDateTimeToDate(expirationTime);

        return Jwts.builder()
                .subject(userInfo.getUsername())
                .issuedAt(new Date())
                .expiration(expirationDate)
                .signWith(signKey)
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
            return Jwts.parser()
                    .verifyWith(signKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration().before(Date.from(Instant.now()));
    }

    @Override
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(signKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
