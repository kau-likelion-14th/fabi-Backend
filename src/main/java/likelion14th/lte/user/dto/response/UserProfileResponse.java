package likelion14th.lte.user.dto.response;


import likelion14th.lte.user.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileResponse{
    private String userName;
    private String profileImageUrl;
    private String introduction;

    // [Q4. Controller가 DB에서 꺼낸 원본 Entity(User)를 클라이언트 화면에 그대로 반환하지 않고,
    // 굳이 from() 메서드를 통해 DTO로 한번 변환해서 내보내는 핵심적인 이유 2가지는 무엇인가요?]
    /** 답변:
     * 1. DB구조 노출과 같은 보안이슈
     * 2. 필요한 데이터만 전달하여 유지보수 용이
     */
    public static UserProfileResponse from(User user){
        return new UserProfileResponse(
                user.getUsername() + "#" + user.getUserTag(),
                user.getProfileImage(),
                user.getIntroduction()
        );
    }
}
