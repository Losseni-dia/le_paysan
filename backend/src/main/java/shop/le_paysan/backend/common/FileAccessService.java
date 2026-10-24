package shop.le_paysan.backend.common;

import java.util.Set;

import org.springframework.stereotype.Service;

import shop.le_paysan.backend.auth.AccountRecoveryRequestRepository;
import shop.le_paysan.backend.order.OrderRepository;
import shop.le_paysan.backend.shop.ShopRepository;
import shop.le_paysan.backend.user.Role;
import shop.le_paysan.backend.user.User;
import shop.le_paysan.backend.user.UserRepository;

/**
 * Verifie que l'appelant est bien autorise a voir un fichier precis (pas seulement
 * authentifie), cf. audit securite - TODO historique de FileController.
 */
@Service
public class FileAccessService {

    private static final Set<String> PUBLIC_CATEGORIES = Set.of("products", "shop-public");

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final AccountRecoveryRequestRepository accountRecoveryRequestRepository;

    public FileAccessService(
            ShopRepository shopRepository,
            UserRepository userRepository,
            OrderRepository orderRepository,
            AccountRecoveryRequestRepository accountRecoveryRequestRepository) {
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.accountRecoveryRequestRepository = accountRecoveryRequestRepository;
    }

    public boolean canAccess(String category, String relativePath, User principal) {
        if (PUBLIC_CATEGORIES.contains(category)) {
            return true;
        }
        if (principal == null) {
            return false;
        }
        if (principal.getRoles().stream().anyMatch(r -> Role.ADMIN.equals(r.getName()))) {
            return true;
        }

        Long userId = principal.getId();
        return switch (category) {
            case "shop-kyc" -> shopRepository.existsKycDocumentOwnedBy(relativePath, userId);
            case "security-selfies" -> userRepository.existsSecuritySelfieOwnedBy(relativePath, userId);
            case "livreur-kyc", "livreur-photos" -> userRepository.existsLivreurDocumentOwnedBy(relativePath, userId);
            case "account-recovery" -> accountRecoveryRequestRepository.existsSubmittedSelfieOwnedBy(relativePath, userId);
            case "delivery-proofs" -> orderRepository.existsDeliveryProofAccessibleBy(relativePath, userId);
            // Categorie inconnue : on refuse par defaut plutot que d'autoriser par erreur.
            default -> false;
        };
    }
}
