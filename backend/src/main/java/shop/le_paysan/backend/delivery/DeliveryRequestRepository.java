package shop.le_paysan.backend.delivery;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRequestRepository extends JpaRepository<DeliveryRequest, Long> {

    List<DeliveryRequest> findByOrderIdAndStatus(Long orderId, String status);

    List<DeliveryRequest> findByLivreurIdAndStatusOrderByCreatedAtDesc(Long livreurId, String status);

    Optional<DeliveryRequest> findByOrderIdAndLivreurId(Long orderId, Long livreurId);

    boolean existsByOrderIdAndLivreurIdAndStatus(Long orderId, Long livreurId, String status);
}
