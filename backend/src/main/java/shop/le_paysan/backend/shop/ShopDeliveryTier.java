package shop.le_paysan.backend.shop;

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

/** Palier de prix de livraison a domicile selon la distance boutique -> acheteur. */
@Entity
@Table(name = "shop_delivery_tiers")
@Getter
@Setter
@NoArgsConstructor
public class ShopDeliveryTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "max_distance_km", nullable = false)
    private Integer maxDistanceKm;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
}
