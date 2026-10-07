package shop.le_paysan.backend.review;

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

import shop.le_paysan.backend.order.OrderItem;
import shop.le_paysan.backend.product.Product;
import shop.le_paysan.backend.user.User;

/** Cf. docs/02-specifications-fonctionnelles.md UC-11 : un seul avis par (acheteur, produit), modifiable. */
@Entity
@Table(name = "product_reviews")
@Getter
@Setter
@NoArgsConstructor
public class ProductReview {

    public static final String STATUS_VISIBLE = "VISIBLE";
    public static final String STATUS_MASQUE = "MASQUE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_user_id", nullable = false)
    private User buyerUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(nullable = false)
    private Integer rating;

    @Column
    private String comment;

    @Column(nullable = false, length = 10)
    private String status = STATUS_VISIBLE;

    @Column(name = "moderation_reason")
    private String moderationReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
