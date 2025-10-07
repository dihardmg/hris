package hris.hris.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "annual_leave_balance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnualLeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employee", nullable = false)
    private Employee employee;

    @Column(name = "cuti_tahunan", nullable = false)
    private Integer cutiTahunan;

    @Column(name = "cuti_tahunan_terpakai", nullable = false)
    private Integer cutiTahunanTerpakai;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Calculate remaining annual leave balance
     * @return remaining days (could be negative if used more than quota)
     */
    public int getRemainingBalance() {
        return cutiTahunan - cutiTahunanTerpakai;
    }

    /**
     * Check if employee has sufficient balance for requested days
     * @param requestedDays number of days requested
     * @return true if balance is sufficient
     */
    public boolean hasSufficientBalance(int requestedDays) {
        return getRemainingBalance() >= requestedDays;
    }

    /**
     * Add used days to the balance
     * @param days number of days to deduct
     */
    public void deductBalance(int days) {
        this.cutiTahunanTerpakai += days;
    }

    /**
     * Reset annual balance for new year
     * @param newQuota new annual quota
     */
    public void resetAnnualBalance(int newQuota) {
        this.cutiTahunan = newQuota;
        this.cutiTahunanTerpakai = 0;
    }
}