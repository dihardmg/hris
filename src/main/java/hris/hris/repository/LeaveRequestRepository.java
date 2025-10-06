package hris.hris.repository;

import hris.hris.model.LeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    Page<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId, Pageable pageable);

    List<LeaveRequest> findByStatusOrderByCreatedAtDesc(LeaveRequest.RequestStatus status);

    @Query("SELECT lr FROM LeaveRequest lr JOIN lr.employee e WHERE e.supervisorId = :supervisorId AND lr.status = 'PENDING'")
    List<LeaveRequest> findPendingRequestsBySupervisor(@Param("supervisorId") Long supervisorId);

    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.status = 'APPROVED' AND " +
           "((lr.startDate <= :endDate AND lr.endDate >= :startDate))")
    int countOverlappingLeaves(@Param("employeeId") Long employeeId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND " +
           "lr.status = 'APPROVED' AND lr.startDate <= :currentDate AND lr.endDate >= :currentDate")
    Optional<LeaveRequest> findCurrentLeave(@Param("employeeId") Long employeeId,
                                           @Param("currentDate") LocalDate currentDate);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND " +
           "lr.createdAt >= :startDate ORDER BY lr.createdAt DESC")
    Page<LeaveRequest> findByEmployeeIdAndCreatedAtAfter(@Param("employeeId") Long employeeId,
                                                         @Param("startDate") LocalDateTime startDate,
                                                         Pageable pageable);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND " +
           "lr.leaveType.code = :leaveTypeCode AND lr.status = :status")
    List<LeaveRequest> findByEmployeeIdAndLeaveTypeCodeAndStatus(@Param("employeeId") Long employeeId,
                                                                  @Param("leaveTypeCode") String leaveTypeCode,
                                                                  @Param("status") LeaveRequest.RequestStatus status);

    // UUID-based queries for API security
    Optional<LeaveRequest> findByUuid(UUID uuid);

    // Check for duplicate leave requests (same employee, start date, and end date)
    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND " +
           "lr.startDate = :startDate AND lr.endDate = :endDate")
    int countDuplicateLeaveRequests(@Param("employeeId") Long employeeId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);
}