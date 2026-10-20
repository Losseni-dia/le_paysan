package shop.le_paysan.backend.livreur;

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

/** Historique des decisions admin sur le KYC d'un livreur (recto/verso/selfie). */
@Entity
@Table(name = "livreur_kyc_validations")
@Getter
@Setter
@NoArgsConstructor
public class LivreurKycValidation {

    public static final String DECISION_VALIDE = "VALIDE";
    public static final String DECISION_REJETE = "REJETE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "livreur_user_id", nullable = false)
    private User livreur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @Column(nullable = false, length = 20)
    private String decision;

    @Column
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
