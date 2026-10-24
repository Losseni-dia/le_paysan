package shop.le_paysan.backend.shop;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    Optional<Shop> findByOwnerId(Long ownerId);

    Optional<Shop> findBySlug(String slug);

    boolean existsByOwnerId(Long ownerId);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    Page<Shop> findByStatus(String status, Pageable pageable);

    Page<Shop> findByStatusAndNameContainingIgnoreCase(String status, String name, Pageable pageable);

    long countByStatus(String status);
}
