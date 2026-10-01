package shop.le_paysan.backend.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Charge un fichier .env (KEY=VALUE par ligne) a la racine du module backend,
 * s'il existe, comme source de proprietes basse priorite (surchargeable par
 * de vraies variables d'environnement OS). Fichier non committe (.gitignore).
 *
 * <p>EnvironmentPostProcessor est deprecated depuis Spring Boot 4.0 (marque
 * pour suppression) mais reste fonctionnel en 4.1.1 ; a migrer vers son
 * successeur lors d'une montee de version future de Spring Boot.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path dotenv = Path.of(".env");
        if (!Files.isRegularFile(dotenv)) {
            return;
        }

        Map<String, Object> values = new LinkedHashMap<>();
        try {
            List<String> lines = Files.readAllLines(dotenv, StandardCharsets.UTF_8);
            for (String rawLine : lines) {
                String line = rawLine.strip();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int separator = line.indexOf('=');
                if (separator <= 0) {
                    continue;
                }
                String key = line.substring(0, separator).strip();
                String value = line.substring(separator + 1).strip();
                values.put(key, value);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de lire le fichier .env", e);
        }

        if (!values.isEmpty()) {
            environment.getPropertySources().addLast(new MapPropertySource("dotenv", values));
        }
    }
}
