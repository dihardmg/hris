package hris.hris.repository;

import hris.hris.model.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

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
}