package likelion14th.lte.user.repository;

import likelion14th.lte.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// [추가문제] (필수 X) 이 코드는 인터페이스일 뿐이고 구현체(implements) 클래스가 없습니다.
// 그런데 어떻게 프로그램 실행 시 DB와 통신하는 객체로 동작할 수 있나요?
/** 답변:
 * extends JpaRepository를 통해 실행 시점에 자동으로 구현체를 생성함.
 */
public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findById(Long id);
    Page<User> findByUsernameContainingIgnoreCase(String nickName, Pageable pageable);
    Optional<User> findByUserTag(String userTag);

    @Query("SELECT u FROM User u " +
            "WHERE u.id != :userId " +
            "AND NOT EXISTS (SELECT f FROM Follow f WHERE f.fromUser.id = :userId AND f.toUser.id = u.id)")
    Page<User> findCanFollowUser(@Param("userId") Long userId, Pageable pageable);
}