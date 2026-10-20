export type OwnerType = 'PHYSIQUE' | 'ENTREPRENANT' | 'MORALE';

export interface ShopResponse {
  id: number;
  ownerType: OwnerType;
  name: string;
  description: string | null;
  category: string | null;
  addressCity: string;
  addressDetail: string;
  latitude: number;
  longitude: number;
  phone: string;
  logoUrl: string | null;
  coverUrl: string | null;
  status: 'EN_ATTENTE' | 'VALIDEE' | 'REJETEE' | 'SUSPENDUE';
  createdAt: string;
}

export interface ShopAdminDetailResponse {
  shop: ShopResponse;
  ownerUsername: string;
  ownerEmail: string | null;
  individualProfile: Record<string, string> | null;
  entreprenantProfile: Record<string, string> | null;
  companyProfile: Record<string, string> | null;
}

export interface PayoutAccountResponse {
  operator: 'ORANGE' | 'MTN' | 'MOOV' | 'WAVE';
  phoneNumber: string;
  accountHolderName: string;
  verified: boolean;
}

export interface ShopDeliveryTier {
  id?: number;
  maxDistanceKm: number;
  price: number;
}

export interface ShopDeliveryCity {
  id?: number;
  cityName: string;
  price: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
