package likelion14th.lte.youtube.service;

import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.user.entity.User;
import likelion14th.lte.user.repository.UserRepository;
import likelion14th.lte.youtube.client.YouTubeClient;
import likelion14th.lte.youtube.entity.SavedSong;
import likelion14th.lte.youtube.dto.response.SavedSongResponse;
import likelion14th.lte.youtube.dto.response.YouTubeSongItemResponse;
import likelion14th.lte.youtube.repository.SavedSongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
@Transactional
public class YouTubeService {

    //TODO: 카카오 로그인 구현 후 더미 사용자 대신 실제 로그인 사용자로 교체
    private static final String DUMMY_LOGIN_ID = "youtube_dummy";
    private static final String DUMMY_USERNAME = "YouTube Demo User";
    private static final String DUMMY_USER_TAG = "YTDUMMY01";

    private final YouTubeClient youTubeClient;
    private final SavedSongRepository savedSongRepository;
    private final UserRepository userRepository;

    // 카카오 로그인 전 임시 사용자.
    // 최초 호출 시 DB에 한 번 생성하고, 이후에는 같은 사용자를 계속 사용합니다.
    private User getDummyUser() {
        return userRepository.findByUsername(DUMMY_USERNAME)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .username(DUMMY_USERNAME)
                                .userTag(DUMMY_USER_TAG)
                                .introduction("YouTube 세션용 더미 사용자")
                                .build()
                ));
    }

    // 유튜브 검색
    @Transactional(readOnly = true)
    public List<YouTubeSongItemResponse> searchSongs(String query, int limit) {
        JsonNode root = youTubeClient.searchVideosRaw(query, limit);
        JsonNode items = root.path("items");
        List<YouTubeSongItemResponse> result = new ArrayList<>();

        if (!items.isArray()) {
            return result;
        }

        for (JsonNode item : items) {
            String videoId = item.path("id").path("videoId").asText(null);
            if (videoId == null) {
                continue;
            }

            JsonNode snippet = item.path("snippet");

            result.add(YouTubeSongItemResponse.builder()
                    .songId(videoId)
                    .title(snippet.path("title").asText(""))
                    .artist(snippet.path("channelTitle").asText(""))
                    .imageUrl(extractThumbnail(snippet))
                    .build());
        }

        return result;
    }

    // 곡 저장
    public SavedSongResponse saveSong(String songId) {
        if (songId == null || songId.isBlank()) {
            throw new GeneralException(ErrorCode.BAD_REQUEST);
        }

        User user = getDummyUser();

        if (savedSongRepository.existsByUserAndSongId(user, songId)) {
            throw new GeneralException(ErrorCode.SONG_ALREADY_SAVED);
        }

        // search 결과를 프론트에서 그대로 믿고 저장하지 않고,
        // videoId로 YouTube API를 한 번 더 조회해 실제 정보를 DB에 저장합니다.
        JsonNode item = getFirstVideo(songId);
        JsonNode snippet = item.path("snippet");
        String duration = item.path("contentDetails").path("duration").asText(null);

        SavedSong savedSong = SavedSong.builder()
                .user(user)
                .songId(songId)
                .title(snippet.path("title").asText(""))
                .artist(snippet.path("channelTitle").asText(""))
                .imageUrl(extractThumbnail(snippet))
                .durationMs(parseDurationMs(duration))
                .build();

        return SavedSongResponse.from(savedSongRepository.save(savedSong));
    }

    // videoId 하나로 영상 상세 정보를 가져옵니다. 외부 API로 공개하지 않고 저장 로직 내부에서만 사용합니다.
    private JsonNode getFirstVideo(String videoId) {
        JsonNode root = youTubeClient.getVideoRaw(videoId);
        JsonNode items = root.path("items");

        if (!items.isArray() || items.size() == 0) {
            throw new GeneralException(ErrorCode.SONG_NOT_FOUND);
        }

        return items.get(0);
    }

    // 내 저장 곡 조회 - 현재는 더미 사용자 기준
    public List<SavedSongResponse> mySavedSongs() {
        User user = getDummyUser();

        return savedSongRepository.findAllByUserOrderBySavedAtDesc(user)
                .stream()
                .map(SavedSongResponse::from)
                .toList();
    }

    // 저장 곡 삭제 - 현재는 더미 사용자 기준
    public void deleteSavedSong(String songId) {
        User user = getDummyUser();

        SavedSong savedSong = savedSongRepository.findByUserAndSongId(user, songId)
                .orElseThrow(() -> new GeneralException(ErrorCode.SONG_NOT_FOUND));

        savedSongRepository.delete(savedSong);
    }

    private String extractThumbnail(JsonNode snippet) {
        String imageUrl = snippet.path("thumbnails").path("high").path("url").asText(null);

        if (imageUrl == null || imageUrl.isBlank()) {
            imageUrl = snippet.path("thumbnails").path("medium").path("url").asText(null);
        }

        if (imageUrl == null || imageUrl.isBlank()) {
            imageUrl = snippet.path("thumbnails").path("default").path("url").asText(null);
        }

        return imageUrl;
    }

    private Long parseDurationMs(String duration) {
        if (duration == null || duration.isBlank()) {
            return null;
        }

        try {
            return Duration.parse(duration).toMillis();
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
