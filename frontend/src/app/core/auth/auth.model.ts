export interface UserResponse {
  id: number;
  username: string;
  email: string | null;
  firstName: string;
  lastName: string;
  phone: string | null;
  hasSecuritySelfie: boolean;
  roles: string[];
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: UserResponse;
  roles: string[];
}

export interface RegisterRequest {
  username: string;
  password: string;
  email?: string;
  firstName: string;
  lastName: string;
  phone?: string;
  acceptCgu: boolean;
  acceptCgv: boolean;
  acceptPrivacy: boolean;
}

export interface LoginRequest {
  username: string;
  password: string;
}
