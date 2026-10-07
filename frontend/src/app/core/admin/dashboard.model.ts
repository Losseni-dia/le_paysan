export interface DashboardResponse {
  utilisateursTotal: number;
  boutiquesValidees: number;
  boutiquesEnAttente: number;
  produitsEnAttente: number;
  commandesEnAttentePaiement: number;
  reversementsEnEchec: number;
  avisTotal: number;
}

export interface ShopRevenueResponse {
  shopName: string;
  revenue: number;
}

export interface StatsResponse {
  chiffreAffairesTotal: number;
  commissionTotale: number;
  boutiquesActives: number;
  commandesParStatut: Record<string, number>;
  topBoutiques: ShopRevenueResponse[];
}
