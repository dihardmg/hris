package hris.hris.repository;

import hris.hris.model.AnnualLeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnualLeaveBalanceRepository extends JpaRepository<AnnualLeaveBalance, Long> {

    /**
     * Find annual leave balance by employee ID
     */
    Optional<AnnualLeaveBalance> findByEmployeeId(Long employeeId);

    /**
     * Find annual leave balances for multiple employees
     */
    List<AnnualLeaveBalance> findByEmployeeIdIn(List<Long> employeeIds);

    /**
     * Check if employee has annual leave balance record
     */
    boolean existsByEmployeeId(Long employeeId);

    /**
     * Get remaining balance for specific employee
     */
    @Query("SELECT alb.cutiTahunan - alb.cutiTahunanTerpakai " +
           "FROM AnnualLeaveBalance alb WHERE alb.employee.id = :employeeId")
    Optional<Integer> findRemainingBalanceByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Get all employees with insufficient balance (negative or zero remaining)
     */
    @Query("SELECT alb FROM AnnualLeaveBalance alb " +
           "WHERE alb.cutiTahunan - alb.cutiTahunanTerpakai <= 0")
    List<AnnualLeaveBalance> findEmployeesWithInsufficientBalance();

    /**
     * Get annual leave balance statistics for reporting
     */
    @Query("SELECT " +
           "COUNT(alb) as totalEmployees, " +
           "SUM(alb.cutiTahunan) as totalQuota, " +
           "SUM(alb.cutiTahunanTerpakai) as totalUsed, " +
           "SUM(alb.cutiTahunan - alb.cutiTahunanTerpakai) as totalRemaining " +
           "FROM AnnualLeaveBalance alb")
    Object[] getAnnualLeaveStatistics();

    /**
     * Find employees with specific remaining balance range
     */
    @Query("SELECT alb FROM AnnualLeaveBalance alb " +
           "WHERE (alb.cutiTahunan - alb.cutiTahunanTerpakai) BETWEEN :minBalance AND :maxBalance")
    List<AnnualLeaveBalance> findByRemainingBalanceRange(
            @Param("minBalance") int minBalance,
            @Param("maxBalance") int maxBalance
    );
}