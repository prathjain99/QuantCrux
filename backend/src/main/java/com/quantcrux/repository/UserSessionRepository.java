package com.quantcrux.repository;

import com.quantcrux.model.User;
import com.quantcrux.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    
    Optional<UserSession> findBySessionToken(String sessionToken);
    
    List<UserSession> findByUserAndIsActiveTrue(User user);
    
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false, s.logoutTime = CURRENT_TIMESTAMP WHERE s.user = :user")
    void deactivateAllUserSessions(User user);
    
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false, s.logoutTime = CURRENT_TIMESTAMP WHERE s.sessionToken = :token")
    void deactivateSession(String token);
}