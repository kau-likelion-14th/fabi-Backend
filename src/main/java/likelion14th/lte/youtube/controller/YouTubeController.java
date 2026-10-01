package likelion14th.lte.youtube.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import likelion14th.lte.global.api.ApiResponse;
import likelion14th.lte.global.api.SuccessCode;
import likelion14th.lte.youtube.dto.request.SongSaveRequest;
import likelion14th.lte.youtube.dto.response.SavedSongResponse;
import likelion14th.lte.youtube.dto.response.YouTubeSongItemResponse;
import likelion14th.lte.youtube.service.YouTubeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/youtube")
public class YouTubeController {

    private final YouTubeService youTubeService;

    @GetMapping("/search")
    @Operation(summary = "유튜브 음악 검색",
            description = "YouTube Data API를 통해 검색어(q)에 해당하는 음악(영상) 목록을 조회합니다.")
    // 유튜브 검색
    public ApiResponse<List<YouTubeSongItemResponse>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {

        return ApiResponse.onSuccess(SuccessCode.OK, youTubeService.searchSongs(q, limit));
    }

    @PostMapping("/save")
    @Operation(summary = "곡 저장",
            description = "검색 결과의 songId(videoId)를 전달하면 해당 곡 정보를 조회한 뒤 로그인 사용자 기준으로 DB에 저장합니다.")
    // 곡 저장
    public ApiResponse<SavedSongResponse> save(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SongSaveRequest req) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ApiResponse.onSuccess(SuccessCode.OK, youTubeService.saveSong(userId, req.getSongId()));
    }

    @GetMapping("/me")
    @Operation(summary = "내 저장 곡 조회",
            description = "로그인 사용자의 저장 곡을 조회합니다.")
    // 내 저장 목록
    public ApiResponse<List<SavedSongResponse>> myList(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ApiResponse.onSuccess(SuccessCode.OK, youTubeService.mySavedSongs(userId));
    }

    @DeleteMapping("/save/{songId}")
    @Operation(summary = "저장된 곡 삭제",
            description = "로그인 사용자가 저장한 곡 중 songId(videoId)에 해당하는 항목을 삭제합니다.")
    // 곡 삭제
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String songId) {
        Long userId = Long.valueOf(jwt.getSubject());
        youTubeService.deleteSavedSong(userId, songId);
        return ApiResponse.onSuccess(SuccessCode.OK, null);
    }
}
