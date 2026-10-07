package shop.le_paysan.backend.admin.dashboard.dto;

public record DashboardResponse(
        long utilisateursTotal,
        long boutiquesValidees,
        long boutiquesEnAttente,
        long produitsEnAttente,
        long commandesEnAttentePaiement,
        long reversementsEnEchec,
        long avisTotal) {
}
