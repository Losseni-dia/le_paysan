package shop.le_paysan.backend.auth;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import shop.le_paysan.backend.user.User;

/** Cf. docs/03-modele-donnees.md §2.21 et docs/02-specifications-fonctionnelles.md UC-01b. */
@Entity
@Table(name = "account_recovery_requests")
@Getter
@Setter
@NoArgsConstructor
public class AccountRecoveryRequest {

    public static final String STATUS_NOUVELLE = "NOUVELLE";
    public static final String STATUS_EN_COURS = "EN_COURS";
    public static final String STATUS_APPROUVEE = "APPROUVEE";
    public static final String STATUS_REJETEE = "REJETEE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "submitted_selfie_url", nullable = false)
    private String submittedSelfieUrl;

    @Column(nullable = false, length = 15)
    private String status = STATUS_NOUVELLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_admin_id")
    private User reviewedByAdmin;

    @Column(name = "review_reason")
    private String reviewReason;

    @Column(name = "temporary_password_issued_at")
    private LocalDateTime temporaryPasswordIssuedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}
