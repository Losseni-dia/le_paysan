package shop.le_paysan.backend.order;

import java.math.BigDecimal;

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

import shop.le_paysan.backend.product.Product;

/** Cf. docs/03-modele-donnees.md §2.15. Snapshot du nom/prix au moment de la commande. */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_name_snapshot", nullable = false, length = 150)
    private String productNameSnapshot;

    @Column(name = "unit_price_snapshot", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPriceSnapshot;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "delivery_mode", nullable = false, length = 20)
    private String deliveryMode;

    @Column(name = "delivery_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal deliveryPrice = BigDecimal.ZERO;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    /** Copie des coordonnees du panier (cart_items) : necessaire pour matcher les livreurs
     * disponibles a proximite lors de la diffusion de la demande de livraison. */
    @Column(name = "delivery_city_name")
    private String deliveryCityName;

    @Column(name = "delivery_city_lat")
    private BigDecimal deliveryCityLat;

    @Column(name = "delivery_city_lng")
    private BigDecimal deliveryCityLng;
}
