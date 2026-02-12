package com.backend.kdt.auth.repository;

import com.backend.kdt.auth.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String userName);

    boolean existsByUserName(String userName);

    @Query("SELECT u FROM User u WHERE u.userName = :userName")
    Optional<User> findUserByUserName(@Param("userName") String userName);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.userName = :userName")
    boolean checkUserNameExists(@Param("userName") String userName);

}
