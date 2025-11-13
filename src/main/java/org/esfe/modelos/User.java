package org.esfe.modelos;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank @Size(max = 100)
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Email @NotBlank @Size(max = 100)
    @Column(nullable = false, unique = true)
    private String email;

    @Size(max = 20) @Column(name = "student_id")
    private String studentId;

    @Size(max = 100)
    private String career;

    @Size(max = 20) @Column(name = "national_id")
    private String nationalId;

    @Size(max = 255) @Column(name = "password_hash")
    private String passwordHash;

    @NotBlank
    @Pattern(regexp = "(?i)student|admin|superadmin") // <-- case-insensitive
    @Column(nullable = false)
    private String role;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user") @ToString.Exclude
    private List<Loans> loans;

    @OneToMany(mappedBy = "user") @ToString.Exclude
    private List<Penalty> penalties;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCareer() {
        return career;
    }

    public void setCareer(String career) {
        this.career = career;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
