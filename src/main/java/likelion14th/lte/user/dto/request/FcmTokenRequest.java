package likelion14th.lte.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class FcmTokenRequest {
    @NotBlank(message = "FCM token is required.")
    private String token;
}
