package shop.le_paysan.backend.livreur;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import shop.le_paysan.backend.common.FileStorageService;
import shop.le_paysan.backend.common.notification.NotificationService;
import shop.le_paysan.backend.livreur.dto.CreateLivreurRequest;
import shop.le_paysan.backend.livreur.dto.LivreurAdminResponse;
import shop.le_paysan.backend.livreur.dto.LivreurResponse;
import shop.le_paysan.backend.livreur.dto.UpdateLivreurRequest;
import shop.le_paysan.backend.order.Order;
import shop.le_paysan.backend.order.OrderRepository;
import shop.le_paysan.backend.shop.Shop;
import shop.le_paysan.backend.user.Role;
import shop.le_paysan.backend.user.RoleRepository;
import shop.le_paysan.backend.user.User;
import shop.le_paysan.backend.user.UserRepository;

@Service
public class LivreurService {

    private final ShopLivreurRepository shopLivreurRepository;
    private final LivreurKycValidationRepository kycValidationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public LivreurService(
            ShopLivreurRepository shopLivreurRepository,
            LivreurKycValidationRepository kycValidationRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            OrderRepository orderRepository,
            PasswordEncoder passwordEncoder,
            FileStorageService fileStorageService,
            NotificationService notificationService) {
        this.shopLivreurRepository = shopLivreurRepository;
        this.kycValidationRepository = kycValidationRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    @Transactional
    public LivreurResponse create(Shop shop, CreateLivreurRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce login est déjà utilisé");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setCity(request.getCity());
        if (request.getPhoto() != null && !request.getPhoto().isEmpty()) {
            user.setPhotoUrl(fileStorageService.store(request.getPhoto(), "livreur-photos"));
        }
        if (request.getIdDocumentFront() != null && !request.getIdDocumentFront().isEmpty()) {
            user.setIdDocumentFrontUrl(fileStorageService.store(request.getIdDocumentFront(), "livreur-kyc"));
        }
        if (request.getIdDocumentBack() != null && !request.getIdDocumentBack().isEmpty()) {
            user.setIdDocumentBackUrl(fileStorageService.store(request.getIdDocumentBack(), "livreur-kyc"));
        }
        if (request.getSelfieWithId() != null && !request.getSelfieWithId().isEmpty()) {
            user.setSelfieWithIdUrl(fileStorageService.store(request.getSelfieWithId(), "livreur-kyc"));
        }
        if (user.getIdDocumentFrontUrl() != null && user.getSelfieWithIdUrl() != null) {
            user.setKycStatus(User.KYC_STATUS_EN_ATTENTE);
        }
        user.setAuthProvider(User.PROVIDER_LOCAL);

        LocalDateTime now = LocalDateTime.now();
        user.setCguAcceptedAt(now);
        user.setCguVersion("1.0");
        user.setCgvAcceptedAt(now);
        user.setCgvVersion("1.0");
        user.setPrivacyAcceptedAt(now);
        user.setPrivacyVersion("1.0");

        Role livreurRole = roleRepository
                .findByName(Role.LIVREUR)
                .orElseThrow(() -> new IllegalStateException("Role ROLE_LIVREUR manquant en base (cf. sql/schema.sql)"));
        user.getRoles().add(livreurRole);
        user = userRepository.save(user);

        ShopLivreur link = new ShopLivreur();
        link.setShop(shop);
        link.setLivreur(user);
        shopLivreurRepository.save(link);

        return LivreurResponse.from(user, link.isAvailable(), 0);
    }

    @Transactional(readOnly = true)
    public List<LivreurResponse> listForShop(Shop shop) {
        return shopLivreurRepository.findByShopIdOrderByCreatedAtDesc(shop.getId()).stream()
                .map(link -> {
                    User livreur = link.getLivreur();
                    long deliveryCount = orderRepository.countByAssignedLivreurIdAndStatus(livreur.getId(), Order.STATUS_LIVREE);
                    return LivreurResponse.from(livreur, link.isAvailable(), deliveryCount);
                })
                .toList();
    }

    @Transactional
    public LivreurResponse setEnabled(Shop shop, Long livreurUserId, boolean enabled) {
        User livreur = requireBelongsToShop(shop, livreurUserId);
        livreur.setEnabled(enabled);
        userRepository.save(livreur);
        return toResponse(livreur);
    }

    @Transactional
    public LivreurResponse update(Shop shop, Long livreurUserId, UpdateLivreurRequest request) {
        User livreur = requireBelongsToShop(shop, livreurUserId);
        livreur.setFirstName(request.getFirstName());
        livreur.setLastName(request.getLastName());
        livreur.setPhone(request.getPhone());
        livreur.setCity(request.getCity());
        if (request.getPhoto() != null && !request.getPhoto().isEmpty()) {
            livreur.setPhotoUrl(fileStorageService.store(request.getPhoto(), "livreur-photos"));
        }
        if (request.getIdDocumentFront() != null && !request.getIdDocumentFront().isEmpty()) {
            livreur.setIdDocumentFrontUrl(fileStorageService.store(request.getIdDocumentFront(), "livreur-kyc"));
        }
        if (request.getIdDocumentBack() != null && !request.getIdDocumentBack().isEmpty()) {
            livreur.setIdDocumentBackUrl(fileStorageService.store(request.getIdDocumentBack(), "livreur-kyc"));
        }
        if (request.getSelfieWithId() != null && !request.getSelfieWithId().isEmpty()) {
            livreur.setSelfieWithIdUrl(fileStorageService.store(request.getSelfieWithId(), "livreur-kyc"));
        }
        if (livreur.getIdDocumentFrontUrl() != null && livreur.getSelfieWithIdUrl() != null
                && !User.KYC_STATUS_VALIDE.equals(livreur.getKycStatus())) {
            livreur.setKycStatus(User.KYC_STATUS_EN_ATTENTE);
        }
        userRepository.save(livreur);
        return toResponse(livreur);
    }

    @Transactional
    public LivreurResponse resetPassword(Shop shop, Long livreurUserId, String newPassword) {
        User livreur = requireBelongsToShop(shop, livreurUserId);
        livreur.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(livreur);
        return toResponse(livreur);
    }

    @Transactional(readOnly = true)
    public LivreurResponse getSelf(User livreur) {
        return toResponse(livreur);
    }

    /** Le livreur declare lui-meme sa disponibilite, visible par le vendeur dans sa liste de livreurs. */
    @Transactional
    public LivreurResponse setAvailability(User livreur, boolean available) {
        ShopLivreur link = shopLivreurRepository
                .findByLivreurId(livreur.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livreur introuvable"));
        link.setAvailable(available);
        shopLivreurRepository.save(link);
        long deliveryCount = orderRepository.countByAssignedLivreurIdAndStatus(livreur.getId(), Order.STATUS_LIVREE);
        return LivreurResponse.from(livreur, available, deliveryCount);
    }

    private LivreurResponse toResponse(User livreur) {
        ShopLivreur link = shopLivreurRepository
                .findByLivreurId(livreur.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livreur introuvable"));
        long deliveryCount = orderRepository.countByAssignedLivreurIdAndStatus(livreur.getId(), Order.STATUS_LIVREE);
        return LivreurResponse.from(livreur, link.isAvailable(), deliveryCount);
    }

    @Transactional(readOnly = true)
    public Page<LivreurAdminResponse> listForAdmin(String kycStatus, Pageable pageable) {
        return shopLivreurRepository.findAllForAdmin(kycStatus, pageable).map(LivreurAdminResponse::from);
    }

    @Transactional(readOnly = true)
    public LivreurAdminResponse getAdminDetail(Long livreurUserId) {
        ShopLivreur link = shopLivreurRepository
                .findByLivreurId(livreurUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livreur introuvable"));
        return LivreurAdminResponse.from(link);
    }

    @Transactional
    public LivreurAdminResponse validateKyc(Long livreurUserId, User admin) {
        ShopLivreur link = shopLivreurRepository
                .findByLivreurId(livreurUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livreur introuvable"));
        User livreur = link.getLivreur();
        if (livreur.getIdDocumentFrontUrl() == null || livreur.getSelfieWithIdUrl() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce livreur n'a pas encore soumis ses documents KYC");
        }
        livreur.setKycStatus(User.KYC_STATUS_VALIDE);
        userRepository.save(livreur);

        LivreurKycValidation validation = new LivreurKycValidation();
        validation.setLivreur(livreur);
        validation.setAdmin(admin);
        validation.setDecision(LivreurKycValidation.DECISION_VALIDE);
        kycValidationRepository.save(validation);

        notificationService.notify(
                livreur,
                "LIVREUR_KYC_VALIDATED",
                "Identité validée",
                "Vos documents d'identité ont été validés par l'administration.",
                "/livreur/parametres");

        return LivreurAdminResponse.from(link);
    }

    @Transactional
    public LivreurAdminResponse rejectKyc(Long livreurUserId, User admin, String reason) {
        ShopLivreur link = shopLivreurRepository
                .findByLivreurId(livreurUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livreur introuvable"));
        User livreur = link.getLivreur();
        livreur.setKycStatus(User.KYC_STATUS_REJETE);
        userRepository.save(livreur);

        LivreurKycValidation validation = new LivreurKycValidation();
        validation.setLivreur(livreur);
        validation.setAdmin(admin);
        validation.setDecision(LivreurKycValidation.DECISION_REJETE);
        validation.setReason(reason);
        kycValidationRepository.save(validation);

        notificationService.notify(
                livreur,
                "LIVREUR_KYC_REJECTED",
                "Identité rejetée",
                "Vos documents d'identité ont été rejetés. Motif : " + reason,
                "/livreur/parametres");

        return LivreurAdminResponse.from(link);
    }

    /** Suspension par l'admin (independante du toggle vendeur, qui ne peut agir que sur ses propres livreurs). */
    @Transactional
    public LivreurAdminResponse suspendByAdmin(Long livreurUserId, String reason) {
        ShopLivreur link = shopLivreurRepository
                .findByLivreurId(livreurUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livreur introuvable"));
        User livreur = link.getLivreur();
        livreur.setEnabled(false);
        userRepository.save(livreur);

        notificationService.notify(
                livreur,
                "LIVREUR_SUSPENDED",
                "Compte suspendu",
                "Votre compte livreur a été suspendu par l'administration. Motif : " + reason,
                "/livreur/parametres");

        return LivreurAdminResponse.from(link);
    }

    @Transactional
    public LivreurAdminResponse restoreByAdmin(Long livreurUserId) {
        ShopLivreur link = shopLivreurRepository
                .findByLivreurId(livreurUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livreur introuvable"));
        User livreur = link.getLivreur();
        if (livreur.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce livreur n'est pas suspendu");
        }
        livreur.setEnabled(true);
        userRepository.save(livreur);

        notificationService.notify(
                livreur,
                "LIVREUR_RESTORED",
                "Compte réactivé",
                "Votre compte livreur a été réactivé par l'administration.",
                "/livreur/parametres");

        return LivreurAdminResponse.from(link);
    }

    /** Verifie que le livreur assigne a une commande appartient bien a la boutique en cours. */
    @Transactional(readOnly = true)
    public User requireBelongsToShop(Shop shop, Long livreurUserId) {
        return shopLivreurRepository
                .findByShopIdAndLivreurId(shop.getId(), livreurUserId)
                .map(ShopLivreur::getLivreur)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce livreur n'appartient pas à votre boutique"));
    }
}
