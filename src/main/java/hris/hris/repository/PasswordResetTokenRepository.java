package hris.hris.repository;

import hris.hris.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);

    @Query("SELECT t FROM PasswordResetToken t WHERE t.email = :email AND t.used = false AND t.expiryDate > :now ORDER BY t.createdAt DESC")
    List<PasswordResetToken> findValidTokensByEmail(@Param("email") String email, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(t) FROM PasswordResetToken t WHERE t.email = :email AND t.createdAt >= :since")
    long countRecentRequestsByEmail(@Param("email") String email, @Param("since") LocalDateTime since);

    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.used = true, t.usedAt = :usedAt WHERE t.email = :email AND t.used = false")
    void invalidateAllTokensForEmail(@Param("email") String email, @Param("usedAt") LocalDateTime usedAt);

    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}