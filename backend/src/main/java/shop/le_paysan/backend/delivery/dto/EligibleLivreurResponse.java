package shop.le_paysan.backend.delivery.dto;

import shop.le_paysan.backend.user.User;

public record EligibleLivreurResponse(
        Long id, String firstName, String lastName, String phone, String city, Double distanceKm) {

    public static EligibleLivreurResponse from(User livreur, Double distanceKm) {
        return new EligibleLivreurResponse(
                livreur.getId(), livreur.getFirstName(), livreur.getLastName(), livreur.getPhone(),
                livreur.getCity(), distanceKm);
    }
}
