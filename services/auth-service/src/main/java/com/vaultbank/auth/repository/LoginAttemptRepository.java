package com.vaultbank.auth.repository;

import com.vaultbank.auth.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    @Query("""
        SELECT COUNT(la) FROM LoginAttempt la
        WHERE la.email = :email
          AND la.success = false
          AND la.attemptedAt >= :since
        """)
    long countFailedAttemptsSince(String email, LocalDateTime since);
}
