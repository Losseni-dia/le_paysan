package shop.le_paysan.backend.user;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cree un compte admin de demarrage s'il n'existe pas deja, cf.
 * docs/07-plan-execution.md (Phase 1) : email losdiakite@gmail.com.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_EMAIL = "losdiakite@gmail.com";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername(ADMIN_USERNAME)) {
            return;
        }

        Role adminRole = roleRepository
                .findByName(Role.ADMIN)
                .orElseThrow(() -> new IllegalStateException("Role ROLE_ADMIN manquant en base (cf. sql/schema.sql)"));

        String generatedPassword = generateRandomPassword();

        User admin = new User();
        admin.setUsername(ADMIN_USERNAME);
        admin.setEmail(ADMIN_EMAIL);
        admin.setPasswordHash(passwordEncoder.encode(generatedPassword));
        admin.setFirstName("Admin");
        admin.setLastName("Le Paysan");
        admin.setEmailVerified(true);
        LocalDateTime now = LocalDateTime.now();
        admin.setCguAcceptedAt(now);
        admin.setCguVersion("1.0");
        admin.setCgvAcceptedAt(now);
        admin.setCgvVersion("1.0");
        admin.setPrivacyAcceptedAt(now);
        admin.setPrivacyVersion("1.0");
        admin.getRoles().add(adminRole);

        userRepository.save(admin);

        log.warn(
                "Compte admin de demarrage cree : username='{}', mot de passe genere='{}' "
                        + "-- a changer immediatement apres la premiere connexion.",
                ADMIN_USERNAME,
                generatedPassword);
    }

    private static String generateRandomPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
