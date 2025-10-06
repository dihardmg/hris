package hris.hris.repository;

import hris.hris.model.BusinessTravelRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessTravelRequestRepository extends JpaRepository<BusinessTravelRequest, Long> {

    List<BusinessTravelRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    Page<BusinessTravelRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId, Pageable pageable);

    Optional<BusinessTravelRequest> findByUuid(UUID uuid);

    List<BusinessTravelRequest> findByStatusOrderByCreatedAtDesc(BusinessTravelRequest.RequestStatus status);

    @Query("SELECT btr FROM BusinessTravelRequest btr JOIN btr.employee e WHERE e.supervisorId = :supervisorId AND btr.status = 'PENDING'")
    List<BusinessTravelRequest> findPendingRequestsBySupervisor(@Param("supervisorId") Long supervisorId);

    @Query("SELECT btr FROM BusinessTravelRequest btr JOIN btr.employee e WHERE e.supervisorId = :supervisorId AND btr.status = 'PENDING'")
    Page<BusinessTravelRequest> findPendingRequestsBySupervisor(@Param("supervisorId") Long supervisorId, Pageable pageable);

    @Query("SELECT btr FROM BusinessTravelRequest btr WHERE btr.employee.id = :employeeId AND " +
           "btr.status = 'APPROVED' AND btr.startDate <= :currentDate AND btr.endDate >= :currentDate")
    List<BusinessTravelRequest> findCurrentTravel(@Param("employeeId") Long employeeId,
                                                  @Param("currentDate") LocalDate currentDate);

    @Query("SELECT btr FROM BusinessTravelRequest btr WHERE btr.employee.id = :employeeId AND " +
           "btr.startDate = :startDate AND btr.endDate = :endDate AND " +
           "btr.status IN ('PENDING', 'APPROVED')")
    Optional<BusinessTravelRequest> findDuplicateRequest(@Param("employeeId") Long employeeId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);
}