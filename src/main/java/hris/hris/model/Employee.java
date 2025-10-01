package hris.hris.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String employeeId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "position_id")
    private Long positionId;

    @Column(name = "supervisor_id")
    private Long supervisorId;

    // Temporarily commented out to avoid schema issues
    // @Column(name = "face_template")
    // @Lob
    // private byte[] faceTemplate;

    @Column(name = "hire_date")
    private Date hireDate;

    @Column(name = "employment_status")
    @Enumerated(EnumType.STRING)
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;

    @Column(name = "annual_leave_balance")
    private Integer annualLeaveBalance = 12;

    @Column(name = "sick_leave_balance")
    private Integer sickLeaveBalance = 10;

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (employeeId == null) {
            employeeId = generateEmployeeId();
        }
    }

    private String generateEmployeeId() {
        return "EMP" + System.currentTimeMillis() % 100000;
    }

    public enum EmploymentStatus {
        ACTIVE, ON_LEAVE, TERMINATED, PROBATION
    }
}