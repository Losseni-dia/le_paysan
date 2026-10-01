package shop.le_paysan.backend.common;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint protege de diffusion des fichiers uploades (jamais d'acces statique direct).
 * Authentification requise (cf. SecurityConfig : anyRequest().authenticated() par defaut).
 * TODO : verification fine que l'appelant est bien proprietaire/autorise a voir ce fichier
 * precis (notamment pour les futurs documents KYC) -- a affiner en Phase 2.
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/{category}/{filename}")
    public ResponseEntity<InputStreamResource> get(@PathVariable String category, @PathVariable String filename) {
        InputStreamResource resource = new InputStreamResource(fileStorageService.load(category + "/" + filename));
        return ResponseEntity.ok().body(resource);
    }
}
