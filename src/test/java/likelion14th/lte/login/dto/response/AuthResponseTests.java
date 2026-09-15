package likelion14th.lte.login.dto.response;

import likelion14th.lte.user.entity.User;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseTests {
    @Test
    void includesUserTagButNeverSerializesRefreshToken() {
        User user = User.builder().username("tester").userTag("1234").build();
        AuthResponse response = AuthResponse.from(user, "access", "private-refresh");
        var json = JsonMapper.builder().build().valueToTree(response);

        assertEquals("1234", json.get("userTag").asText());
        assertEquals("access", json.get("accessToken").asText());
        assertFalse(json.has("refreshToken"));
        assertEquals("private-refresh", response.getRefreshToken());
    }
}
