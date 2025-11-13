package org.esfe.modelos;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "loans")
public class Loans {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne
    @JoinColumn(name = "laptop_id")
    @ToString.Exclude
    private Laptop laptop;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Min(1)
    @Column(name = "requested_hours")
    private Integer requestedHours;

    @NotBlank
    @Pattern(regexp = "pending|approved|rejected|active|completed")
    @Column(nullable = true)
    private String status;

    @Column(name = "terms_accepted")
    private Boolean termsAccepted = false;

    // --- Getters/Setters y helpers adicionales (Lombok @Data ya genera los básicos) ---

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getRequestedHours() { return requestedHours; }
    public void setRequestedHours(Integer requestedHours) { this.requestedHours = requestedHours; }

    public Boolean getTermsAccepted() { return termsAccepted; }
    public void setTermsAccepted(Boolean termsAccepted) { this.termsAccepted = termsAccepted; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Laptop getLaptop() { return laptop; }
    public void setLaptop(Laptop laptop) { this.laptop = laptop; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }

    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }

    // Conveniencia para vistas
    public String getUserFullName() { return (user != null) ? user.getFullName() : ""; }
    public String getUserEmail() { return (user != null) ? user.getEmail() : ""; }
    public String getUserStudentId() { return (user != null) ? user.getStudentId() : ""; }
}
