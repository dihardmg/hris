package hris.hris.repository;

import hris.hris.model.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {

    Optional<LeaveType> findByCode(String code);

    List<LeaveType> findByIsActiveTrue();

    List<LeaveType> findByHasBalanceQuotaTrue();
}