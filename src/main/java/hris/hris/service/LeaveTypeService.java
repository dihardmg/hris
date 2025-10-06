package hris.hris.service;

import hris.hris.model.LeaveType;
import hris.hris.repository.LeaveTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class LeaveTypeService {

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Transactional(readOnly = true)
    public List<LeaveType> getAllActiveLeaveTypes() {
        return leaveTypeRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public Optional<LeaveType> getLeaveTypeById(Long id) {
        return leaveTypeRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<LeaveType> getLeaveTypeByCode(String code) {
        return leaveTypeRepository.findByCode(code);
    }

    @Transactional(readOnly = true)
    public List<LeaveType> getLeaveTypesWithBalance() {
        return leaveTypeRepository.findByHasBalanceQuotaTrue();
    }

    @Transactional
    public LeaveType createLeaveType(LeaveType leaveType) {
        leaveType.setIsActive(true);
        LeaveType saved = leaveTypeRepository.save(leaveType);
        log.info("Created new leave type: {} with code: {}", saved.getName(), saved.getCode());
        return saved;
    }

    @Transactional
    public LeaveType updateLeaveType(Long id, LeaveType leaveTypeDetails) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Leave type not found with id: " + id));

        leaveType.setCode(leaveTypeDetails.getCode());
        leaveType.setName(leaveTypeDetails.getName());
        leaveType.setDescription(leaveTypeDetails.getDescription());
        leaveType.setMinDurationDays(leaveTypeDetails.getMinDurationDays());
        leaveType.setHasBalanceQuota(leaveTypeDetails.getHasBalanceQuota());
        leaveType.setIsPaidLeave(leaveTypeDetails.getIsPaidLeave());
        leaveType.setMaxDurationDays(leaveTypeDetails.getMaxDurationDays());
        leaveType.setRequiresDocument(leaveTypeDetails.getRequiresDocument());
        leaveType.setIsActive(leaveTypeDetails.getIsActive());

        LeaveType updated = leaveTypeRepository.save(leaveType);
        log.info("Updated leave type: {} with code: {}", updated.getName(), updated.getCode());
        return updated;
    }

    @Transactional
    public void deleteLeaveType(Long id) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Leave type not found with id: " + id));

        leaveType.setIsActive(false);
        leaveTypeRepository.save(leaveType);
        log.info("Deactivated leave type: {} with code: {}", leaveType.getName(), leaveType.getCode());
    }
}