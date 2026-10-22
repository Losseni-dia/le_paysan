package shop.le_paysan.backend.shop;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import shop.le_paysan.backend.common.FileStorageService;
import shop.le_paysan.backend.common.notification.NotificationService;
import shop.le_paysan.backend.product.Product;
import shop.le_paysan.backend.product.ProductRepository;
import shop.le_paysan.backend.shop.dto.PayoutAccountRequest;
import shop.le_paysan.backend.shop.dto.PayoutAccountResponse;
import shop.le_paysan.backend.shop.dto.ShopAdminDetailResponse;
import shop.le_paysan.backend.shop.dto.ShopRequestForm;
import shop.le_paysan.backend.shop.dto.ShopResponse;
import shop.le_paysan.backend.shop.dto.UpdateShopRequest;
import shop.le_paysan.backend.user.Role;
import shop.le_paysan.backend.user.RoleRepository;
import shop.le_paysan.backend.user.User;
import shop.le_paysan.backend.user.UserRepository;

@Service
public class ShopService {

    private static final String LEGAL_DOCS_VERSION = "1.0";
    private static final String FILE_CATEGORY = "shop-kyc";
    // Logo et photo de couverture sont affiches publiquement sur la fiche boutique :
    // categorie distincte des justificatifs KYC (qui restent authentifies).
    private static final String PUBLIC_FILE_CATEGORY = "shop-public";

    private final ShopRepository shopRepository;
    private final ShopValidationRepository shopValidationRepository;
    private final ShopPayoutAccountRepository payoutAccountRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public ShopService(
            ShopRepository shopRepository,
            ShopValidationRepository shopValidationRepository,
            ShopPayoutAccountRepository payoutAccountRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            FileStorageService fileStorageService,
            NotificationService notificationService) {
        this.shopRepository = shopRepository;
        this.shopValidationRepository = shopValidationRepository;
        this.payoutAccountRepository = payoutAccountRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ShopResponse submitRequest(User user, ShopRequestForm form) {
        if (shopRepository.existsByOwnerId(user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vous avez déjà une boutique");
        }
        if (form.getName() == null || form.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nom de la boutique est obligatoire");
        }
        if (shopRepository.existsByName(form.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce nom de boutique est déjà pris");
        }
        requireNotBlank(form.getAddressCity(), "La ville est obligatoire");
        requireNotBlank(form.getAddressDetail(), "L'adresse détaillée est obligatoire");
        requireNotBlank(form.getPhone(), "Le téléphone est obligatoire");
        if (form.getLatitude() == null || form.getLongitude() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La localisation sur carte est obligatoire");
        }
        if (form.getLogo() == null || form.getLogo().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le logo est obligatoire");
        }
        if (!form.isAcceptCgv()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous devez accepter les Conditions vendeur");
        }

        Shop shop = new Shop();
        shop.setOwner(user);
        shop.setOwnerType(form.getOwnerType());
        shop.setName(form.getName());
        shop.setDescription(form.getDescription());
        shop.setCategory(form.getCategory());
        shop.setAddressCity(form.getAddressCity());
        shop.setAddressDetail(form.getAddressDetail());
        shop.setLatitude(form.getLatitude());
        shop.setLongitude(form.getLongitude());
        shop.setPhone(form.getPhone());
        shop.setLogoUrl(fileStorageService.store(form.getLogo(), PUBLIC_FILE_CATEGORY));
        if (form.getCover() != null && !form.getCover().isEmpty()) {
            shop.setCoverUrl(fileStorageService.store(form.getCover(), PUBLIC_FILE_CATEGORY));
        }
        shop.setStatus(Shop.STATUS_EN_ATTENTE);
        shop.setCgvVendeurAcceptedAt(LocalDateTime.now());
        shop.setCgvVendeurVersion(LEGAL_DOCS_VERSION);

        switch (form.getOwnerType() == null ? "" : form.getOwnerType()) {
            case Shop.TYPE_PHYSIQUE -> shop.setIndividualProfile(buildIndividualProfile(shop, form));
            case Shop.TYPE_ENTREPRENANT -> shop.setEntreprenantProfile(buildEntreprenantProfile(shop, form));
            case Shop.TYPE_MORALE -> shop.setCompanyProfile(buildCompanyProfile(shop, form));
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type de détenteur invalide");
        }

        shop = shopRepository.save(shop);

        notificationService.notify(
                user,
                "SHOP_SUBMITTED",
                "Demande de boutique envoyée",
                "Votre demande de boutique \"" + shop.getName() + "\" a été envoyée et est en attente de validation.",
                "/vendeur/ma-boutique");

        return ShopResponse.from(shop);
    }

    private ShopIndividualProfile buildIndividualProfile(Shop shop, ShopRequestForm form) {
        requireNotBlank(form.getFirstName(), "Le prénom est obligatoire");
        requireNotBlank(form.getLastName(), "Le nom est obligatoire");
        requireField(form.getBirthDate() != null, "La date de naissance est obligatoire");
        requireNotBlank(form.getIdDocumentType(), "Le type de pièce d'identité est obligatoire");
        requireNotBlank(form.getIdDocumentNumber(), "Le numéro de pièce est obligatoire");
        requireFile(form.getIdDocumentFront(), "La photo recto de la pièce est obligatoire");
        requireFile(form.getSelfieWithId(), "Le selfie avec la pièce d'identité est obligatoire");

        ShopIndividualProfile profile = new ShopIndividualProfile();
        profile.setShop(shop);
        profile.setFirstName(form.getFirstName());
        profile.setLastName(form.getLastName());
        profile.setBirthDate(form.getBirthDate());
        profile.setIdDocumentType(form.getIdDocumentType());
        profile.setIdDocumentNumber(form.getIdDocumentNumber());
        profile.setIdDocumentFrontUrl(fileStorageService.store(form.getIdDocumentFront(), FILE_CATEGORY));
        if (form.getIdDocumentBack() != null && !form.getIdDocumentBack().isEmpty()) {
            profile.setIdDocumentBackUrl(fileStorageService.store(form.getIdDocumentBack(), FILE_CATEGORY));
        }
        profile.setSelfieWithIdUrl(fileStorageService.store(form.getSelfieWithId(), FILE_CATEGORY));
        return profile;
    }

    private ShopEntreprenantProfile buildEntreprenantProfile(Shop shop, ShopRequestForm form) {
        requireNotBlank(form.getFirstName(), "Le prénom est obligatoire");
        requireNotBlank(form.getLastName(), "Le nom est obligatoire");
        requireField(form.getBirthDate() != null, "La date de naissance est obligatoire");
        requireNotBlank(form.getIdDocumentType(), "Le type de pièce d'identité est obligatoire");
        requireNotBlank(form.getIdDocumentNumber(), "Le numéro de pièce est obligatoire");
        requireFile(form.getIdDocumentFront(), "La photo recto de la pièce est obligatoire");
        requireFile(form.getSelfieWithId(), "Le selfie avec la pièce d'identité est obligatoire");
        requireNotBlank(form.getRecepisseNumber(), "Le numéro de récépissé est obligatoire");
        requireField(form.getRecepisseDate() != null, "La date de récépissé est obligatoire");
        requireFile(form.getRecepisseDocument(), "Le document de récépissé est obligatoire");

        ShopEntreprenantProfile profile = new ShopEntreprenantProfile();
        profile.setShop(shop);
        profile.setFirstName(form.getFirstName());
        profile.setLastName(form.getLastName());
        profile.setBirthDate(form.getBirthDate());
        profile.setIdDocumentType(form.getIdDocumentType());
        profile.setIdDocumentNumber(form.getIdDocumentNumber());
        profile.setIdDocumentFrontUrl(fileStorageService.store(form.getIdDocumentFront(), FILE_CATEGORY));
        if (form.getIdDocumentBack() != null && !form.getIdDocumentBack().isEmpty()) {
            profile.setIdDocumentBackUrl(fileStorageService.store(form.getIdDocumentBack(), FILE_CATEGORY));
        }
        profile.setSelfieWithIdUrl(fileStorageService.store(form.getSelfieWithId(), FILE_CATEGORY));
        profile.setRecepisseNumber(form.getRecepisseNumber());
        profile.setRecepisseDate(form.getRecepisseDate());
        profile.setRecepisseDocumentUrl(fileStorageService.store(form.getRecepisseDocument(), FILE_CATEGORY));
        return profile;
    }

    private ShopCompanyProfile buildCompanyProfile(Shop shop, ShopRequestForm form) {
        requireNotBlank(form.getCompanyName(), "La raison sociale est obligatoire");
        requireNotBlank(form.getLegalForm(), "La forme juridique est obligatoire");
        requireNotBlank(form.getRccmNumber(), "Le numéro RCCM est obligatoire");
        requireNotBlank(form.getNccNumber(), "Le NCC est obligatoire");
        requireFile(form.getLegalDocument(), "Le document RCCM/statuts est obligatoire");
        requireNotBlank(form.getLegalRepName(), "Le nom du représentant légal est obligatoire");
        requireNotBlank(form.getLegalRepRole(), "La fonction du représentant légal est obligatoire");
        requireNotBlank(form.getLegalRepIdDocumentType(), "Le type de pièce du représentant légal est obligatoire");
        requireNotBlank(form.getLegalRepIdDocumentNumber(), "Le numéro de pièce du représentant légal est obligatoire");
        requireFile(form.getLegalRepIdDocumentFront(), "La photo recto de la pièce du représentant légal est obligatoire");
        requireFile(form.getLegalRepSelfieWithId(), "Le selfie du représentant légal avec sa pièce est obligatoire");

        ShopCompanyProfile profile = new ShopCompanyProfile();
        profile.setShop(shop);
        profile.setCompanyName(form.getCompanyName());
        profile.setLegalForm(form.getLegalForm());
        profile.setRccmNumber(form.getRccmNumber());
        profile.setNccNumber(form.getNccNumber());
        profile.setLegalDocumentUrl(fileStorageService.store(form.getLegalDocument(), FILE_CATEGORY));
        profile.setLegalRepName(form.getLegalRepName());
        profile.setLegalRepRole(form.getLegalRepRole());
        profile.setLegalRepIdDocumentType(form.getLegalRepIdDocumentType());
        profile.setLegalRepIdDocumentNumber(form.getLegalRepIdDocumentNumber());
        profile.setLegalRepIdDocumentFrontUrl(fileStorageService.store(form.getLegalRepIdDocumentFront(), FILE_CATEGORY));
        if (form.getLegalRepIdDocumentBack() != null && !form.getLegalRepIdDocumentBack().isEmpty()) {
            profile.setLegalRepIdDocumentBackUrl(fileStorageService.store(form.getLegalRepIdDocumentBack(), FILE_CATEGORY));
        }
        profile.setLegalRepSelfieWithIdUrl(fileStorageService.store(form.getLegalRepSelfieWithId(), FILE_CATEGORY));
        return profile;
    }

    @Transactional(readOnly = true)
    public ShopResponse getMyShop(Long userId) {
        return ShopResponse.from(findByOwnerOrThrow(userId));
    }

    @Transactional
    public ShopResponse updateMyShop(Long userId, UpdateShopRequest request) {
        Shop shop = findByOwnerOrThrow(userId);
        shop.setDescription(request.description());
        shop.setCategory(request.category());
        shop.setAddressCity(request.addressCity());
        shop.setAddressDetail(request.addressDetail());
        shop.setLatitude(request.latitude());
        shop.setLongitude(request.longitude());
        shop.setPhone(request.phone());
        shop.setUpdatedAt(LocalDateTime.now());
        return ShopResponse.from(shopRepository.save(shop));
    }

    @Transactional(readOnly = true)
    public ShopResponse getPublic(Long id) {
        Shop shop = shopRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!Shop.STATUS_VALIDEE.equals(shop.getStatus())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ShopResponse.from(shop);
    }

    @Transactional(readOnly = true)
    public Page<ShopResponse> listPublic(String search, Pageable pageable) {
        Page<Shop> page = (search == null || search.isBlank())
                ? shopRepository.findByStatus(Shop.STATUS_VALIDEE, pageable)
                : shopRepository.findByStatusAndNameContainingIgnoreCase(Shop.STATUS_VALIDEE, search, pageable);
        return page.map(ShopResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ShopAdminDetailResponse> listForAdmin(String status, Pageable pageable) {
        Page<Shop> page = status != null ? shopRepository.findByStatus(status, pageable) : shopRepository.findAll(pageable);
        return page.map(ShopAdminDetailResponse::from);
    }

    @Transactional(readOnly = true)
    public ShopAdminDetailResponse getAdminDetail(Long id) {
        return ShopAdminDetailResponse.from(findOrThrow(id));
    }

    @Transactional
    public ShopResponse validate(Long id, User admin) {
        Shop shop = findOrThrow(id);
        shop.setStatus(Shop.STATUS_VALIDEE);
        shop.setUpdatedAt(LocalDateTime.now());
        shopRepository.save(shop);

        ShopValidation validation = new ShopValidation();
        validation.setShop(shop);
        validation.setAdmin(admin);
        validation.setDecision(ShopValidation.DECISION_VALIDEE);
        shopValidationRepository.save(validation);

        User owner = shop.getOwner();
        Role vendeurRole = roleRepository
                .findByName(Role.VENDEUR)
                .orElseThrow(() -> new IllegalStateException("Role ROLE_VENDEUR manquant en base"));
        if (!owner.getRoles().contains(vendeurRole)) {
            owner.getRoles().add(vendeurRole);
            userRepository.save(owner);
        }

        notificationService.notify(
                owner,
                "SHOP_VALIDATED",
                "Boutique validée !",
                "Votre boutique \"" + shop.getName() + "\" a été validée. Vous pouvez maintenant publier des produits.",
                "/vendeur/ma-boutique");

        return ShopResponse.from(shop);
    }

    @Transactional
    public ShopResponse reject(Long id, User admin, String reason) {
        Shop shop = findOrThrow(id);
        shop.setStatus(Shop.STATUS_REJETEE);
        shop.setUpdatedAt(LocalDateTime.now());
        shopRepository.save(shop);

        ShopValidation validation = new ShopValidation();
        validation.setShop(shop);
        validation.setAdmin(admin);
        validation.setDecision(ShopValidation.DECISION_REJETEE);
        validation.setReason(reason);
        shopValidationRepository.save(validation);

        notificationService.notify(
                shop.getOwner(),
                "SHOP_REJECTED",
                "Demande de boutique rejetée",
                "Votre demande de boutique \"" + shop.getName() + "\" a été rejetée. Motif : " + reason,
                "/vendeur/ma-boutique");

        return ShopResponse.from(shop);
    }

    @Transactional
    public ShopResponse suspend(Long id, String reason) {
        Shop shop = findOrThrow(id);
        shop.setStatus(Shop.STATUS_SUSPENDUE);
        shop.setUpdatedAt(LocalDateTime.now());
        shopRepository.save(shop);

        // La suspension de la boutique dépublie explicitement ses produits visibles : le
        // catalogue public les exclut déjà via le statut boutique, mais sans ça le vendeur
        // et l'admin continueraient de les voir "Publié" dans leurs listes respectives.
        List<Product> visibleProducts = productRepository.findByShopIdAndStatus(shop.getId(), Product.STATUS_VALIDE);
        for (Product product : visibleProducts) {
            product.setStatus(Product.STATUS_DEPUBLIE);
            product.setUpdatedAt(LocalDateTime.now());
        }
        productRepository.saveAll(visibleProducts);

        notificationService.notify(
                shop.getOwner(),
                "SHOP_SUSPENDED",
                "Boutique suspendue",
                "Votre boutique \"" + shop.getName() + "\" a été suspendue par l'administration. Motif : " + reason
                        + ". Ses produits ont été retirés du catalogue.",
                "/vendeur/ma-boutique");

        return ShopResponse.from(shop);
    }

    /**
     * Cf. demande admin : pouvoir lever une suspension. Les produits dépubliés lors de la
     * suspension restent dépubliés (le vendeur les republie lui-meme via "Mes produits") :
     * on ne peut pas distinguer ceux deja dépubliés manuellement avant la suspension.
     */
    @Transactional
    public ShopResponse restore(Long id) {
        Shop shop = findOrThrow(id);
        if (!Shop.STATUS_SUSPENDUE.equals(shop.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette boutique n'est pas suspendue");
        }
        shop.setStatus(Shop.STATUS_VALIDEE);
        shop.setUpdatedAt(LocalDateTime.now());
        shopRepository.save(shop);

        notificationService.notify(
                shop.getOwner(),
                "SHOP_RESTORED",
                "Boutique réactivée",
                "Votre boutique \"" + shop.getName() + "\" a été réactivée par l'administration.",
                "/vendeur/ma-boutique");

        return ShopResponse.from(shop);
    }

    @Transactional(readOnly = true)
    public PayoutAccountResponse getPayoutAccount(Long userId) {
        Shop shop = findByOwnerOrThrow(userId);
        ShopPayoutAccount account = payoutAccountRepository
                .findById(shop.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucun compte de reversement"));
        return PayoutAccountResponse.from(account);
    }

    @Transactional
    public PayoutAccountResponse upsertPayoutAccount(Long userId, PayoutAccountRequest request) {
        Shop shop = findByOwnerOrThrow(userId);
        ShopPayoutAccount account = payoutAccountRepository.findById(shop.getId()).orElseGet(() -> {
            ShopPayoutAccount a = new ShopPayoutAccount();
            a.setShop(shop);
            return a;
        });
        account.setOperator(request.operator());
        account.setPhoneNumber(request.phoneNumber());
        account.setAccountHolderName(request.accountHolderName());
        account.setUpdatedAt(LocalDateTime.now());
        return PayoutAccountResponse.from(payoutAccountRepository.save(account));
    }

    private Shop findByOwnerOrThrow(Long userId) {
        return shopRepository
                .findByOwnerId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucune boutique"));
    }

    private Shop findOrThrow(Long id) {
        return shopRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private static void requireField(boolean condition, String message) {
        if (!condition) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private static void requireFile(org.springframework.web.multipart.MultipartFile file, String message) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
