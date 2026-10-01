package shop.le_paysan.backend.auth;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import shop.le_paysan.backend.auth.dto.AccountRecoveryResponse;
import shop.le_paysan.backend.common.FileStorageService;
import shop.le_paysan.backend.user.User;
import shop.le_paysan.backend.user.UserRepository;

/**
 * UC-01b (sans email) : recuperation de compte par selfie compare manuellement
 * par un admin au selfie de reference. Cf. docs/02-specifications-fonctionnelles.md.
 */
@Service
public class AccountRecoveryService {

    private final AccountRecoveryRequestRepository repository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;

    public AccountRecoveryService(
            AccountRecoveryRequestRepository repository,
            UserRepository userRepository,
            FileStorageService fileStorageService,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AccountRecoveryResponse create(String username, MultipartFile selfie) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Compte introuvable"));

        String path = fileStorageService.store(selfie, "account-recovery");

        AccountRecoveryRequest request = new AccountRecoveryRequest();
        request.setUser(user);
        request.setSubmittedSelfieUrl(path);
        request.setStatus(AccountRecoveryRequest.STATUS_NOUVELLE);

        return AccountRecoveryResponse.from(repository.save(request));
    }

    @Transactional(readOnly = true)
    public Page<AccountRecoveryResponse> list(String status, Pageable pageable) {
        Page<AccountRecoveryRequest> page = status != null
                ? repository.findByStatus(status, pageable)
                : repository.findAll(pageable);
        return page.map(AccountRecoveryResponse::from);
    }

    @Transactional(readOnly = true)
    public AccountRecoveryResponse get(Long id) {
        return AccountRecoveryResponse.from(findOrThrow(id));
    }

    /** @return le mot de passe temporaire genere, a communiquer par l'admin hors-ligne. */
    @Transactional
    public String approve(Long id, User admin) {
        AccountRecoveryRequest request = findOrThrow(id);
        User user = request.getUser();

        String temporaryPassword = generateRandomPassword();
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        request.setStatus(AccountRecoveryRequest.STATUS_APPROUVEE);
        request.setReviewedByAdmin(admin);
        request.setTemporaryPasswordIssuedAt(LocalDateTime.now());
        request.setReviewedAt(LocalDateTime.now());
        repository.save(request);

        return temporaryPassword;
    }

    @Transactional
    public void reject(Long id, User admin, String reason) {
        AccountRecoveryRequest request = findOrThrow(id);
        request.setStatus(AccountRecoveryRequest.STATUS_REJETEE);
        request.setReviewedByAdmin(admin);
        request.setReviewReason(reason);
        request.setReviewedAt(LocalDateTime.now());
        repository.save(request);
    }

    private AccountRecoveryRequest findOrThrow(Long id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
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
