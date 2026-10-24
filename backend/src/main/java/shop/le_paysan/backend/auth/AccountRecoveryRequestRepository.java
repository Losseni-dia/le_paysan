package shop.le_paysan.backend.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRecoveryRequestRepository extends JpaRepository<AccountRecoveryRequest, Long> {

    Page<AccountRecoveryRequest> findByStatus(String status, Pageable pageable);

    /** Cf. FileAccessService : le selfie soumis pour une demande de recuperation n'appartient
     * qu'au demandeur (l'admin a de toute facon un acces global via FileAccessService). */
    @Query(
            value = "SELECT EXISTS (SELECT 1 FROM account_recovery_requests r WHERE r.user_id = :userId AND r.submitted_selfie_url = :path)",
            nativeQuery = true)
    boolean existsSubmittedSelfieOwnedBy(@Param("path") String path, @Param("userId") Long userId);
}
