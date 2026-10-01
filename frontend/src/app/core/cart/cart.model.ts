export type DeliveryMode = 'RETRAIT_BOUTIQUE' | 'LIVRAISON_DOMICILE';

export interface CartItemResponse {
  productId: number;
  productName: string;
  imageUrl: string | null;
  shopId: number;
  shopName: string;
  unitPrice: number;
  quantity: number;
  deliveryMode: DeliveryMode;
  deliveryPrice: number;
  deliveryAddress: string | null;
  lineSubtotal: number;
  availableDeliveryModes: DeliveryMode[];
  stockAvailable: number;
  available: boolean;
}

export interface CartResponse {
  items: CartItemResponse[];
  subtotalAmount: number;
  deliveryAmount: number;
  totalAmount: number;
}

export interface AddCartItemRequest {
  productId: number;
  quantity: number;
  deliveryMode: DeliveryMode;
  deliveryAddress?: string;
}

export interface UpdateCartItemRequest {
  quantity: number;
  deliveryMode: DeliveryMode;
  deliveryAddress?: string;
}
