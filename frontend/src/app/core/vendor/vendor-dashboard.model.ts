export interface VendorDashboardResponse {
  produitsActifs: number;
  produitsEnAttente: number;
  commandesATraiter: number;
  chiffreAffairesNet: number;
}

export interface ProductSalesResponse {
  productName: string;
  quantitySold: number;
  revenue: number;
}

export interface VendorStatsResponse {
  chiffreAffairesNet: number;
  commandesParStatut: Record<string, number>;
  topProduits: ProductSalesResponse[];
}
