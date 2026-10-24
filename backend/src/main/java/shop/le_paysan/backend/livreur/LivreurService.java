package shop.le_paysan.backend.livreur;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import shop.le_paysan.backend.common.FileStorageService;
import shop.le_paysan.backend.common.notification.NotificationService;
import shop.le_paysan.backend.livreur.dto.LivreurAdminResponse;
import shop.le_paysan.backend.livreur.dto.LivreurResponse;
import shop.le_paysan.backend.order.Order;
import shop.le_paysan.backend.order.OrderRepository;
import shop.le_paysan.backend.user.Role;
import shop.le_paysan.backend.user.User;
import shop.le_paysan.backend.user.UserRepository;

/** Le livreur est independant (inscription volontaire, cf. AuthService.registerLivreur) :
 * ce service ne gere plus que son propre profil, sa disponibilite et sa validation KYC. */
@Service
public class LivreurService {

    private final UserRepository userRepository;
    private final LivreurKycValidationRepository kycValidationRepository;
    private final OrderRepository orderRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public LivreurService(
            UserRepository userRepository,
            LivreurKycValidationRepository kycValidationRepository,
            OrderRepository orderRepository,
            FileStorageService fileStorageService,
            NotificationService notificationService) {
        this.userRepository = userRepository;
        this.kycValidationRepository = kycValidationRepository;
        this.orderRepository = orderRepository;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public LivreurResponse getSelf(User livreur) {
        return toResponse(livreur);
    }

    @Transactional
    public LivreurResponse setAvailability(User livreur, boolean available) {
        livreur.setAvailable(available);
        userRepository.save(livreur);
        return toResponse(livreur);
    }

    /** Soumission/remplacement des documents KYC par le livreur lui-meme (auto-service). */
    @Transactional
    public LivreurResponse uploadDocuments(
            User livreur, MultipartFile photo, MultipartFile idDocumentFront, MultipartFile idDocumentBack,
            MultipartFile selfieWithId) {
        if (photo != null && !photo.isEmpty()) {
            livreur.setPhotoUrl(fileStorageService.store(photo, "livreur-photos"));
        }
        if (idDocumentFront != null && !idDocumentFront.isEmpty()) {
            livreur.setIdDocumentFrontUrl(fileStorageService.store(idDocumentFront, "livreur-kyc"));
        }
        if (idDocumentBack != null && !idDocumentBack.isEmpty()) {
            livreur.setIdDocumentBackUrl(fileStorageService.store(idDocumentBack, "livreur-kyc"));
        }
        if (selfieWithId != null && !selfieWithId.isEmpty()) {
            livreur.setSelfieWithIdUrl(fileStorageService.store(selfieWithId, "livreur-kyc"));
        }
        if (livreur.getIdDocumentFrontUrl() != null && livreur.getSelfieWithIdUrl() != null
                && !User.KYC_STATUS_VALIDE.equals(livreur.getKycStatus())) {
            livreur.setKycStatus(User.KYC_STATUS_EN_ATTENTE);
        }
        userRepository.save(livreur);
        return toResponse(livreur);
    }

    private LivreurResponse toResponse(User livreur) {
        long deliveryCount = orderRepository.countByAssignedLivreurIdAndStatus(livreur.getId(), Order.STATUS_LIVREE);
        return LivreurResponse.from(livreur, livreur.isAvailable(), deliveryCount);
    }

    @Transactional(readOnly = true)
    public Page<LivreurAdminResponse> listForAdmin(String kycStatus, Pageable pageable) {
        List<User> livreurs = userRepository.findByRoles_Name(Role.LIVREUR).stream()
                .filter(u -> kycStatus == null || kycStatus.equals(u.getKycStatus()))
                .sorted(java.util.Comparator.comparing(User::getCreatedAt).reversed())
                .toList();
        int start = Math.min((int) pageable.getOffset(), livreurs.size());
        int end = Math.min(start + pageable.getPageSize(), livreurs.size());
        List<LivreurAdminResponse> page = livreurs.subList(start, end).stream().map(LivreurAdminResponse::from).toList();
        return new org.springframework.data.domain.PageImpl<>(page, pageable, livreurs.size());
    }

    @Transactional(readOnly = true)
    public LivreurAdminResponse getAdminDetail(Long livreurUserId) {
        return LivreurAdminResponse.from(requireLivreur(livreurUserId));
    }

    @Transactional
    public LivreurAdminResponse validateKyc(Long livreurUserId, User admin) {
        User livreur = requireLivreur(livreurUserId);
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

        return LivreurAdminResponse.from(livreur);
    }

    @Transactional
    public LivreurAdminResponse rejectKyc(Long livreurUserId, User admin, String reason) {
        User livreur = requireLivreur(livreurUserId);
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

        return LivreurAdminResponse.from(livreur);
    }

    @Transactional
    public LivreurAdminResponse suspendByAdmin(Long livreurUserId, String reason) {
        User livreur = requireLivreur(livreurUserId);
        livreur.setEnabled(false);
        userRepository.save(livreur);

        notificationService.notify(
                livreur,
                "LIVREUR_SUSPENDED",
                "Compte suspendu",
                "Votre compte livreur a été suspendu par l'administration. Motif : " + reason,
                "/livreur/parametres");

        return LivreurAdminResponse.from(livreur);
    }

    @Transactional
    public LivreurAdminResponse restoreByAdmin(Long livreurUserId) {
        User livreur = requireLivreur(livreurUserId);
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

        return LivreurAdminResponse.from(livreur);
    }

    private User requireLivreur(Long livreurUserId) {
        User user = userRepository
                .findById(livreurUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livreur introuvable"));
        if (user.getRoles().stream().noneMatch(r -> Role.LIVREUR.equals(r.getName()))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Livreur introuvable");
        }
        return user;
    }
}
