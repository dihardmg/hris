package hris.hris.repository;

import hris.hris.model.Attendance;
import hris.hris.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
    List<Attendance> findByEmployeeAndClockInTimeBetween(Employee employee, LocalDateTime start, LocalDateTime end);

    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND a.clockInTime >= :startOfDay AND a.clockInTime < :endOfDay")
    Optional<Attendance> findTodayAttendance(@Param("employeeId") Long employeeId,
                                            @Param("startOfDay") LocalDateTime startOfDay,
                                            @Param("endOfDay") LocalDateTime endOfDay);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND a.clockInTime >= :startOfDay AND a.clockInTime < :endOfDay")
    Optional<Attendance> findTodayAttendanceWithLock(@Param("employeeId") Long employeeId,
                                                   @Param("startOfDay") LocalDateTime startOfDay,
                                                   @Param("endOfDay") LocalDateTime endOfDay);

    boolean existsByEmployeeIdAndClockOutTimeIsNull(Long employeeId);

    @Query("SELECT COUNT(a) > 0 FROM Attendance a WHERE a.employee.id = :employeeId AND a.clockInTime >= :startOfDay AND a.clockInTime < :endOfDay AND a.clockOutTime IS NOT NULL")
    boolean existsByEmployeeIdAndClockInTimeBetweenAndClockOutTimeIsNotNull(@Param("employeeId") Long employeeId,
                                                                           @Param("startOfDay") LocalDateTime startOfDay,
                                                                           @Param("endOfDay") LocalDateTime endOfDay);

    List<Attendance> findByClockInTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Attendance> findByEmployeeIdAndClockInTimeBetweenOrderByCreatedAtDesc(Long employeeId, LocalDateTime start, LocalDateTime end);

    Page<Attendance> findByEmployeeIdAndClockInTimeBetweenOrderByCreatedAtDesc(Long employeeId, LocalDateTime start, LocalDateTime end, Pageable pageable);
}