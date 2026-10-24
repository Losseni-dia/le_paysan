package shop.le_paysan.backend.auth;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import shop.le_paysan.backend.auth.dto.AuthResponse;
import shop.le_paysan.backend.auth.dto.LoginRequest;
import shop.le_paysan.backend.auth.dto.RegisterLivreurRequest;
import shop.le_paysan.backend.auth.dto.RegisterRequest;
import shop.le_paysan.backend.user.Role;
import shop.le_paysan.backend.user.RoleRepository;
import shop.le_paysan.backend.user.User;
import shop.le_paysan.backend.user.UserRepository;
import shop.le_paysan.backend.user.dto.BecomeLivreurRequest;
import shop.le_paysan.backend.user.dto.UserResponse;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /** Version des documents legaux en vigueur, cf. docs/08-mentions-legales-cgu-rgpd.md. */
    private static final String LEGAL_DOCS_VERSION = "1.0";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Ce login est deja utilise");
        }
        if (request.email() != null && !request.email().isBlank() && userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Cet email est deja utilise");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmail(blankToNull(request.email()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(blankToNull(request.phone()));
        user.setCity(blankToNull(request.city()));
        user.setAuthProvider(User.PROVIDER_LOCAL);

        LocalDateTime now = LocalDateTime.now();
        user.setCguAcceptedAt(now);
        user.setCguVersion(LEGAL_DOCS_VERSION);
        user.setCgvAcceptedAt(now);
        user.setCgvVersion(LEGAL_DOCS_VERSION);
        user.setPrivacyAcceptedAt(now);
        user.setPrivacyVersion(LEGAL_DOCS_VERSION);

        Role userRole = roleRepository
                .findByName(Role.USER)
                .orElseThrow(() -> new IllegalStateException("Role ROLE_USER manquant en base (cf. sql/schema.sql)"));
        user.getRoles().add(userRole);

        user = userRepository.save(user);

        return buildAuthResponse(user);
    }

    /** Inscription volontaire livreur (independante de toute boutique) : cf. demande utilisateur. */
    @Transactional
    public AuthResponse registerLivreur(RegisterLivreurRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Ce login est deja utilise");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(blankToNull(request.phone()));
        user.setCity(request.city());
        user.setCityLat(request.cityLat());
        user.setCityLng(request.cityLng());
        user.setAvailable(true);
        user.setKycStatus(User.KYC_STATUS_EN_ATTENTE);
        user.setAuthProvider(User.PROVIDER_LOCAL);

        LocalDateTime now = LocalDateTime.now();
        user.setCguAcceptedAt(now);
        user.setCguVersion(LEGAL_DOCS_VERSION);
        user.setCgvAcceptedAt(now);
        user.setCgvVersion(LEGAL_DOCS_VERSION);
        user.setPrivacyAcceptedAt(now);
        user.setPrivacyVersion(LEGAL_DOCS_VERSION);

        Role livreurRole = roleRepository
                .findByName(Role.LIVREUR)
                .orElseThrow(() -> new IllegalStateException("Role ROLE_LIVREUR manquant en base (cf. sql/schema.sql)"));
        user.getRoles().add(livreurRole);

        user = userRepository.save(user);

        return buildAuthResponse(user);
    }

    /**
     * Passage d'un compte deja existant (acheteur/vendeur) au role livreur : pas de nouvelle
     * inscription, pas de resaisie du nom/prenom/telephone (cf. demande utilisateur - eviter le
     * chevauchement d'informations entre les deux comptes).
     */
    @Transactional
    public AuthResponse becomeLivreur(User user, BecomeLivreurRequest request) {
        boolean alreadyLivreur = user.getRoles().stream().anyMatch(r -> Role.LIVREUR.equals(r.getName()));
        if (alreadyLivreur) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Vous êtes déjà livreur");
        }

        user.setCity(request.city());
        user.setCityLat(request.cityLat());
        user.setCityLng(request.cityLng());
        user.setAvailable(true);
        user.setKycStatus(User.KYC_STATUS_EN_ATTENTE);

        LocalDateTime now = LocalDateTime.now();
        user.setCguAcceptedAt(now);
        user.setCguVersion(LEGAL_DOCS_VERSION);
        user.setCgvAcceptedAt(now);
        user.setCgvVersion(LEGAL_DOCS_VERSION);
        user.setPrivacyAcceptedAt(now);
        user.setPrivacyVersion(LEGAL_DOCS_VERSION);

        Role livreurRole = roleRepository
                .findByName(Role.LIVREUR)
                .orElseThrow(() -> new IllegalStateException("Role ROLE_LIVREUR manquant en base (cf. sql/schema.sql)"));
        user.getRoles().add(livreurRole);

        user = userRepository.save(user);

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Identifiants invalides", e);
        }

        User user = userRepository
                .findByUsername(request.username())
                .orElseThrow(() -> new IllegalStateException("Utilisateur authentifie introuvable"));

        return buildAuthResponse(user);
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwtService.isValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Refresh token invalide");
        }
        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));

        return buildAuthResponse(user);
    }

    /**
     * UC-01b (email renseigne) : envoie un lien de reinitialisation si l'email
     * correspond a un compte. Toujours silencieux (pas d'enumeration de comptes).
     */
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = jwtService.generatePasswordResetToken(user);
            String link = frontendUrl + "/reinitialiser-mot-de-passe?token=" + token;
            emailService.sendPasswordResetEmail(email, link);
            log.info("Lien de reinitialisation envoye a l'utilisateur id={}", user.getId());
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (!jwtService.isValid(token) || !jwtService.isPasswordResetToken(token)) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Lien de reinitialisation invalide ou expire");
        }
        String username = jwtService.extractUsername(token);
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        var roleNames = user.getRoles().stream().map(Role::getName).toList();
        return new AuthResponse(accessToken, refreshToken, UserResponse.from(user), roleNames);
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
