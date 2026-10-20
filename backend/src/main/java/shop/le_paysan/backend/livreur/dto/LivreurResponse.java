package shop.le_paysan.backend.livreur.dto;

import shop.le_paysan.backend.user.User;

public record LivreurResponse(
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
        long deliveryCount) {

    public static LivreurResponse from(User user, boolean available, long deliveryCount) {
        return new LivreurResponse(
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
                available,
                deliveryCount);
    }
}
