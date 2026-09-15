package likelion14th.lte.login.client;

import tools.jackson.databind.JsonNode;
import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class KakaoClient {
    private final RestTemplate restTemplate;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String tokenUri;
    private final String userInfoUri;

    @Autowired
    public KakaoClient(@Value("${kakao.client-id}") String clientId,
                       @Value("${kakao.client-secret:}") String clientSecret,
                       @Value("${kakao.redirect-uri}") String redirectUri,
                       @Value("${kakao.token-uri}") String tokenUri,
                       @Value("${kakao.user-info-uri}") String userInfoUri) {
        this(createRestTemplate(), clientId, clientSecret, redirectUri, tokenUri, userInfoUri);
    }

    KakaoClient(RestTemplate restTemplate, String clientId, String clientSecret,
                String redirectUri, String tokenUri, String userInfoUri) {
        this.restTemplate = restTemplate;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.tokenUri = tokenUri;
        this.userInfoUri = userInfoUri;
    }

    private static RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
    }

    public String getAccessToken(String code) {
        if (!StringUtils.hasText(code)) {
            throw GeneralException.of(ErrorCode.BAD_REQUEST);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);
        body.add("client_id", clientId);
        if (StringUtils.hasText(clientSecret)) {
            body.add("client_secret", clientSecret);
        }
        try {
            JsonNode response = restTemplate.postForEntity(tokenUri,
                    new HttpEntity<>(body, headers), JsonNode.class).getBody();
            if (response == null || !response.path("access_token").isString()
                    || !StringUtils.hasText(response.path("access_token").asText())) {
                throw GeneralException.of(ErrorCode.KAKAO_API_FAILED);
            }
            return response.path("access_token").asText();
        } catch (HttpClientErrorException e) {
            throw GeneralException.of(ErrorCode.KAKAO_AUTH_FAILED);
        } catch (RestClientException e) {
            throw GeneralException.of(ErrorCode.KAKAO_API_FAILED);
        }
    }

    public JsonNode getUserInfo(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            throw GeneralException.of(ErrorCode.BAD_REQUEST);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    userInfoUri,
                    HttpMethod.GET,
                    request,
                    JsonNode.class
            );

            JsonNode body = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || body == null
                    || !body.path("id").isIntegralNumber()
                    || !body.path("id").canConvertToLong() || body.path("id").asLong() <= 0) {
                throw GeneralException.of(ErrorCode.KAKAO_API_FAILED);
            }
            return body;
        } catch (HttpClientErrorException e) {
            throw GeneralException.of(ErrorCode.KAKAO_AUTH_FAILED);
        } catch (RestClientException e) {
            throw GeneralException.of(ErrorCode.KAKAO_API_FAILED);
        }
    }

}
