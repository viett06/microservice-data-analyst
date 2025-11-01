package com.viet.auth.repository;

import com.viet.auth.module.GuestSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface GuestSessionRepository extends JpaRepository<GuestSession, Long> {
    Optional<GuestSession> findBySessionId(String sessionId);

    @Modifying
    @Query("UPDATE GuestSession gs SET gs.lastActivity = :lastActivity WHERE gs.sessionId = :sessionId")
    void updateLastActivity(@Param("sessionId") String sessionId, @Param("lastActivity") LocalDateTime lastActivity);

    @Modifying
    @Query("DELETE FROM GuestSession gs WHERE gs.expiresAt < :now")
    void deleteAllExpiredSince(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(gs) FROM GuestSession gs WHERE gs.isActive = true AND gs.expiresAt > :now")
    long countActiveSessions(@Param("now") LocalDateTime now);
}
