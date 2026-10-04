package shop.le_paysan.backend.livreur.dto;

import shop.le_paysan.backend.user.User;

public record LivreurResponse(Long id, String username, String firstName, String lastName, String phone) {

    public static LivreurResponse from(User user) {
        return new LivreurResponse(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getPhone());
    }
}
