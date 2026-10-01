package shop.le_paysan.backend.product;

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

/** Cf. docs/03-modele-donnees.md §2.11. */
@Entity
@Table(name = "product_delivery_options")
@Getter
@Setter
@NoArgsConstructor
public class ProductDeliveryOption {

    public static final String MODE_RETRAIT = "RETRAIT_BOUTIQUE";
    public static final String MODE_LIVRAISON = "LIVRAISON_DOMICILE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 20)
    private String mode;

    @Column(name = "pickup_location")
    private String pickupLocation;

    @Column(name = "delivery_price", precision = 12, scale = 2)
    private BigDecimal deliveryPrice;
}
