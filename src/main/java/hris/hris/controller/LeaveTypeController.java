package hris.hris.controller;

import hris.hris.dto.LeaveTypeDto;
import hris.hris.model.LeaveType;
import hris.hris.service.LeaveTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/leave-types")
@CrossOrigin(origins = "*")
@Slf4j
public class LeaveTypeController {

    @Autowired
    private LeaveTypeService leaveTypeService;

    @GetMapping
    public ResponseEntity<List<LeaveTypeDto>> getAllActiveLeaveTypes() {
        List<LeaveType> leaveTypes = leaveTypeService.getAllActiveLeaveTypes();
        List<LeaveTypeDto> leaveTypeDtos = leaveTypes.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(leaveTypeDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveTypeDto> getLeaveType(@PathVariable Long id) {
        return leaveTypeService.getLeaveTypeById(id)
            .map(leaveType -> ResponseEntity.ok(convertToDto(leaveType)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<LeaveTypeDto> createLeaveType(@RequestBody LeaveTypeDto leaveTypeDto) {
        LeaveType leaveType = convertToEntity(leaveTypeDto);
        LeaveType created = leaveTypeService.createLeaveType(leaveType);
        return ResponseEntity.ok(convertToDto(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveTypeDto> updateLeaveType(
            @PathVariable Long id,
            @RequestBody LeaveTypeDto leaveTypeDto) {
        LeaveType leaveTypeDetails = convertToEntity(leaveTypeDto);
        LeaveType updated = leaveTypeService.updateLeaveType(id, leaveTypeDetails);
        return ResponseEntity.ok(convertToDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeaveType(@PathVariable Long id) {
        leaveTypeService.deleteLeaveType(id);
        return ResponseEntity.noContent().build();
    }

    private LeaveTypeDto convertToDto(LeaveType leaveType) {
        LeaveTypeDto dto = new LeaveTypeDto();
        dto.setId(leaveType.getId());
        dto.setCode(leaveType.getCode());
        dto.setName(leaveType.getName());
        dto.setDescription(leaveType.getDescription());
        dto.setMinDurationDays(leaveType.getMinDurationDays());
        dto.setHasBalanceQuota(leaveType.getHasBalanceQuota());
        dto.setIsPaidLeave(leaveType.getIsPaidLeave());
        dto.setMaxDurationDays(leaveType.getMaxDurationDays());
        dto.setRequiresDocument(leaveType.getRequiresDocument());
        dto.setIsActive(leaveType.getIsActive());
        return dto;
    }

    private LeaveType convertToEntity(LeaveTypeDto dto) {
        LeaveType leaveType = new LeaveType();
        leaveType.setCode(dto.getCode());
        leaveType.setName(dto.getName());
        leaveType.setDescription(dto.getDescription());
        leaveType.setMinDurationDays(dto.getMinDurationDays());
        leaveType.setHasBalanceQuota(dto.getHasBalanceQuota());
        leaveType.setIsPaidLeave(dto.getIsPaidLeave());
        leaveType.setMaxDurationDays(dto.getMaxDurationDays());
        leaveType.setRequiresDocument(dto.getRequiresDocument());
        leaveType.setIsActive(dto.getIsActive());
        return leaveType;
    }
}