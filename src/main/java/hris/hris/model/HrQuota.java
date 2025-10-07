package hris.hris.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Year;

@Entity
@Table(name = "hr_quota",
       uniqueConstraints = @UniqueConstraint(columnNames = {"id_employee", "tahun"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HrQuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_employee", nullable = false)
    private Long idEmployee;

    @Column(name = "cuti_tahunan", nullable = false)
    private Integer cutiTahunan;

    @Column(name = "cuti_tahunan_terpakai", nullable = false)
    private Integer cutiTahunanTerpakai;

    @Column(name = "tahun", nullable = false)
    private Year tahun;

    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new java.util.Date();
        updatedAt = new java.util.Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new java.util.Date();
    }

    // Utility methods
    public void tambahCutiTerpakai(Integer hari) {
        // Kurangi cuti tahunan dan tambah cuti terpakai sesuai permintaan user
        this.cutiTahunan = Math.max(0, this.cutiTahunan - hari);
        this.cutiTahunanTerpakai += hari;
    }

    public void kurangiCutiTerpakai(Integer hari) {
        // Kembalikan cuti tahunan dan kurangi cuti terpakai
        this.cutiTahunan += hari;
        this.cutiTahunanTerpakai = Math.max(0, this.cutiTahunanTerpakai - hari);
    }

    public Boolean adaSisaCuti(Integer hari) {
        return cutiTahunan >= hari;
    }
}