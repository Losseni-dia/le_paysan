package shop.le_paysan.backend.commission;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import shop.le_paysan.backend.commission.dto.CommissionRateResponse;

/**
 * Cf. docs/02-specifications-fonctionnelles.md UC-10 : le taux applique a une commande est figé
 * au moment du paiement (snapshot), donc une mise a jour cree une nouvelle ligne active plutot que
 * de modifier l'historique existant.
 */
@Service
public class CommissionService {

    private static final BigDecimal DEFAULT_RATE = BigDecimal.valueOf(10);

    private final CommissionSettingsRepository repository;

    public CommissionService(CommissionSettingsRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public CommissionRateResponse getCurrentRate() {
        return repository
                .findFirstByCategoryIsNullAndActiveTrueOrderByCreatedAtDesc()
                .map(CommissionRateResponse::from)
                .orElseGet(() -> new CommissionRateResponse(null, DEFAULT_RATE, true, null));
    }

    @Transactional(readOnly = true)
    public List<CommissionRateResponse> getHistory() {
        return repository.findByCategoryIsNullOrderByCreatedAtDesc().stream()
                .map(CommissionRateResponse::from)
                .toList();
    }

    @Transactional
    public CommissionRateResponse updateRate(BigDecimal rate) {
        for (CommissionSettings active : repository.findByCategoryIsNullAndActiveTrue()) {
            active.setActive(false);
            repository.save(active);
        }

        CommissionSettings settings = new CommissionSettings();
        settings.setRate(rate);
        settings.setActive(true);
        settings.setCreatedAt(LocalDateTime.now());
        return CommissionRateResponse.from(repository.save(settings));
    }
}
