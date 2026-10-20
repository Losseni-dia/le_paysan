package shop.le_paysan.backend.common;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Garantit une forme JSON unique (status, error, message, path) pour toutes les erreurs API,
 * y compris les erreurs de validation (@Valid) qui ont par defaut une forme differente (liste
 * de field errors) - un client generique (mobile) n'a ainsi qu'un seul format a parser.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return ResponseEntity.status(status).body(body(status, ex.getReason(), request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(this::readableFieldMessage)
                .orElse("Certaines informations saisies sont invalides.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(HttpStatus.BAD_REQUEST, message, request));
    }

    /**
     * Les DTO sans attribut `message` sur leurs annotations (@NotBlank, @Pattern, etc.) recoivent de
     * Bean Validation un texte par defaut en anglais - voire, pour @Pattern, l'expression reguliere
     * brute ("must match ..."). On ne garde le message par defaut (deja redige en francais dans le
     * code) que s'il ne correspond a aucun de ces textes bruts connus ; sinon on le remplace par un
     * message generique lisible, pour qu'un client (web ou mobile) n'affiche jamais de texte technique.
     */
    private String readableFieldMessage(FieldError error) {
        String raw = error.getDefaultMessage() == null ? "" : error.getDefaultMessage();
        boolean isKnownRawEnglishDefault = raw.isBlank()
                || raw.startsWith("must not be")
                || raw.startsWith("must be")
                || raw.startsWith("must match")
                || raw.startsWith("size must be");
        if (!isKnownRawEnglishDefault) {
            return raw;
        }
        return "Le champ \"" + error.getField() + "\" est invalide.";
    }

    private Map<String, Object> body(HttpStatus status, String message, HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getRequestURI());
        return body;
    }
}
