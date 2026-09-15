package likelion14th.lte.login.client;

import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class KakaoClientTests {
    private MockRestServiceServer server;
    private KakaoClient client;

    @BeforeEach
    void setUp() {
        RestTemplate template = new RestTemplate();
        server = MockRestServiceServer.createServer(template);
        client = new KakaoClient(template, "client", "secret", "http://localhost/callback",
                "https://kauth.kakao.com/oauth/token", "https://kapi.kakao.com/v2/user/me");
    }

    @Test
    void exchangesCodeAndRetrievesUserWithoutOptionalProfile() {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", "test-code");
        form.add("redirect_uri", "http://localhost/callback");
        form.add("client_id", "client");
        form.add("client_secret", "secret");
        server.expect(requestTo("https://kauth.kakao.com/oauth/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().formData(form))
                .andRespond(withSuccess("{\"access_token\":\"token\",\"token_type\":\"bearer\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer token"))
                .andRespond(withSuccess("{\"id\":123,\"connected_at\":\"2026-09-10\"}", MediaType.APPLICATION_JSON));

        var user = client.getUserInfo(client.getAccessToken("test-code"));
        assertEquals(123L, user.path("id").asLong());
        assertTrue(user.path("kakao_account").isMissingNode());
        assertEquals("2026-09-10", user.path("connected_at").asText());
        server.verify();
    }

    @Test
    void retrievesRawJsonUsingExistingAccessToken() {
        server.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
                .andExpect(header("Authorization", "Bearer existing-token"))
                .andRespond(withSuccess("{\"id\":123,\"kakao_account\":{\"profile\":{\"nickname\":\"tester\"}}}",
                        MediaType.APPLICATION_JSON));
        var user = client.getUserInfo("existing-token");
        assertEquals("tester", user.path("kakao_account").path("profile").path("nickname").asText());
        server.verify();
    }

    @Test
    void rejectsUserResponseWithoutId() {
        server.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        assertEquals(ErrorCode.KAKAO_API_FAILED,
                assertThrows(GeneralException.class, () -> client.getUserInfo("token")).getCode());
        server.verify();
    }

    @Test
    void rejectsBlankCodeWithoutCallingKakao() {
        assertEquals(ErrorCode.BAD_REQUEST,
                assertThrows(GeneralException.class, () -> client.getAccessToken(" ")).getCode());
        server.verify();
    }

    @Test
    void mapsRejectedCodeToAuthenticationFailure() {
        server.expect(requestTo("https://kauth.kakao.com/oauth/token"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));
        assertEquals(ErrorCode.KAKAO_AUTH_FAILED,
                assertThrows(GeneralException.class, () -> client.getAccessToken("invalid")).getCode());
        server.verify();
    }

    @Test
    void rejectsResponseWithoutAccessToken() {
        server.expect(requestTo("https://kauth.kakao.com/oauth/token"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        assertEquals(ErrorCode.KAKAO_API_FAILED,
                assertThrows(GeneralException.class, () -> client.getAccessToken("code")).getCode());
        server.verify();
    }

    @Test
    void mapsServerFailureToApiFailure() {
        server.expect(requestTo("https://kauth.kakao.com/oauth/token"))
                .andRespond(withServerError());
        assertEquals(ErrorCode.KAKAO_API_FAILED,
                assertThrows(GeneralException.class, () -> client.getAccessToken("code")).getCode());
        server.verify();
    }
}
