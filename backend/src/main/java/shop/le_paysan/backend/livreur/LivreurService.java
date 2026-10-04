package shop.le_paysan.backend.livreur;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import shop.le_paysan.backend.livreur.dto.CreateLivreurRequest;
import shop.le_paysan.backend.livreur.dto.LivreurResponse;
import shop.le_paysan.backend.shop.Shop;
import shop.le_paysan.backend.user.Role;
import shop.le_paysan.backend.user.RoleRepository;
import shop.le_paysan.backend.user.User;
import shop.le_paysan.backend.user.UserRepository;

@Service
public class LivreurService {

    private final ShopLivreurRepository shopLivreurRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public LivreurService(
            ShopLivreurRepository shopLivreurRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.shopLivreurRepository = shopLivreurRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LivreurResponse create(Shop shop, CreateLivreurRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce login est déjà utilisé");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
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

        return LivreurResponse.from(user);
    }

    @Transactional(readOnly = true)
    public List<LivreurResponse> listForShop(Shop shop) {
        return shopLivreurRepository.findByShopIdOrderByCreatedAtDesc(shop.getId()).stream()
                .map(link -> LivreurResponse.from(link.getLivreur()))
                .toList();
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
