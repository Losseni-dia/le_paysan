package shop.le_paysan.backend.shop;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Cf. docs/03-modele-donnees.md §2.22. Compte Mobile Money de reversement. */
@Entity
@Table(name = "shop_payout_accounts")
@Getter
@Setter
@NoArgsConstructor
public class ShopPayoutAccount {

    @Id
    @Column(name = "shop_id")
    private Long shopId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @Column(nullable = false, length = 20)
    private String operator;

    @Column(name = "phone_number", nullable = false, length = 30)
    private String phoneNumber;

    @Column(name = "account_holder_name", nullable = false, length = 150)
    private String accountHolderName;

    @Column(nullable = false)
    private boolean verified = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
