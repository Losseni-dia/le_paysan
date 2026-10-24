package shop.le_paysan.backend.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import shop.le_paysan.backend.category.Category;
import shop.le_paysan.backend.shop.Shop;

/** Cf. docs/03-modele-donnees.md §2.9. */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    public static final String STATUS_EN_ATTENTE = "EN_ATTENTE";
    public static final String STATUS_VALIDE = "VALIDE";
    public static final String STATUS_REJETE = "REJETE";
    public static final String STATUS_DEPUBLIE = "DEPUBLIE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 150)
    private String name;

    /** Genere une fois a la creation (slugify(nom) + "-" + slugify(nom boutique)) : stable,
     * jamais regenere si le nom change ensuite. */
    @Column(nullable = false, length = 180, unique = true)
    private String slug;

    @Column
    private String description;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false, length = 20)
    private String status = STATUS_EN_ATTENTE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    /** Verrou optimiste : evite la survente en cas de paiements concurrents sur le meme produit
     * (cf. audit securite, decrementation du stock dans PaymentService.onPaymentApproved). */
    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductDeliveryOption> deliveryOptions = new ArrayList<>();
}
