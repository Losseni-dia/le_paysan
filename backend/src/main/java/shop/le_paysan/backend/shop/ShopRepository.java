package shop.le_paysan.backend.shop;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    /** Cf. FileAccessService : verifie que le document KYC appartient bien a une boutique dont
     * l'utilisateur donne est le proprietaire. */
    @Query(
            value = "SELECT EXISTS ("
                    + "SELECT 1 FROM shops s "
                    + "LEFT JOIN shop_individual_profile ip ON ip.shop_id = s.id "
                    + "LEFT JOIN shop_entreprenant_profile ep ON ep.shop_id = s.id "
                    + "LEFT JOIN shop_company_profile cp ON cp.shop_id = s.id "
                    + "WHERE s.owner_user_id = :ownerId AND :path IN ("
                    + "ip.id_document_front_url, ip.id_document_back_url, ip.selfie_with_id_url, "
                    + "ep.id_document_front_url, ep.id_document_back_url, ep.selfie_with_id_url, ep.recepisse_document_url, "
                    + "cp.legal_document_url, cp.legal_rep_id_document_front_url, cp.legal_rep_id_document_back_url, "
                    + "cp.legal_rep_selfie_with_id_url))",
            nativeQuery = true)
    boolean existsKycDocumentOwnedBy(@Param("path") String path, @Param("ownerId") Long ownerId);

    Optional<Shop> findByOwnerId(Long ownerId);

    Optional<Shop> findBySlug(String slug);

    boolean existsByOwnerId(Long ownerId);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    Page<Shop> findByStatus(String status, Pageable pageable);

    Page<Shop> findByStatusAndNameContainingIgnoreCase(String status, String name, Pageable pageable);

    long countByStatus(String status);
}
