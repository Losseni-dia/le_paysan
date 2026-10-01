package shop.le_paysan.backend.cart;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    List<CartItem> findByCartIdOrderByIdAsc(Long cartId);

    void deleteByCartIdAndProductId(Long cartId, Long productId);
}
