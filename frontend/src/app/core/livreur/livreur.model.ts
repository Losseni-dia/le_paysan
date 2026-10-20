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

export interface CreateLivreurRequest {
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
  city?: string;
  photo?: File | null;
  idDocumentFront?: File | null;
  idDocumentBack?: File | null;
  selfieWithId?: File | null;
}

export interface UpdateLivreurRequest {
  firstName: string;
  lastName: string;
  phone?: string;
  city?: string;
  photo?: File | null;
  idDocumentFront?: File | null;
  idDocumentBack?: File | null;
  selfieWithId?: File | null;
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
  shopId: number;
  shopName: string;
  createdAt: string;
}
