package shop.le_paysan.backend.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * Stockage local des fichiers uploades (selfies, futurs documents KYC).
 * MVP : disque local hors controle de version, servi via un endpoint protege
 * (FileController), jamais en acces statique direct. Cf. docs/04-architecture-technique.md §7.
 */
@Service
public class FileStorageService {

    private final Path baseDir;

    public FileStorageService(@Value("${app.uploads.base-dir}") String baseDirPath) {
        this.baseDir = Path.of(baseDirPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de creer le dossier d'upload : " + baseDir, e);
        }
    }

    /**
     * Stocke le fichier sous {@code category/<uuid>.<ext>} et retourne le chemin
     * relatif (a exposer ensuite via FileController, jamais le chemin disque).
     */
    public String store(MultipartFile file, String category) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Fichier vide");
        }
        String extension = extractExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + extension;

        Path categoryDir = baseDir.resolve(category).normalize();
        if (!categoryDir.startsWith(baseDir)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Categorie invalide");
        }

        try {
            Files.createDirectories(categoryDir);
            Path target = categoryDir.resolve(filename);
            file.transferTo(target);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Echec de l'enregistrement du fichier", e);
        }

        return category + "/" + filename;
    }

    public InputStream load(String relativePath) {
        Path target = baseDir.resolve(relativePath).normalize();
        if (!target.startsWith(baseDir)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Chemin invalide");
        }
        try {
            if (!Files.isRegularFile(target)) {
                throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);
            }
            return Files.newInputStream(target);
        } catch (IOException e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Fichier introuvable", e);
        }
    }

    private static String extractExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dot = originalFilename.lastIndexOf('.');
        return dot >= 0 ? originalFilename.substring(dot) : "";
    }
}
