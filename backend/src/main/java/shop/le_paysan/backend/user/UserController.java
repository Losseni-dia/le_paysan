package shop.le_paysan.backend.user;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import shop.le_paysan.backend.auth.SecurityUser;
import shop.le_paysan.backend.common.FileStorageService;
import shop.le_paysan.backend.user.dto.UpdateProfileRequest;
import shop.le_paysan.backend.user.dto.UserResponse;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public UserController(UserRepository userRepository, FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
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
