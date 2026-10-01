package shop.le_paysan.backend.commission;

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

import shop.le_paysan.backend.category.Category;

/**
 * Cf. docs/03-modele-donnees.md §2.17. Pour le MVP (Phase 4), seul le taux par defaut
 * (category = null) est utilise : une commande peut regrouper des produits de plusieurs
 * categories, le modele actuel ne porte qu'un taux unique par commande.
 */
@Entity
@Table(name = "commission_settings")
@Getter
@Setter
@NoArgsConstructor
public class CommissionSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal rate;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
