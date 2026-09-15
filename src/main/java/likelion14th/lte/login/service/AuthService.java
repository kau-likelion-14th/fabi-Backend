package likelion14th.lte.login.service;

import likelion14th.lte.login.jwt.JwtProvider;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.login.client.KakaoClient;
import likelion14th.lte.login.dto.response.AuthResponse;
import likelion14th.lte.login.entity.RefreshToken;
import likelion14th.lte.login.repository.RefreshTokenRepository;
import likelion14th.lte.todo.repository.TodoRepository;
import likelion14th.lte.user.entity.User;
import likelion14th.lte.user.repository.UserRepository;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final KakaoClient kakaoClient;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TodoRepository todoRepository;
    private final JwtProvider jwtProvider;

    public AuthResponse handleLoginCode(String code) {
        String kakaoAccessToken = kakaoClient.getAccessToken(code);
        JsonNode info = kakaoClient.getUserInfo(kakaoAccessToken);
        String nickname = info.path("kakao_account").path("profile").path("nickname").asText("");
        String username = StringUtils.hasText(nickname) ? nickname : "카카오 유저";
        String providerId = info.path("id").asText();
        User user = userRepository.findByProviderId(providerId)
                .orElseGet(() -> userRepository.save(User.builder().providerId(providerId)
                        .username(username).userTag(createUniqueUserTag()).build()));
        return issueToken(user);
    }

    public AuthResponse issueToken(User user) {
        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        saveOrUpdateRefreshToken(user, refreshToken, jwtProvider.getRefreshTokenExpiration(refreshToken));
        return AuthResponse.from(user, accessToken, refreshToken);
    }

    private void saveOrUpdateRefreshToken(User user, String token, long expiresAt) {
        refreshTokenRepository.findByUser(user).ifPresentOrElse(
                existing -> existing.updateToken(token, expiresAt),
                () -> refreshTokenRepository.save(RefreshToken.builder().user(user)
                        .refreshToken(token).refreshTokenExpiration(expiresAt).build()));
    }

    @Transactional(readOnly = true)
    public String reissuedAccessToken(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw GeneralException.of(ErrorCode.TOKEN_INVALID);
        }
        Long userId;
        try {
            userId = jwtProvider.validateRefreshToken(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw GeneralException.of(ErrorCode.TOKEN_INVALID);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> GeneralException.of(ErrorCode.TOKEN_INVALID));
        RefreshToken saved = refreshTokenRepository.findByUser(user)
                .orElseThrow(() -> GeneralException.of(ErrorCode.WRONG_REFRESH_TOKEN));
        if (!saved.getRefreshToken().equals(refreshToken)) {
            throw GeneralException.of(ErrorCode.TOKEN_INVALID);
        }
        if (saved.getRefreshTokenExpiration() <= System.currentTimeMillis()) {
            throw GeneralException.of(ErrorCode.TOKEN_EXPIRED);
        }
        return jwtProvider.createAccessToken(user.getId());
    }

    public void logout(Long userId) {
        User user = getUserOrThrow(userId);
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
    }

    public void withdraw(Long userId) {
        User user = getUserOrThrow(userId);
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
        todoRepository.deleteAll(todoRepository.findAllByUser(user));
        refreshTokenRepository.flush();
        todoRepository.flush();
        userRepository.delete(user);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> GeneralException.of(ErrorCode.USER_NOT_FOUND));
    }

    private String createUniqueUserTag() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String tag = "KAKAO" + UUID.randomUUID().toString().replace("-", "")
                    .substring(0, 8).toUpperCase(java.util.Locale.ROOT);
            if (!userRepository.existsByUserTag(tag)) {
                return tag;
            }
        }
        throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR);
    }

}
