package hris.hris.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "leave_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "min_duration_days")
    private Integer minDurationDays;

    @Column(name = "has_balance_quota", nullable = false)
    private Boolean hasBalanceQuota = false;

    @Column(name = "is_paid_leave", nullable = false)
    private Boolean isPaidLeave = false;

    @Column(name = "max_duration_days")
    private Integer maxDurationDays;

    @Column(name = "requires_document", nullable = false)
    private Boolean requiresDocument = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}