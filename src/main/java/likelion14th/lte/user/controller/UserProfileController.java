package likelion14th.lte.user.controller;


import io.swagger.v3.oas.annotations.Operation;
import likelion14th.lte.global.api.ApiResponse;
import likelion14th.lte.global.api.SuccessCode;
import likelion14th.lte.user.dto.request.CreateTestUserRequest;
import likelion14th.lte.user.dto.response.UserProfileResponse;
import likelion14th.lte.user.repository.UserRepository;
import likelion14th.lte.user.service.UserProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class    UserProfileController{

    public final UserProfileService userProfileService;

    // [Q9. Controller 내부에서 userRepository.findById()를 직접 호출해서 유저를 찾지 않고,
    // 반드시 userProfileService를 호출하여 작업을 위임해야 하는 이유는 무엇인가요? (단일 책임 원칙 관점)]
    /** 답변:
     * Service에 작업을 위임하면 Controller는 API I/O만 담당하고 실제 로직은 Service에서
     * 처리가 이루어지기 때문에 계층별 역할이 명확해져 유지보수가 용이하다.
     */

    @GetMapping
    public ApiResponse<UserProfileResponse> getUserProfile(@RequestParam Long userId){
        UserProfileResponse response = userProfileService.getUserProfile(userId);
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
}