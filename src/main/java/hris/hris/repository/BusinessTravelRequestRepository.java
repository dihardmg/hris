package hris.hris.repository;

import hris.hris.model.BusinessTravelRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BusinessTravelRequestRepository extends JpaRepository<BusinessTravelRequest, Long> {

    List<BusinessTravelRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    List<BusinessTravelRequest> findByStatusOrderByCreatedAtDesc(BusinessTravelRequest.RequestStatus status);

    @Query("SELECT btr FROM BusinessTravelRequest btr JOIN btr.employee e WHERE e.supervisorId = :supervisorId AND btr.status = 'PENDING'")
    List<BusinessTravelRequest> findPendingRequestsBySupervisor(@Param("supervisorId") Long supervisorId);

    @Query("SELECT btr FROM BusinessTravelRequest btr WHERE btr.employee.id = :employeeId AND " +
           "btr.status = 'APPROVED' AND btr.startDate <= :currentDate AND btr.endDate >= :currentDate")
    List<BusinessTravelRequest> findCurrentTravel(@Param("employeeId") Long employeeId,
                                                  @Param("currentDate") LocalDate currentDate);
}