package shop.le_paysan.backend.user;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    java.util.List<User> findByRoles_Name(String roleName);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("""
            SELECT DISTINCT u FROM User u LEFT JOIN u.roles r
            WHERE (:search IS NULL
                    OR LOWER(u.username) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                    OR LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                    OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                    OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
                AND (:role IS NULL OR r.name = CAST(:role AS string))
            """)
    Page<User> search(@Param("search") String search, @Param("role") String role, Pageable pageable);
}
