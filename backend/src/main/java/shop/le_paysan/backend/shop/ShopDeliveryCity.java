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

/**
 * Ville de livraison declaree explicitement par le vendeur, hors de son rayon de distance normal
 * (ex: une ferme a Samango qui livre aussi a Bouake). Rend le produit visible dans le catalogue
 * pour un acheteur de cette ville et fixe un prix dedie (prioritaire sur les paliers de distance).
 */
@Entity
@Table(name = "shop_delivery_cities")
@Getter
@Setter
@NoArgsConstructor
public class ShopDeliveryCity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "city_name", nullable = false, length = 100)
    private String cityName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
}
