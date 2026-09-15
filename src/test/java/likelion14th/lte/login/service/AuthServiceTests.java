package likelion14th.lte.login.service;

import java.util.Optional;
import likelion14th.lte.todo.repository.TodoRepository;
import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.config.JwtConfig;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.login.client.KakaoClient;
import likelion14th.lte.login.entity.RefreshToken;
import likelion14th.lte.login.repository.RefreshTokenRepository;
import likelion14th.lte.user.entity.User;
import likelion14th.lte.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTests {
    private final KakaoClient kakao = mock(KakaoClient.class);
    private final UserRepository users = mock(UserRepository.class);
    private final RefreshTokenRepository tokens = mock(RefreshTokenRepository.class);
    private final TodoRepository todos = mock(TodoRepository.class);
    private AuthService service;
    private JwtDecoder accessDecoder;
    private JwtDecoder refreshDecoder;
    private User user;

    @BeforeEach
    void setUp() {
        JwtConfig config = new JwtConfig();
        var key = config.jwtSecretKey("test-secret-with-at-least-32-bytes-long");
        accessDecoder = config.accessTokenDecoder(key);
        refreshDecoder = config.refreshTokenDecoder(key);
        service = new AuthService(kakao, users, tokens, todos, new likelion14th.lte.login.jwt.JwtProvider(config.jwtEncoder(key), refreshDecoder,
                3600000, 1209600000));
        user = User.builder().username("tester").userTag("1234").providerId("123").build();
        ReflectionTestUtils.setField(user, "id", 1L);
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(tokens.findByUser(user)).thenReturn(Optional.empty());
    }

    @Test
    void issuesAndReissuesCompatibleTokens() {
        var response = service.issueToken(user);
        assertEquals("1", accessDecoder.decode(response.getAccessToken()).getSubject());
        var refresh = refreshDecoder.decode(response.getRefreshToken());
        var captor = org.mockito.ArgumentCaptor.forClass(RefreshToken.class);
        verify(tokens).save(captor.capture());
        var saved = captor.getValue();
        assertTrue(Math.abs(saved.getRefreshTokenExpiration() - refresh.getExpiresAt().toEpochMilli()) < 1000);
        when(tokens.findByUser(user)).thenReturn(Optional.of(saved));
        assertEquals("ACCESS", accessDecoder.decode(service.reissuedAccessToken(response.getRefreshToken()))
                .getClaimAsString("type"));
    }

    @Test
    void rejectsAccessTokenForReissue() {
        var response = service.issueToken(user);
        assertEquals(ErrorCode.TOKEN_INVALID, assertThrows(GeneralException.class,
                () -> service.reissuedAccessToken(response.getAccessToken())).getCode());
    }

    @Test
    void rejectsReplacedRefreshToken() {
        var response = service.issueToken(user);
        when(tokens.findByUser(user)).thenReturn(Optional.of(RefreshToken.builder().user(user)
                .refreshToken("replacement").refreshTokenExpiration(Long.MAX_VALUE).build()));
        assertEquals(ErrorCode.TOKEN_INVALID, assertThrows(GeneralException.class,
                () -> service.reissuedAccessToken(response.getRefreshToken())).getCode());
    }

    @Test
    void existingKakaoUserIsNotCreatedAgain() {
        when(kakao.getAccessToken("code")).thenReturn("kakao-token");
        when(kakao.getUserInfo("kakao-token")).thenReturn(JsonMapper.builder().build().readTree("{\"id\":123}"));
        when(users.findByProviderId("123")).thenReturn(Optional.of(user));
        assertEquals("tester", service.handleLoginCode("code").getUsername());
        verify(users, never()).save(any());
    }

    @Test
    void createsUserWithFallbackNickname() {
        when(kakao.getAccessToken("code")).thenReturn("kakao-token");
        when(kakao.getUserInfo("kakao-token")).thenReturn(JsonMapper.builder().build().readTree("{\"id\":456}"));
        when(users.findByProviderId("456")).thenReturn(Optional.empty());
        when(users.save(any(User.class))).thenAnswer(invocation -> {
            User created = invocation.getArgument(0);
            ReflectionTestUtils.setField(created, "id", 2L);
            return created;
        });
        var response = service.handleLoginCode("code");
        assertEquals("카카오 유저", response.getUsername());
        assertTrue(response.getUserTag().matches("KAKAO[0-9A-F]{8}"));
        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(users).save(captor.capture());
        assertEquals("456", captor.getValue().getProviderId());
    }

    @Test
    void logoutDeletesStoredRefreshToken() {
        var saved = RefreshToken.builder().user(user).refreshToken("token")
                .refreshTokenExpiration(Long.MAX_VALUE).build();
        when(tokens.findByUser(user)).thenReturn(Optional.of(saved));
        service.logout(1L);
        verify(tokens).delete(saved);
        verify(users, never()).delete(any());
    }

    @Test
    void logoutWithoutRefreshTokenSucceeds() {
        service.logout(1L);
        verify(tokens, never()).delete(any());
    }

    @Test
    void withdrawalCleansDependentsBeforeDeletingUser() {
        var saved = RefreshToken.builder().user(user).refreshToken("token")
                .refreshTokenExpiration(Long.MAX_VALUE).build();
        when(tokens.findByUser(user)).thenReturn(Optional.of(saved));
        when(todos.findAllByUser(user)).thenReturn(java.util.List.of());
        service.withdraw(1L);
        var order = inOrder(tokens, todos, users);
        order.verify(tokens).delete(saved);
        order.verify(todos).deleteAll(java.util.List.of());
        order.verify(tokens).flush();
        order.verify(todos).flush();
        order.verify(users).delete(user);
    }
}
