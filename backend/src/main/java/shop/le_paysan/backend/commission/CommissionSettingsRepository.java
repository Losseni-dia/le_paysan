package shop.le_paysan.backend.commission;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommissionSettingsRepository extends JpaRepository<CommissionSettings, Long> {

    Optional<CommissionSettings> findFirstByCategoryIsNullAndActiveTrueOrderByCreatedAtDesc();

    List<CommissionSettings> findByCategoryIsNullOrderByCreatedAtDesc();

    List<CommissionSettings> findByCategoryIsNullAndActiveTrue();
}
