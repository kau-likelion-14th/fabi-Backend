package likelion14th.lte.login.jwt;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final long accessExpMs;
    private final long refreshExpMs;

    public JwtProvider(JwtEncoder jwtEncoder,
                       @Qualifier("refreshTokenDecoder") JwtDecoder jwtDecoder,
                       @Value("${jwt.access-exp-ms}") long accessExpMs,
                       @Value("${jwt.refresh-exp-ms}") long refreshExpMs
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.accessExpMs = accessExpMs;
        this.refreshExpMs = refreshExpMs;
    }

    public String createAccessToken(Long userId) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiresAt(now.plusMillis(accessExpMs))
                .id(UUID.randomUUID().toString())
                .claim("type", "ACCESS")
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public String createRefreshToken(Long userId) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiresAt(now.plusMillis(refreshExpMs))
                .id(UUID.randomUUID().toString())
                .claim("type", "REFRESH")
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public Long validateRefreshToken(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        if (!"REFRESH".equals(jwt.getClaimAsString("type")) || jwt.getExpiresAt() == null
                || !jwt.getExpiresAt().isAfter(Instant.now())) {
            throw new JwtException("유효한 리프레시 토큰이 아닙니다.");
        }
        return Long.parseLong(jwt.getSubject());
    }

    public long getRefreshTokenExpiration(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        if (jwt.getExpiresAt() == null) {
            throw new JwtException("토큰 만료 시각이 없습니다.");
        }
        return jwt.getExpiresAt().toEpochMilli();
    }
}
