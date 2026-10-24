export type ProductStatus = 'EN_ATTENTE' | 'VALIDE' | 'REJETE' | 'DEPUBLIE';
export type DeliveryMode = 'RETRAIT_BOUTIQUE' | 'LIVRAISON_DOMICILE';

export interface ProductDeliveryOptionResponse {
  mode: DeliveryMode;
  pickupLocation: string | null;
  deliveryPrice: number | null;
}

export interface ProductResponse {
  id: number;
  slug: string;
  shopId: number;
  shopSlug: string;
  shopName: string;
  shopCity: string | null;
  shopLatitude: number | null;
  shopLongitude: number | null;
  categoryId: number;
  categoryName: string;
  name: string;
  description: string | null;
  unit: string;
  unitPrice: number;
  stockQuantity: number;
  status: ProductStatus;
  imageUrls: string[];
  deliveryOptions: ProductDeliveryOptionResponse[];
  distanceKm: number | null;
  ratingAverage: number | null;
  ratingCount: number | null;
  createdAt: string;
}
