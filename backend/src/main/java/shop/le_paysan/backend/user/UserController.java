package shop.le_paysan.backend.user;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import shop.le_paysan.backend.auth.AuthService;
import shop.le_paysan.backend.auth.SecurityUser;
import shop.le_paysan.backend.auth.dto.AuthResponse;
import shop.le_paysan.backend.common.FileStorageService;
import shop.le_paysan.backend.user.dto.BecomeLivreurRequest;
import shop.le_paysan.backend.user.dto.ChangePasswordRequest;
import shop.le_paysan.backend.user.dto.UpdateProfileRequest;
import shop.le_paysan.backend.user.dto.UserResponse;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public UserController(
            UserRepository userRepository,
            FileStorageService fileStorageService,
            PasswordEncoder passwordEncoder,
            AuthService authService) {
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal SecurityUser principal) {
        return UserResponse.from(principal.getUser());
    }

    @PutMapping("/me")
    public UserResponse updateMe(
            @AuthenticationPrincipal SecurityUser principal, @Valid @RequestBody UpdateProfileRequest request) {
        User user = userRepository
                .findById(principal.getUser().getId())
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));

        if (request.email() != null && !request.email().equals(user.getEmail())
                && userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Cet email est deja utilise");
        }

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setUpdatedAt(java.time.LocalDateTime.now());

        return UserResponse.from(userRepository.save(user));
    }

    @PutMapping("/me/password")
    public void changePassword(@AuthenticationPrincipal SecurityUser principal, @Valid @RequestBody ChangePasswordRequest request) {
        User user = userRepository
                .findById(principal.getUser().getId())
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Mot de passe actuel incorrect");
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        // Tronque a la seconde : le claim JWT "iat" (norme JWT) perd la precision sous-seconde,
        // un token regenere dans la meme seconde paraitrait sinon "emis avant" ce timestamp.
        user.setTokenValidFrom(now.truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        user.setUpdatedAt(now);
        userRepository.save(user);
    }

    /**
     * Passage du compte connecte au role livreur (bouton "Devenir livreur" du profil) : pas de
     * nouvelle inscription, pas de resaisie du nom/prenom (cf. demande utilisateur).
     */
    @PostMapping("/me/devenir-livreur")
    public AuthResponse becomeLivreur(
            @AuthenticationPrincipal SecurityUser principal, @Valid @RequestBody BecomeLivreurRequest request) {
        return authService.becomeLivreur(principal.getUser(), request);
    }

    @PutMapping(value = "/me/security-selfie", consumes = "multipart/form-data")
    public UserResponse updateSecuritySelfie(
            @AuthenticationPrincipal SecurityUser principal, @RequestParam("selfie") MultipartFile selfie) {
        User user = userRepository
                .findById(principal.getUser().getId())
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));

        String path = fileStorageService.store(selfie, "security-selfies");
        user.setSecuritySelfieUrl(path);
        user.setUpdatedAt(java.time.LocalDateTime.now());

        return UserResponse.from(userRepository.save(user));
    }
}
