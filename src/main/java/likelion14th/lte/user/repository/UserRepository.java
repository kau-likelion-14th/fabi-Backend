package likelion14th.lte.user.repository;

import likelion14th.lte.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository  extends JpaRepository<User, Long>{
    Optional<User> findById(Long id);
}