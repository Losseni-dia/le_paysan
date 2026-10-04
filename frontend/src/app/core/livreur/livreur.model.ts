export interface LivreurResponse {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  phone: string | null;
}

export interface CreateLivreurRequest {
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
}
