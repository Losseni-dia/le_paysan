package shop.le_paysan.backend.commission;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Taux de commission plateforme par defaut (cf. docs/07-plan-execution.md Phase 4). */
@Component
public class CommissionSettingsSeeder implements CommandLineRunner {

    private static final BigDecimal DEFAULT_RATE = BigDecimal.valueOf(10);

    private final CommissionSettingsRepository repository;

    public CommissionSettingsSeeder(CommissionSettingsRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.findFirstByCategoryIsNullAndActiveTrueOrderByCreatedAtDesc().isPresent()) {
            return;
        }
        CommissionSettings settings = new CommissionSettings();
        settings.setCategory(null);
        settings.setRate(DEFAULT_RATE);
        settings.setActive(true);
        repository.save(settings);
    }
}
