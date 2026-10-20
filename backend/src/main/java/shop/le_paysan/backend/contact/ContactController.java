package shop.le_paysan.backend.contact;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import shop.le_paysan.backend.auth.SecurityUser;
import shop.le_paysan.backend.contact.dto.ContactMessageRequest;

@RestController
public class ContactController {

    private final ContactService service;

    public ContactController(ContactService service) {
        this.service = service;
    }

    /** Ouvert aux visiteurs non connectes (cf. docs/02 UC-12) ; rattache au compte si un token est fourni. */
    @PostMapping("/api/v1/contact")
    public ResponseEntity<Void> submit(
            @AuthenticationPrincipal SecurityUser principal, @Valid @RequestBody ContactMessageRequest request) {
        service.submit(principal == null ? null : principal.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
