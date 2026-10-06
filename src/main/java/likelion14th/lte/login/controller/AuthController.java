package likelion14th.lte.login.controller;

import org.springframework.security.oauth2.jwt.Jwt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion14th.lte.global.api.ApiResponse;
import likelion14th.lte.global.api.SuccessCode;
import likelion14th.lte.login.dto.response.AuthResponse;
import likelion14th.lte.login.service.AuthService;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import likelion14th.lte.login.dto.request.KakaoRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Tag(name = "Auth", description = "카카오 인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${auth.cookie.secure}")
    private boolean cookieSecure;

    @Value("${auth.cookie.same-site}")
    private String cookieSameSite;

    @Value("${jwt.refresh-exp-ms}")
    private long refreshExpMs;

    @PostMapping("/kakao")
    @Operation(summary = "카카오 로그인", description = "kakao 사용자 정보 확인")
    public ApiResponse<AuthResponse> kakaologin(
        @Valid @RequestBody KakaoRequest request,
        HttpServletResponse httpResponse
    ) {
        AuthResponse response = authService.handleLoginCode(request.getCode());

        httpResponse.addHeader(
            HttpHeaders.SET_COOKIE,
            createRefreshTokenCookie(response.getRefreshToken()).toString()
        );

        return ApiResponse.onSuccess(SuccessCode.USER_LOGIN_SUCCESS, response);
    }

    @PostMapping("/reissue")
    @Operation(summary = "Access Token 재발급", description = "accessToken 재발급")
    public ApiResponse<String> reissue(
        @Parameter(hidden = true)
        @CookieValue(value = "refresh_token", required = false) String refreshToken
    ) {
        String newAccessToken = authService.reissuedAccessToken(refreshToken);

        return ApiResponse.onSuccess(SuccessCode.USER_REISSUE_SUCCESS, newAccessToken);

    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그아웃 합니다.")
    public ApiResponse<Void> logout(
        @AuthenticationPrincipal Jwt jwt,
        HttpServletResponse httpResponse
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        authService.logout(userId);

        httpResponse.addHeader(
            HttpHeaders.SET_COOKIE,
            deleteRefreshTokenCookie().toString()
        );

        return ApiResponse.onSuccess(SuccessCode.USER_LOGOUT_SUCCESS, null);
    }

    @DeleteMapping("/withdraw")
    @Operation(summary = "회원탈퇴", description = "회원탈퇴 합니다.")
    public ApiResponse<Void> withdraw(
        @AuthenticationPrincipal Jwt jwt,
        HttpServletResponse httpResponse
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        authService.withdraw(userId);
        httpResponse.addHeader(
            HttpHeaders.SET_COOKIE,
            deleteRefreshTokenCookie().toString()
        );
        return ApiResponse.onSuccess(SuccessCode.USER_DELETE_SUCCESS, null);
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .maxAge(Duration.ofMillis(refreshExpMs))
                .path("/")
                .build();
    }

    private ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .maxAge(Duration.ZERO)
                .path("/")
                .build();
    }

}
