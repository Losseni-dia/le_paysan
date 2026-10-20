package shop.le_paysan.backend.livreur;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LivreurKycValidationRepository extends JpaRepository<LivreurKycValidation, Long> {

    List<LivreurKycValidation> findByLivreurIdOrderByCreatedAtDesc(Long livreurUserId);
}
