package likelion14th.lte.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import likelion14th.lte.global.api.ApiResponse;
import likelion14th.lte.global.api.SuccessCode;
import likelion14th.lte.user.dto.request.FcmTokenRequest;
import likelion14th.lte.user.service.UserFcmTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserFcmTokenController {

    private final UserFcmTokenService userFcmTokenService;

    @PostMapping("/me/fcm-token")
    @Operation(summary = "FCM 토큰 저장", description = "현재 사용자에게 발급된 FCM 디바이스 토큰을 저장합니다.")
    public ApiResponse<String> updateMyFcmToken(
            @RequestParam Long userId,
            @Valid @RequestBody FcmTokenRequest request
    ) {
        userFcmTokenService.updateFcmToken(userId, request.getToken());
        return ApiResponse.onSuccess(SuccessCode.FCM_TOKEN_UPDATE_SUCCESS, "OK");
    }
}
