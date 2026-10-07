package shop.le_paysan.backend.product;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import shop.le_paysan.backend.category.Category;
import shop.le_paysan.backend.category.CategoryRepository;
import shop.le_paysan.backend.common.FileStorageService;
import shop.le_paysan.backend.common.notification.NotificationService;
import shop.le_paysan.backend.product.dto.ProductCreateForm;
import shop.le_paysan.backend.product.dto.ProductResponse;
import shop.le_paysan.backend.shop.Shop;
import shop.le_paysan.backend.shop.ShopPayoutAccountRepository;
import shop.le_paysan.backend.shop.ShopRepository;
import shop.le_paysan.backend.user.User;

@Service
public class ProductService {

    private static final String FILE_CATEGORY = "products";

    private final ProductRepository productRepository;
    private final ProductValidationRepository productValidationRepository;
    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;
    private final ShopPayoutAccountRepository payoutAccountRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public ProductService(
            ProductRepository productRepository,
            ProductValidationRepository productValidationRepository,
            CategoryRepository categoryRepository,
            ShopRepository shopRepository,
            ShopPayoutAccountRepository payoutAccountRepository,
            FileStorageService fileStorageService,
            NotificationService notificationService) {
        this.productRepository = productRepository;
        this.productValidationRepository = productValidationRepository;
        this.categoryRepository = categoryRepository;
        this.shopRepository = shopRepository;
        this.payoutAccountRepository = payoutAccountRepository;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ProductResponse create(User vendor, ProductCreateForm form) {
        Shop shop = shopRepository
                .findByOwnerId(vendor.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucune boutique"));
        if (!Shop.STATUS_VALIDEE.equals(shop.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Votre boutique doit être validée pour publier des produits");
        }
        if (!payoutAccountRepository.existsById(shop.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Renseignez votre compte Mobile Money de reversement avant de publier un produit");
        }

        Product product = new Product();
        product.setShop(shop);
        applyCommonFields(product, form);
        applyDeliveryOptions(product, form, shop);

        if (form.getImages() == null || form.getImages().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Au moins une photo est obligatoire");
        }
        for (int i = 0; i < form.getImages().size(); i++) {
            MultipartFile file = form.getImages().get(i);
            if (file.isEmpty()) {
                continue;
            }
            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setUrl(fileStorageService.store(file, FILE_CATEGORY));
            image.setPosition(i);
            product.getImages().add(image);
        }

        product = productRepository.save(product);

        notificationService.notify(
                vendor,
                "PRODUCT_SUBMITTED",
                "Produit envoyé",
                "Votre produit \"" + product.getName() + "\" a été envoyé et est en attente de validation.",
                "/vendeur/produits");

        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse update(User vendor, Long productId, ProductCreateForm form) {
        Product product = findOwnedOrThrow(vendor, productId);
        applyCommonFields(product, form);
        product.getDeliveryOptions().clear();
        applyDeliveryOptions(product, form, product.getShop());

        if (form.getImages() != null) {
            int startPosition = product.getImages().size();
            for (int i = 0; i < form.getImages().size(); i++) {
                MultipartFile file = form.getImages().get(i);
                if (file.isEmpty()) {
                    continue;
                }
                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setUrl(fileStorageService.store(file, FILE_CATEGORY));
                image.setPosition(startPosition + i);
                product.getImages().add(image);
            }
        }

        // Toute modification repasse le produit en attente de re-validation (cf. docs/02 UC-04).
        product.setStatus(Product.STATUS_EN_ATTENTE);
        product.setUpdatedAt(LocalDateTime.now());

        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse togglePublishState(User vendor, Long productId) {
        Product product = findOwnedOrThrow(vendor, productId);
        applyPublishToggle(product);
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse adminTogglePublishState(Long productId, User admin) {
        Product product = findOrThrow(productId);
        applyPublishToggle(product);
        productRepository.save(product);

        notificationService.notify(
                product.getShop().getOwner(),
                Product.STATUS_DEPUBLIE.equals(product.getStatus()) ? "PRODUCT_HIDDEN_BY_ADMIN" : "PRODUCT_RESTORED_BY_ADMIN",
                Product.STATUS_DEPUBLIE.equals(product.getStatus()) ? "Produit masqué par l'administration" : "Produit republié par l'administration",
                Product.STATUS_DEPUBLIE.equals(product.getStatus())
                        ? "Votre produit \"" + product.getName() + "\" a été retiré du catalogue par l'administration."
                        : "Votre produit \"" + product.getName() + "\" est de nouveau visible dans le catalogue.",
                "/vendeur/produits");

        return ProductResponse.from(product);
    }

    private void applyPublishToggle(Product product) {
        if (Product.STATUS_VALIDE.equals(product.getStatus())) {
            product.setStatus(Product.STATUS_DEPUBLIE);
        } else if (Product.STATUS_DEPUBLIE.equals(product.getStatus())) {
            product.setStatus(Product.STATUS_VALIDE);
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Seul un produit validé ou dépublié peut changer d'état de publication");
        }
        product.setUpdatedAt(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listMine(User vendor, Pageable pageable) {
        Shop shop = shopRepository
                .findByOwnerId(vendor.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucune boutique"));
        return productRepository.findByShopId(shop.getId(), pageable).map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse getPublic(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!Product.STATUS_VALIDE.equals(product.getStatus()) || !Shop.STATUS_VALIDEE.equals(product.getShop().getStatus())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ProductResponse.from(product);
    }

    private static final double CATALOGUE_RADIUS_KM = 50.0;

    @Transactional(readOnly = true)
    public Page<ProductResponse> listPublic(
            Long categoryId,
            Long shopId,
            String deliveryMode,
            String search,
            Double refLat,
            Double refLng,
            Pageable pageable) {
        Specification<Product> spec = Specification.allOf(
                ProductSpecifications.isVisible(),
                ProductSpecifications.hasCategory(categoryId),
                ProductSpecifications.hasShop(shopId),
                ProductSpecifications.hasDeliveryMode(deliveryMode),
                ProductSpecifications.nameContains(search));

        if (refLat == null || refLng == null) {
            return productRepository.findAll(spec, pageable).map(ProductResponse::from);
        }

        // Cf. demande acheteur : "ville ou les environs", trie du plus proche au plus eloigne.
        // Pas d'index geospatial (PostGIS) en place : calcul Haversine en memoire, acceptable au
        // volume actuel du catalogue.
        List<ProductResponse> sorted = productRepository.findAll(spec).stream()
                .map(p -> ProductResponse.from(
                        p, haversineKm(refLat, refLng, p.getShop().getLatitude().doubleValue(), p.getShop().getLongitude().doubleValue())))
                .filter(r -> r.distanceKm() <= CATALOGUE_RADIUS_KM)
                .sorted(Comparator.comparingDouble(ProductResponse::distanceKm))
                .toList();

        int start = Math.min((int) pageable.getOffset(), sorted.size());
        int end = Math.min(start + pageable.getPageSize(), sorted.size());
        return new PageImpl<>(sorted.subList(start, end), pageable, sorted.size());
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listForAdmin(String status, Pageable pageable) {
        Page<Product> page = status != null ? productRepository.findByStatus(status, pageable) : productRepository.findAll(pageable);
        return page.map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse getAdminDetail(Long id) {
        return ProductResponse.from(findOrThrow(id));
    }

    @Transactional
    public ProductResponse validate(Long id, User admin) {
        Product product = findOrThrow(id);
        product.setStatus(Product.STATUS_VALIDE);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        ProductValidation validation = new ProductValidation();
        validation.setProduct(product);
        validation.setAdmin(admin);
        validation.setDecision(ProductValidation.DECISION_VALIDE);
        productValidationRepository.save(validation);

        notificationService.notify(
                product.getShop().getOwner(),
                "PRODUCT_VALIDATED",
                "Produit validé !",
                "Votre produit \"" + product.getName() + "\" est maintenant visible dans le catalogue.",
                "/vendeur/produits");

        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse reject(Long id, User admin, String reason) {
        Product product = findOrThrow(id);
        product.setStatus(Product.STATUS_REJETE);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        ProductValidation validation = new ProductValidation();
        validation.setProduct(product);
        validation.setAdmin(admin);
        validation.setDecision(ProductValidation.DECISION_REJETE);
        validation.setReason(reason);
        productValidationRepository.save(validation);

        notificationService.notify(
                product.getShop().getOwner(),
                "PRODUCT_REJECTED",
                "Produit rejeté",
                "Votre produit \"" + product.getName() + "\" a été rejeté. Motif : " + reason,
                "/vendeur/produits");

        return ProductResponse.from(product);
    }

    private void applyCommonFields(Product product, ProductCreateForm form) {
        if (form.getName() == null || form.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nom du produit est obligatoire");
        }
        if (form.getCategoryId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La catégorie est obligatoire");
        }
        if (form.getUnitPrice() == null || form.getUnitPrice().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le prix doit être supérieur à 0");
        }
        if (form.getStockQuantity() == null || form.getStockQuantity() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La quantité doit être positive ou nulle");
        }
        if (form.getUnit() == null || form.getUnit().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'unité est obligatoire");
        }

        Category category = categoryRepository
                .findById(form.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Catégorie invalide"));

        product.setCategory(category);
        product.setName(form.getName());
        product.setDescription(form.getDescription());
        product.setUnit(form.getUnit());
        product.setUnitPrice(form.getUnitPrice());
        product.setStockQuantity(form.getStockQuantity());
    }

    private void applyDeliveryOptions(Product product, ProductCreateForm form, Shop shop) {
        if (!form.isRetraitBoutique() && !form.isLivraisonDomicile()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Au moins un mode de livraison doit être actif");
        }
        if (form.isRetraitBoutique()) {
            ProductDeliveryOption option = new ProductDeliveryOption();
            option.setProduct(product);
            option.setMode(ProductDeliveryOption.MODE_RETRAIT);
            option.setPickupLocation(
                    (form.getPickupLocation() == null || form.getPickupLocation().isBlank())
                            ? shop.getAddressDetail() + ", " + shop.getAddressCity()
                            : form.getPickupLocation());
            product.getDeliveryOptions().add(option);
        }
        if (form.isLivraisonDomicile()) {
            if (form.getDeliveryPrice() == null || form.getDeliveryPrice().signum() < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Le prix de livraison à domicile est obligatoire si ce mode est actif");
            }
            ProductDeliveryOption option = new ProductDeliveryOption();
            option.setProduct(product);
            option.setMode(ProductDeliveryOption.MODE_LIVRAISON);
            option.setDeliveryPrice(form.getDeliveryPrice());
            product.getDeliveryOptions().add(option);
        }
    }

    private Product findOwnedOrThrow(User vendor, Long productId) {
        Product product = findOrThrow(productId);
        if (!product.getShop().getOwner().getId().equals(vendor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return product;
    }

    private Product findOrThrow(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
