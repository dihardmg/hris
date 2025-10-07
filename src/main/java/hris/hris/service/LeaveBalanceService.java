package hris.hris.service;

import hris.hris.model.AnnualLeaveBalance;
import hris.hris.model.Employee;
import hris.hris.model.HrQuota;
import hris.hris.dto.LeaveBalanceDto;
import hris.hris.repository.AnnualLeaveBalanceRepository;
import hris.hris.repository.EmployeeRepository;
import hris.hris.repository.HrQuotaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
@Slf4j
public class LeaveBalanceService {

    @Autowired
    private AnnualLeaveBalanceRepository annualLeaveBalanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private HrQuotaRepository hrQuotaRepository;

    /**
     * Get available annual leave balance for an employee
     * @param employeeId the employee ID
     * @return available balance days
     */
    @Transactional(readOnly = true)
    public int getAvailableAnnualLeave(Long employeeId) {
        return getAvailableAnnualLeave(employeeId, Year.now());
    }

    /**
     * Get available annual leave balance for an employee in specific year
     * @param employeeId the employee ID
     * @param tahun the year
     * @return available balance days
     */
    @Transactional(readOnly = true)
    public int getAvailableAnnualLeave(Long employeeId, Year tahun) {
        HrQuota quota = getOrCreateQuota(employeeId, tahun);
        return quota.getCutiTahunan(); // Available balance adalah sisa cuti tahunan
    }

    /**
     * Get full annual leave balance object for an employee
     * @param employeeId the employee ID
     * @return AnnualLeaveBalance object (for backward compatibility)
     */
    @Transactional(readOnly = true)
    public AnnualLeaveBalance getAnnualLeaveBalance(Long employeeId) {
        return getOrCreateBalance(employeeId);
    }

    /**
     * Get HrQuota for an employee in specific year
     * @param employeeId the employee ID
     * @param tahun the year
     * @return HrQuota object
     */
    @Transactional(readOnly = true)
    public HrQuota getHrQuota(Long employeeId, Year tahun) {
        return getOrCreateQuota(employeeId, tahun);
    }

    /**
     * Deduct annual leave balance when leave is approved
     * @param employeeId the employee ID
     * @param days number of days to deduct
     */
    @Transactional
    public void deductAnnualLeave(Long employeeId, int days) {
        deductAnnualLeave(employeeId, days, Year.now());
    }

    /**
     * Deduct annual leave balance when leave is approved in specific year
     * @param employeeId the employee ID
     * @param days number of days to deduct
     * @param tahun the year
     */
    @Transactional
    public void deductAnnualLeave(Long employeeId, int days, Year tahun) {
        HrQuota quota = getOrCreateQuota(employeeId, tahun);

        if (!quota.adaSisaCuti(days)) {
            throw new RuntimeException(
                String.format("Insufficient annual leave balance. Available: %d days, Requested: %d days",
                    quota.getSisaCutiTahunan(), days));
        }

        quota.tambahCutiTerpakai(days);
        hrQuotaRepository.save(quota);

        log.info("Deducted {} days from annual leave balance for employee {} in year {}. Remaining balance: {} days",
                days, employeeId, tahun, quota.getSisaCutiTahunan());
    }

    /**
     * Restore annual leave balance when leave is cancelled/rejected
     * @param employeeId the employee ID
     * @param days number of days to restore
     */
    @Transactional
    public void restoreAnnualLeave(Long employeeId, int days) {
        restoreAnnualLeave(employeeId, days, Year.now());
    }

    /**
     * Restore annual leave balance when leave is cancelled/rejected in specific year
     * @param employeeId the employee ID
     * @param days number of days to restore
     * @param tahun the year
     */
    @Transactional
    public void restoreAnnualLeave(Long employeeId, int days, Year tahun) {
        HrQuota quota = getOrCreateQuota(employeeId, tahun);

        // Ensure we don't restore more than used
        quota.kurangiCutiTerpakai(days);
        hrQuotaRepository.save(quota);

        log.info("Restored {} days to annual leave balance for employee {} in year {}. New used days: {}",
                days, employeeId, tahun, quota.getCutiTahunanTerpakai());
    }

    /**
     * Reset annual leave balance for new year
     * @param employeeId the employee ID
     * @param newQuota new annual quota
     */
    @Transactional
    public void resetAnnualBalance(Long employeeId, int newQuota) {
        AnnualLeaveBalance balance = getOrCreateBalance(employeeId);
        int oldRemaining = balance.getRemainingBalance();

        balance.resetAnnualBalance(newQuota);
        annualLeaveBalanceRepository.save(balance);

        log.info("Reset annual leave balance for employee {}. Old remaining: {} days, New quota: {} days",
                employeeId, oldRemaining, newQuota);
    }

    /**
     * Reset annual leave balance for all employees
     * @param newQuota new annual quota
     */
    @Transactional
    public void resetAllAnnualBalances(int newQuota) {
        List<AnnualLeaveBalance> allBalances = annualLeaveBalanceRepository.findAll();

        for (AnnualLeaveBalance balance : allBalances) {
            int oldRemaining = balance.getRemainingBalance();
            balance.resetAnnualBalance(newQuota);
        }

        annualLeaveBalanceRepository.saveAll(allBalances);

        log.info("Reset annual leave balance for {} employees to {} days quota",
                allBalances.size(), newQuota);
    }

    /**
     * Create new annual leave balance record for employee
     * @param employeeId the employee ID
     * @param initialQuota initial annual quota
     */
    @Transactional
    public AnnualLeaveBalance createInitialBalance(Long employeeId, int initialQuota) {
        if (annualLeaveBalanceRepository.existsByEmployeeId(employeeId)) {
            throw new RuntimeException("Annual leave balance already exists for employee: " + employeeId);
        }

        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));

        AnnualLeaveBalance balance = new AnnualLeaveBalance();
        balance.setEmployee(employee);
        balance.setCutiTahunan(initialQuota);
        balance.setCutiTahunanTerpakai(0);
        balance.setCreatedAt(LocalDateTime.now());
        balance.setUpdatedAt(LocalDateTime.now());

        AnnualLeaveBalance savedBalance = annualLeaveBalanceRepository.save(balance);

        log.info("Created initial annual leave balance for employee {} with quota {} days",
                employeeId, initialQuota);

        return savedBalance;
    }

    /**
     * Update annual leave quota for specific employee
     * @param employeeId the employee ID
     * @param newQuota new annual quota
     */
    @Transactional
    public void updateAnnualQuota(Long employeeId, int newQuota) {
        AnnualLeaveBalance balance = getOrCreateBalance(employeeId);
        int oldQuota = balance.getCutiTahunan();

        balance.setCutiTahunan(newQuota);
        annualLeaveBalanceRepository.save(balance);

        log.info("Updated annual leave quota for employee {} from {} to {} days",
                employeeId, oldQuota, newQuota);
    }

    /**
     * Get annual leave statistics for reporting
     * @return array containing [totalEmployees, totalQuota, totalUsed, totalRemaining]
     */
    @Transactional(readOnly = true)
    public Object[] getAnnualLeaveStatistics() {
        return annualLeaveBalanceRepository.getAnnualLeaveStatistics();
    }

    /**
     * Get employees with insufficient balance
     * @return list of AnnualLeaveBalance with zero or negative remaining days
     */
    @Transactional(readOnly = true)
    public List<AnnualLeaveBalance> getEmployeesWithInsufficientBalance() {
        return annualLeaveBalanceRepository.findEmployeesWithInsufficientBalance();
    }

    /**
     * Get annual leave balances for multiple employees
     * @param employeeIds list of employee IDs
     * @return list of AnnualLeaveBalance
     */
    @Transactional(readOnly = true)
    public List<AnnualLeaveBalance> getBalancesForEmployees(List<Long> employeeIds) {
        return annualLeaveBalanceRepository.findByEmployeeIdIn(employeeIds);
    }

    /**
     * Get or create annual leave balance for employee
     * @param employeeId the employee ID
     * @return AnnualLeaveBalance object
     */
    private AnnualLeaveBalance getOrCreateBalance(Long employeeId) {
        return annualLeaveBalanceRepository.findByEmployeeId(employeeId)
            .orElseGet(() -> createInitialBalance(employeeId, 12)); // Default 12 days
    }

    /**
     * Get or create HrQuota for employee in specific year
     * @param employeeId the employee ID
     * @param tahun the year
     * @return HrQuota object
     */
    private HrQuota getOrCreateQuota(Long employeeId, Year tahun) {
        return hrQuotaRepository.findByIdEmployeeAndTahun(employeeId, tahun)
            .orElseGet(() -> createInitialQuota(employeeId, 12, tahun)); // Default 12 days
    }

    /**
     * Create new HrQuota record for employee
     * @param employeeId the employee ID
     * @param initialQuota initial annual quota
     * @param tahun the year
     * @return HrQuota object
     */
    @Transactional
    public HrQuota createInitialQuota(Long employeeId, int initialQuota, Year tahun) {
        if (hrQuotaRepository.existsByIdEmployeeAndTahun(employeeId, tahun)) {
            throw new RuntimeException("HrQuota already exists for employee " + employeeId + " in year " + tahun);
        }

        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));

        HrQuota quota = new HrQuota();
        quota.setIdEmployee(employeeId);
        quota.setCutiTahunan(initialQuota);
        quota.setCutiTahunanTerpakai(0);
        quota.setTahun(tahun);

        HrQuota savedQuota = hrQuotaRepository.save(quota);

        log.info("Created initial hr quota for employee {} in year {} with quota {} days",
                employeeId, tahun, initialQuota);

        return savedQuota;
    }

    /**
     * Reset annual leave quota for all employees in specific year
     * @param tahun the year
     * @param newQuota new annual quota
     */
    @Transactional
    public void resetAllQuotasByYear(Year tahun, int newQuota) {
        List<HrQuota> allQuotas = hrQuotaRepository.findByTahun(tahun);

        for (HrQuota quota : allQuotas) {
            quota.setCutiTahunan(newQuota);
            quota.setCutiTahunanTerpakai(0);
        }

        hrQuotaRepository.saveAll(allQuotas);

        log.info("Reset hr quota for {} employees in year {} to {} days quota",
                allQuotas.size(), tahun, newQuota);
    }

    /**
     * Update annual quota for employee in specific year
     * @param employeeId the employee ID
     * @param tahun the year
     * @param newQuota new annual quota
     */
    @Transactional
    public void updateAnnualQuota(Long employeeId, Year tahun, int newQuota) {
        HrQuota quota = getOrCreateQuota(employeeId, tahun);
        int oldQuota = quota.getCutiTahunan();

        quota.setCutiTahunan(newQuota);
        hrQuotaRepository.save(quota);

        log.info("Updated annual quota for employee {} in year {} from {} to {} days",
                employeeId, tahun, oldQuota, newQuota);
    }

    /**
     * Get quota statistics for reporting in specific year
     * @param tahun the year
     * @return list of quota summaries
     */
    @Transactional(readOnly = true)
    public List<Object[]> getQuotaStatisticsByYear(Year tahun) {
        return hrQuotaRepository.getQuotaSummaryByYear(tahun);
    }

    /**
     * Get quotas with negative balance for specific year
     * @param tahun the year
     * @return list of HrQuota with negative balance
     */
    @Transactional(readOnly = true)
    public List<HrQuota> getQuotasWithNegativeBalanceByYear(Year tahun) {
        return hrQuotaRepository.findQuotasWithNegativeBalance().stream()
            .filter(quota -> quota.getTahun().equals(tahun))
            .toList();
    }

    // Methods for LeaveBalanceController - Backward compatibility

    /**
     * Get all leave balances for employee (backward compatibility)
     * @param employeeId the employee ID
     * @return list of LeaveBalanceDto
     */
    @Transactional(readOnly = true)
    public List<LeaveBalanceDto> getAllLeaveBalancesForEmployee(Long employeeId) {
        List<LeaveBalanceDto> balances = new ArrayList<>();

        // Get current annual leave balance from hr_quota
        HrQuota currentQuota = getOrCreateQuota(employeeId, Year.now());
        LeaveBalanceDto annualLeaveBalance = new LeaveBalanceDto();
        annualLeaveBalance.setEmployeeId(employeeId);
        annualLeaveBalance.setLeaveTypeName("Annual Leave");
        annualLeaveBalance.setLeaveTypeCode("ANNUAL_LEAVE");
        // Hitung total kuota asli (cuti tahunan + cuti terpakai)
        int totalOriginalQuota = currentQuota.getCutiTahunan() + currentQuota.getCutiTahunanTerpakai();
        annualLeaveBalance.setTotalQuota(totalOriginalQuota);
        annualLeaveBalance.setUsedQuota(currentQuota.getCutiTahunanTerpakai());
        annualLeaveBalance.setRemainingQuota(currentQuota.getCutiTahunan()); // Sisa adalah cuti tahunan yang tersisa
        annualLeaveBalance.setHasBalanceQuota(true);
        balances.add(annualLeaveBalance);

        // Add other leave types from employee entity (for now)
        Optional<Employee> employee = employeeRepository.findById(employeeId);
        if (employee.isPresent()) {
            Employee emp = employee.get();

            // Sick leave
            LeaveBalanceDto sickLeaveBalance = new LeaveBalanceDto();
            sickLeaveBalance.setEmployeeId(employeeId);
            sickLeaveBalance.setLeaveTypeName("Sick Leave");
            sickLeaveBalance.setLeaveTypeCode("SICK_LEAVE");
            sickLeaveBalance.setTotalQuota(emp.getSickLeaveBalance());
            sickLeaveBalance.setUsedQuota(0); // Not tracked yet
            sickLeaveBalance.setRemainingQuota(emp.getSickLeaveBalance());
            sickLeaveBalance.setHasBalanceQuota(true);
            balances.add(sickLeaveBalance);
        }

        return balances;
    }

    /**
     * Get leave balance for employee and specific type (backward compatibility)
     * @param employeeId the employee ID
     * @param leaveTypeId the leave type ID (not used in new implementation, using code instead)
     * @return Optional LeaveBalanceDto
     */
    @Transactional(readOnly = true)
    public Optional<LeaveBalanceDto> getLeaveBalanceForEmployeeAndType(Long employeeId, Long leaveTypeId) {
        // For now, return annual leave balance for any leaveTypeId
        // In the future, this should map leaveTypeId to leave type codes
        return getAllLeaveBalancesForEmployee(employeeId).stream()
            .filter(balance -> "ANNUAL_LEAVE".equals(balance.getLeaveTypeCode()))
            .findFirst();
    }

    /**
     * Get leave balances for supervisor's subordinates (backward compatibility)
     * @param supervisorId the supervisor ID
     * @return list of LeaveBalanceDto
     */
    @Transactional(readOnly = true)
    public List<LeaveBalanceDto> getLeaveBalancesForSupervisor(Long supervisorId) {
        List<LeaveBalanceDto> allBalances = new ArrayList<>();

        // Find all employees supervised by this supervisor
        List<Employee> subordinates = employeeRepository.findBySupervisorId(supervisorId);

        for (Employee subordinate : subordinates) {
            allBalances.addAll(getAllLeaveBalancesForEmployee(subordinate.getId()));
        }

        return allBalances;
    }

    /**
     * Get all leave balances for manager (backward compatibility)
     * @return list of LeaveBalanceDto
     */
    @Transactional(readOnly = true)
    public List<LeaveBalanceDto> getLeaveBalancesForManager() {
        List<LeaveBalanceDto> allBalances = new ArrayList<>();

        // Get all employees
        List<Employee> allEmployees = employeeRepository.findAll();

        for (Employee employee : allEmployees) {
            allBalances.addAll(getAllLeaveBalancesForEmployee(employee.getId()));
        }

        return allBalances;
    }
}