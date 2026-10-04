export type PayoutStatus = 'EN_ATTENTE' | 'ENVOYE' | 'ECHEC';

export interface VendorPayoutResponse {
  id: number;
  orderNumber: string;
  shopId: number;
  shopName: string;
  amount: number;
  status: PayoutStatus;
  fedapayPayoutId: string | null;
  failureReason: string | null;
  createdAt: string;
  processedAt: string | null;
}
