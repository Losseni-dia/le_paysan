package shop.le_paysan.backend.shop;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import shop.le_paysan.backend.user.User;

/** Cf. docs/03-modele-donnees.md §2.3. */
@Entity
@Table(name = "shops")
@Getter
@Setter
@NoArgsConstructor
public class Shop {

    public static final String TYPE_PHYSIQUE = "PHYSIQUE";
    public static final String TYPE_ENTREPRENANT = "ENTREPRENANT";
    public static final String TYPE_MORALE = "MORALE";

    public static final String STATUS_EN_ATTENTE = "EN_ATTENTE";
    public static final String STATUS_VALIDEE = "VALIDEE";
    public static final String STATUS_REJETEE = "REJETEE";
    public static final String STATUS_SUSPENDUE = "SUSPENDUE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false, unique = true)
    private User owner;

    @Column(name = "owner_type", nullable = false, length = 15)
    private String ownerType;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column
    private String description;

    @Column(length = 100)
    private String category;

    @Column(name = "address_city", nullable = false, length = 100)
    private String addressCity;

    @Column(name = "address_detail", nullable = false)
    private String addressDetail;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(nullable = false, length = 20)
    private String status = STATUS_EN_ATTENTE;

    @Column(name = "cgv_vendeur_accepted_at")
    private LocalDateTime cgvVendeurAcceptedAt;

    @Column(name = "cgv_vendeur_version", length = 10)
    private String cgvVendeurVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToOne(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ShopIndividualProfile individualProfile;

    @OneToOne(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ShopEntreprenantProfile entreprenantProfile;

    @OneToOne(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ShopCompanyProfile companyProfile;

    @OneToOne(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ShopPayoutAccount payoutAccount;
}
