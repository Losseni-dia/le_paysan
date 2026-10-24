package shop.le_paysan.backend.delivery;

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
import shop.le_paysan.backend.user.User;

/**
 * Diffusion d'une commande a livrer vers un livreur disponible a proximite. Cf. demande
 * utilisateur : le vendeur diffuse a plusieurs livreurs, le premier qui accepte devient le
 * livreur assigne (Order.assignedLivreur), les demandes des autres passent a EXPIREE.
 */
@Entity
@Table(name = "delivery_requests")
@Getter
@Setter
@NoArgsConstructor
public class DeliveryRequest {

    public static final String STATUS_EN_ATTENTE = "EN_ATTENTE";
    public static final String STATUS_ACCEPTEE = "ACCEPTEE";
    public static final String STATUS_EXPIREE = "EXPIREE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "livreur_user_id", nullable = false)
    private User livreur;

    @Column(nullable = false, length = 20)
    private String status = STATUS_EN_ATTENTE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
}
