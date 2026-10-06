package likelion14th.lte.user.controller;


import io.swagger.v3.oas.annotations.Operation;
import likelion14th.lte.global.api.ApiResponse;
import likelion14th.lte.global.api.SuccessCode;
import likelion14th.lte.user.dto.request.CreateTestUserRequest;
import likelion14th.lte.user.dto.request.UserIntroRequest;
import likelion14th.lte.user.dto.response.UserProfileResponse;
import likelion14th.lte.user.service.UserProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileController{

    public final UserProfileService userProfileService;

    // [Q9. Controller 내부에서 userRepository.findById()를 직접 호출해서 유저를 찾지 않고,
    // 반드시 userProfileService를 호출하여 작업을 위임해야 하는 이유는 무엇인가요? (단일 책임 원칙 관점)]
    /** 답변:
     * Service에 작업을 위임하면 Controller는 API I/O만 담당하고 실제 로직은 Service에서
     * 처리가 이루어지기 때문에 계층별 역할이 명확해져 유지보수가 용이하다.
     */

    @GetMapping
    public ApiResponse<UserProfileResponse> getUserProfile(
            @AuthenticationPrincipal Jwt jwt
    ){
        Long userId = Long.valueOf(jwt.getSubject());
        UserProfileResponse response = userProfileService.getUserProfile(userId);
        return ApiResponse.onSuccess(SuccessCode.USER_INFO_GET_SUCCESS, response);
    }

    @GetMapping("/touser")
    @Operation(summary = "다른 유저 프로필 조회", description = "toUserId에 해당하는 유저 프로필을 조회합니다.")
    public ApiResponse<UserProfileResponse> getToUserProfile(
            @RequestParam Long toUserId
    ){
        UserProfileResponse response = userProfileService.getUserProfile(toUserId);
        return ApiResponse.onSuccess(SuccessCode.USER_INFO_GET_SUCCESS, response);
    }

    @PostMapping
    public ApiResponse<UserProfileResponse> createTestUser(
            // [Q10. 클라이언트가 보낸 JSON 텍스트 데이터가 어떻게 자바 객체인 CreateTestUserRequest로
            // 변환 되는지앞의 어노테이션과 연관 지어 설명해 보세요.]
            /** 답변:
             * @RequestBody가 HTTP request body에 담긴 JSON을 자바 객체로 변환해준다
             * Spring이 내부적으로 JSON의 key랑 자바의 필ㄷ를 매핑한다.
             */
            @RequestBody CreateTestUserRequest request
    ){
        UserProfileResponse response = userProfileService.createTestUser(request);
        return ApiResponse.onSuccess(SuccessCode.CREATED, response);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "유저 프로필 추가 및 수정", description = "유저 프로필 이미지를 추가하거나 수정합니다.")
    public ApiResponse<UserProfileResponse> putUserProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("image") MultipartFile file
    ){
        Long userId = Long.valueOf(jwt.getSubject());

        UserProfileResponse response = userProfileService.updateProfileImage(userId, file);
        return ApiResponse.onSuccess(SuccessCode.PROFILE_PUT_SUCCESS, response);
    }

    @DeleteMapping
    @Operation(summary = "유저 프로필 이미지 삭제", description = "로그인한 유저의 프로필 이미지를 삭제합니다.")
    public ApiResponse<UserProfileResponse> deleteUserProfile(
            @AuthenticationPrincipal Jwt jwt
    ){
        Long userId = Long.valueOf(jwt.getSubject());

        UserProfileResponse response = userProfileService.deleteProfileImage(userId);
        return ApiResponse.onSuccess(SuccessCode.PROFILE_DELETE_SUCCESS, response);
    }

    @PutMapping("/intro")
    @Operation(summary = "유저 한줄 소개 수정", description = "로그인한 유저의 한줄 소개를 수정합니다.")
    public ApiResponse<UserProfileResponse> putUserIntroduction(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UserIntroRequest request
    ){
        Long userId = Long.valueOf(jwt.getSubject());

        UserProfileResponse response = userProfileService.updateIntroduction(userId, request);
        return ApiResponse.onSuccess(SuccessCode.USER_PROFILE_UPDATE_SUCCESS, response);
    }
}
