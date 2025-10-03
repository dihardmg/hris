package hris.hris.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "hashed_password", nullable = false)
    private String hashedPassword;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    // Constructor untuk employeeId dan password
    public PasswordHistory(Long employeeId, String hashedPassword) {
        this.employeeId = employeeId;
        this.hashedPassword = hashedPassword;
    }
}