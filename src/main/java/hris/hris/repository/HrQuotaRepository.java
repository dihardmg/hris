package hris.hris.repository;

import hris.hris.model.HrQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Year;
import java.util.List;
import java.util.Optional;

@Repository
public interface HrQuotaRepository extends JpaRepository<HrQuota, Long> {

    // Find quota by employee and year
    Optional<HrQuota> findByIdEmployeeAndTahun(Long idEmployee, Year tahun);

    // Find all quotas by employee
    List<HrQuota> findByIdEmployee(Long idEmployee);

    // Find all quotas by year
    List<HrQuota> findByTahun(Year tahun);

    
    // Check if quota exists for employee and year
    boolean existsByIdEmployeeAndTahun(Long idEmployee, Year tahun);

    // Get all employees with insufficient leave balance
    @Query("SELECT h FROM HrQuota h WHERE (h.cutiTahunan - h.cutiTahunanTerpakai) < 0")
    List<HrQuota> findQuotasWithNegativeBalance();

    // Get quota summary for all employees in a year
    @Query("SELECT h.idEmployee, h.cutiTahunan, h.cutiTahunanTerpakai, (h.cutiTahunan - h.cutiTahunanTerpakai) as sisaCuti FROM HrQuota h WHERE h.tahun = :tahun")
    List<Object[]> getQuotaSummaryByYear(@Param("tahun") Year tahun);
}