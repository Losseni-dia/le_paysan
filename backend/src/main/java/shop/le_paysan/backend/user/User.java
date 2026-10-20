package shop.le_paysan.backend.user;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Cf. docs/03-modele-donnees.md §2.1. */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    public static final String PROVIDER_LOCAL = "LOCAL";
    public static final String PROVIDER_GOOGLE = "GOOGLE";

    public static final String KYC_STATUS_EN_ATTENTE = "EN_ATTENTE";
    public static final String KYC_STATUS_VALIDE = "VALIDE";
    public static final String KYC_STATUS_REJETE = "REJETE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 30)
    private String phone;

    @Column(length = 100)
    private String city;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "id_document_front_url")
    private String idDocumentFrontUrl;

    @Column(name = "id_document_back_url")
    private String idDocumentBackUrl;

    @Column(name = "selfie_with_id_url")
    private String selfieWithIdUrl;

    @Column(name = "kyc_status")
    private String kycStatus;

    @Column(name = "security_selfie_url")
    private String securitySelfieUrl;

    @Column(name = "auth_provider", nullable = false, length = 20)
    private String authProvider = PROVIDER_LOCAL;

    @Column(name = "provider_id")
    private String providerId;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "cgu_accepted_at")
    private LocalDateTime cguAcceptedAt;

    @Column(name = "cgu_version", length = 10)
    private String cguVersion;

    @Column(name = "cgv_accepted_at")
    private LocalDateTime cgvAcceptedAt;

    @Column(name = "cgv_version", length = 10)
    private String cgvVersion;

    @Column(name = "privacy_accepted_at")
    private LocalDateTime privacyAcceptedAt;

    @Column(name = "privacy_version", length = 10)
    private String privacyVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
}
