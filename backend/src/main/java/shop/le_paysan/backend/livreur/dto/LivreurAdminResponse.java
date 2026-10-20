package shop.le_paysan.backend.livreur.dto;

import java.time.LocalDateTime;

import shop.le_paysan.backend.livreur.ShopLivreur;

public record LivreurAdminResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        String phone,
        String city,
        String photoUrl,
        String idDocumentFrontUrl,
        String idDocumentBackUrl,
        String selfieWithIdUrl,
        String kycStatus,
        boolean enabled,
        boolean available,
        Long shopId,
        String shopName,
        LocalDateTime createdAt) {

    public static LivreurAdminResponse from(ShopLivreur link) {
        var user = link.getLivreur();
        return new LivreurAdminResponse(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getCity(),
                user.getPhotoUrl(),
                user.getIdDocumentFrontUrl(),
                user.getIdDocumentBackUrl(),
                user.getSelfieWithIdUrl(),
                user.getKycStatus(),
                user.isEnabled(),
                link.isAvailable(),
                link.getShop().getId(),
                link.getShop().getName(),
                link.getCreatedAt());
    }
}
