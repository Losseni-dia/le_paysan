import { DeliveryMode } from '../cart/cart.model';

export type OrderStatus =
  | 'EN_ATTENTE_PAIEMENT'
  | 'PAYEE'
  | 'EN_PREPARATION'
  | 'PRETE'
  | 'EXPEDIEE'
  | 'LIVREE'
  | 'ANNULEE';

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
  createdAt: string;
}
