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

import shop.le_paysan.backend.shop.Shop;
import shop.le_paysan.backend.user.User;

/** Rattachement d'un compte ROLE_LIVREUR a la boutique qui l'a cree. */
@Entity
@Table(name = "shop_livreurs")
@Getter
@Setter
@NoArgsConstructor
public class ShopLivreur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "livreur_user_id", nullable = false)
    private User livreur;

    @Column(nullable = false)
    private boolean available = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
