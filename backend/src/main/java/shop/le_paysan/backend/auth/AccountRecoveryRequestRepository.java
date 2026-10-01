package shop.le_paysan.backend.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountRecoveryRequestRepository extends JpaRepository<AccountRecoveryRequest, Long> {

    Page<AccountRecoveryRequest> findByStatus(String status, Pageable pageable);
}
