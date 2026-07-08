package likelion14th.lte.follow.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion14th.lte.follow.dto.FollowUserRequest;
import likelion14th.lte.follow.dto.FollowUserResponse;
import likelion14th.lte.follow.service.FollowService;
import likelion14th.lte.global.api.ApiResponse;
import likelion14th.lte.global.api.SuccessCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("api/follow")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Tag(name = "팔로우 API", description = "팔로우 추가 및 삭제, 조회를 담당하는 api.")
public class FollowController {

    private final FollowService followService;

    @PostMapping
    @Operation(summary = "팔로우 추가", description = "json 요청이 유저아이디를 전달해줍니다")
    public ApiResponse<FollowUserResponse> addFollow(
            @RequestParam Long userId,
            @RequestBody FollowUserRequest followUserRequest

    ){
        FollowUserResponse response = followService.followUser(userId, followUserRequest.getToUserId());

        return ApiResponse.onSuccess(SuccessCode.FOLLOW_ADD_SUCCESS, response);
    }

    @GetMapping("/search")
    @Operation(summary = "팔로우 가능한 유저 검색", description = "닉네임으로 팔로우 가능한 유저를 검색합니다.")
    public ApiResponse<Page<FollowUserResponse>> getSearchFollows(
            @RequestParam Long userId,
            @RequestParam String nickname,
            @ParameterObject @PageableDefault(size = 10, page = 0) Pageable pageable
    ){
        Page<FollowUserResponse> responses = followService.searchCanFollowers(userId, nickname, pageable);
        return ApiResponse.onSuccess(SuccessCode.FOLLOW_SEARCH_SUCCESS, responses);
    }
}
