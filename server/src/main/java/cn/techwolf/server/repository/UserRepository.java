package cn.techwolf.server.repository;

import cn.techwolf.server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    @Query("SELECT COUNT(u) FROM User u WHERE u.email = :email")
    int existsByEmail(@Param("email") String email);
}