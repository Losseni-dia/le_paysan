export interface AdminUserResponse {
  id: number;
  username: string;
  email: string | null;
  firstName: string;
  lastName: string;
  phone: string | null;
  enabled: boolean;
  roles: string[];
  createdAt: string;
}
