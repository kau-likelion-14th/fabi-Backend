package likelion14th.lte.user.entity;

import jakarta.persistence.*;
import likelion14th.lte.Entity.BaseEntity;
import likelion14th.lte.follow.entity.Follow;
import likelion14th.lte.statistic.entity.Statistic;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
// [Q1. @NoArgsConstructor는 매개변수가 없는 기본 생성자를 만듭니다.
// 그런데 왜 누구나 쓸 수 있게 PUBLIC으로 열어두지 않고, 굳이 PROTECTED로 막아두었을까요? (객체 생성의 안전성과 JPA 관점)]
/** 답변:
 * JPA는 DB에서 객체를 조회할때 기본 생성자를 사용하는데
 * 외부에서 마음대로 생성하지 못하도록 protected로 제한하여 의도치 않은
 * 객체 생성을 막아 안정성을 확보한다.
 */
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // [Q2. @Column(nullable = false) 어노테이션이 DB와 자바 코드 사이에서 하는 역할은 무엇인가요?]
    /** 답변:
     * 해당 column에 null이 저장되는 것을 제한한다. 무결성
     */
    @Column(nullable = false)
    private String username;

    @Column(length = 16, nullable = false, unique = true)
    private String userTag;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column(columnDefinition = "TEXT")
    private String profileImage;

    @Column(columnDefinition = "TEXT")
    private String s3ImageKey;

    @OneToMany(mappedBy = "toUser", fetch = FetchType.LAZY, cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<Follow> followers;

    @OneToMany(mappedBy = "toUser", fetch = FetchType.LAZY, cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<Follow> followerings;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "statistic_id")
    private Statistic statistic;

    @Builder(access = AccessLevel.PUBLIC)
    private User(String username, String userTag, String introduction) {
        this.username = username;
        this.userTag = userTag;
        this.introduction = introduction;
        this.followers = new ArrayList<>();
        this.followerings = new ArrayList<>();
        this.statistic = Statistic.create();
    }

    // [Q3. @Setter를 위 @Getter 처럼 사용하면 모든 맴버들에 setIntruduction() 같은 setter 메서드가 생성됩니다.
    // 하지만 왜 @Setter를 쓰지않고 updateIntroduction() 이라는 명확한 메서드를 만든 객체지향적인 이유는 무엇인가요?]
    /** 답변:
     * 의미가 명확한 메서드를 사용하게 되면 코드를 보고 이해하기 쉬워진다.
     * 관련된 추가 내용을 메서드 내부에 넣는 등의 설계와 유지보수에 유리해짐.
     */
    public void  updateIntroduction(String introduction) {
        this.introduction = introduction;
    }
}
