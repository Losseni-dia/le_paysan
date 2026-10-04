package shop.le_paysan.backend.payment;

import java.math.BigDecimal;
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

import shop.le_paysan.backend.order.Order;
import shop.le_paysan.backend.shop.Shop;

/** Cf. docs/03-modele-donnees.md §2.23. Reversement automatique au vendeur, par commande payée. */
@Entity
@Table(name = "vendor_payouts")
@Getter
@Setter
@NoArgsConstructor
public class VendorPayout {

    public static final String STATUS_EN_ATTENTE = "EN_ATTENTE";
    public static final String STATUS_ENVOYE = "ENVOYE";
    public static final String STATUS_ECHEC = "ECHEC";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 15)
    private String status = STATUS_EN_ATTENTE;

    @Column(name = "fedapay_payout_id", length = 100)
    private String fedapayPayoutId;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
