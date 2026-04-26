package com.backend.kdt.auth.repository;

import com.backend.kdt.auth.entity.User;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String userName);

    boolean existsByUserName(String userName);

    @Query("SELECT u FROM User u WHERE u.userName = :userName")
    Optional<User> findUserByUserName(@Param("userName") String userName);

    // 비관적 락: 포인트 차감 시 해당 row 선점
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithLock(@Param("id") Long id);
}
