import { DeliveryMode } from '../cart/cart.model';

export type OrderStatus =
  | 'EN_ATTENTE_PAIEMENT'
  | 'PAYEE'
  | 'EN_PREPARATION'
  | 'PRETE'
  | 'EXPEDIEE'
  | 'RECUPEREE'
  | 'EN_ROUTE'
  | 'LIVREE'
  | 'ANNULEE';

export type ChangedBy = 'SYSTEME' | 'VENDEUR' | 'ACHETEUR' | 'LIVREUR';
export type ProofType = 'NONE' | 'SIGNATURE' | 'PHOTO';

export interface OrderStatusHistoryResponse {
  status: OrderStatus;
  changedBy: ChangedBy;
  note: string | null;
  proofType: ProofType;
  proofUrl: string | null;
  changedAt: string;
}

export interface OrderItemResponse {
  productId: number;
  productName: string;
  unitPrice: number;
  quantity: number;
  deliveryMode: DeliveryMode;
  deliveryPrice: number;
  deliveryAddress: string | null;
  lineSubtotal: number;
}

export interface OrderResponse {
  id: number;
  orderNumber: string;
  shopId: number;
  shopName: string;
  status: OrderStatus;
  subtotalAmount: number;
  deliveryAmount: number;
  totalAmount: number;
  items: OrderItemResponse[];
  historique: OrderStatusHistoryResponse[];
  createdAt: string;
}

export interface AdminOrderResponse {
  id: number;
  orderNumber: string;
  shopId: number;
  shopName: string;
  buyerUserId: number;
  buyerUsername: string;
  buyerFullName: string;
  status: OrderStatus;
  subtotalAmount: number;
  deliveryAmount: number;
  commissionAmount: number;
  totalAmount: number;
  items: OrderItemResponse[];
  historique: OrderStatusHistoryResponse[];
  assignedLivreurId: number | null;
  assignedLivreurName: string | null;
  createdAt: string;
}
