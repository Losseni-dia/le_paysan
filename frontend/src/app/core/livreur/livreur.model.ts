export type LivreurKycStatus = 'EN_ATTENTE' | 'VALIDE' | 'REJETE';

export interface LivreurResponse {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  phone: string | null;
  city: string | null;
  photoUrl: string | null;
  idDocumentFrontUrl: string | null;
  idDocumentBackUrl: string | null;
  selfieWithIdUrl: string | null;
  kycStatus: LivreurKycStatus | null;
  enabled: boolean;
  available: boolean;
  deliveryCount: number;
}

export interface LivreurAdminResponse {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  phone: string | null;
  city: string | null;
  photoUrl: string | null;
  idDocumentFrontUrl: string | null;
  idDocumentBackUrl: string | null;
  selfieWithIdUrl: string | null;
  kycStatus: LivreurKycStatus | null;
  enabled: boolean;
  available: boolean;
  createdAt: string;
}

export interface RegisterLivreurRequest {
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
  city: string;
  cityLat: number;
  cityLng: number;
  acceptCgu: boolean;
  acceptCgv: boolean;
  acceptPrivacy: boolean;
}

export interface UploadLivreurDocumentsRequest {
  photo?: File | null;
  idDocumentFront?: File | null;
  idDocumentBack?: File | null;
  selfieWithId?: File | null;
}

export interface EligibleLivreurResponse {
  id: number;
  firstName: string;
  lastName: string;
  phone: string | null;
  city: string | null;
  distanceKm: number | null;
}

export type DeliveryRequestStatus = 'EN_ATTENTE' | 'ACCEPTEE' | 'EXPIREE';

export interface DeliveryRequestResponse {
  id: number;
  orderId: number;
  orderNumber: string;
  status: DeliveryRequestStatus;
  shopName: string;
  pickupCity: string | null;
  pickupAddress: string | null;
  buyerFullName: string;
  deliveryAddress: string | null;
  totalAmount: number;
  createdAt: string;
}
