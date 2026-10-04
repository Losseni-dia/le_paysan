package shop.le_paysan.backend.order;

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

/** Cf. docs/sql/schema.sql : trace chaque changement de statut pour la frise de suivi de commande. */
@Entity
@Table(name = "order_status_history")
@Getter
@Setter
@NoArgsConstructor
public class OrderStatusHistory {

    public static final String CHANGED_BY_SYSTEME = "SYSTEME";
    public static final String CHANGED_BY_VENDEUR = "VENDEUR";
    public static final String CHANGED_BY_ACHETEUR = "ACHETEUR";
    public static final String CHANGED_BY_LIVREUR = "LIVREUR";

    public static final String PROOF_NONE = "NONE";
    public static final String PROOF_SIGNATURE = "SIGNATURE";
    public static final String PROOF_PHOTO = "PHOTO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, length = 25)
    private String status;

    @Column(name = "changed_by", nullable = false, length = 10)
    private String changedBy;

    @Column
    private String note;

    @Column(name = "proof_type", nullable = false, length = 10)
    private String proofType = PROOF_NONE;

    @Column(name = "proof_url")
    private String proofUrl;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt = LocalDateTime.now();
}
