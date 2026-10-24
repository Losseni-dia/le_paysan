package shop.le_paysan.backend.common;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import shop.le_paysan.backend.auth.SecurityUser;

/**
 * Endpoint protege de diffusion des fichiers uploades (jamais d'acces statique direct).
 * Authentification requise pour les categories privees (cf. SecurityConfig), et verification
 * fine via FileAccessService que l'appelant est bien proprietaire/admin pour ce fichier precis.
 */
@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileStorageService fileStorageService;
    private final FileAccessService fileAccessService;

    public FileController(FileStorageService fileStorageService, FileAccessService fileAccessService) {
        this.fileStorageService = fileStorageService;
        this.fileAccessService = fileAccessService;
    }

    @GetMapping("/{category}/{filename}")
    public ResponseEntity<InputStreamResource> get(
            @AuthenticationPrincipal SecurityUser principal, @PathVariable String category, @PathVariable String filename) {
        String relativePath = category + "/" + filename;
        boolean authorized = fileAccessService.canAccess(category, relativePath, principal != null ? principal.getUser() : null);
        if (!authorized) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        InputStreamResource resource = new InputStreamResource(fileStorageService.load(relativePath));
        // Sans Content-Type explicite, un navigateur (Firefox notamment) ouvrant le blob
        // directement (ex: window.open) ne sait pas que c'est une image et tente de
        // l'interpreter comme du texte/JSON -> affiche les octets bruts.
        MediaType mediaType = MediaTypeFactory.getMediaType(filename).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().contentType(mediaType).body(resource);
    }
}
