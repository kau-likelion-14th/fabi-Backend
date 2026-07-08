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

import java.util.List;

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

    @DeleteMapping
    @Operation(summary = "언팔로우", description = "팔로우를 취소합니다.")
    public ApiResponse<Void> deleteFollow(
            @RequestParam Long userId,
            @RequestBody FollowUserRequest followUserRequest
    ){
        followService.unfollowUser(userId, followUserRequest.getToUserId());

        return ApiResponse.onSuccess(SuccessCode.FOLLOW_DELETE_SUCCESS, null);
    }

    @GetMapping("/followers")
    @Operation(summary = "팔로워 목록 조회", description = "나를 팔로우하는 유저 목록을 조회합니다.")
    public ApiResponse<List<FollowUserResponse>> getFollowers(
            @RequestParam Long userId
    ){
        List<FollowUserResponse> responses = followService.getFollowers(userId);

        return ApiResponse.onSuccess(SuccessCode.FOLLOW_LIST_GET_SUCCESS, responses);
    }

    @GetMapping("/followings")
    @Operation(summary = "팔로잉 목록 조회", description = "내가 팔로우하는 유저 목록을 조회합니다.")
    public ApiResponse<List<FollowUserResponse>> getFollowings(
            @RequestParam Long userId
    ){
        List<FollowUserResponse> responses = followService.getFollowings(userId);

        return ApiResponse.onSuccess(SuccessCode.FOLLOW_LIST_GET_SUCCESS, responses);
    }

    @GetMapping
    @Operation(summary = "팔로우 가능 유저 목록 조회", description = "팔로우 가능한 유저 목록을 페이징하여 조회합니다.")
    public ApiResponse<Page<FollowUserResponse>> getCanFollowUsers(
            @RequestParam Long userId,
            @ParameterObject @PageableDefault(size = 10, page = 0) Pageable pageable
    ){
        Page<FollowUserResponse> responses = followService.getCanFollowUsers(userId, pageable);

        return ApiResponse.onSuccess(SuccessCode.FOLLOW_SEARCH_SUCCESS, responses);
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
