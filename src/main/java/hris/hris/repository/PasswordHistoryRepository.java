package hris.hris.repository;

import hris.hris.entity.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {

    @Query("SELECT ph FROM PasswordHistory ph WHERE ph.employeeId = :employeeId ORDER BY ph.createdAt DESC")
    List<PasswordHistory> findByEmployeeIdOrderByCreatedAtDesc(@Param("employeeId") Long employeeId);

    @Query("SELECT ph FROM PasswordHistory ph WHERE ph.employeeId = :employeeId ORDER BY ph.createdAt DESC LIMIT 5")
    List<PasswordHistory> findLast5PasswordsByEmployeeId(@Param("employeeId") Long employeeId);
}