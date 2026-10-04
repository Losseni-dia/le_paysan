package shop.le_paysan.backend.payment;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorPayoutRepository extends JpaRepository<VendorPayout, Long> {

    List<VendorPayout> findByStatus(String status);

    Page<VendorPayout> findByStatus(String status, Pageable pageable);

    long countByStatus(String status);
}
