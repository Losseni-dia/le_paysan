export type CategoryStatus = 'EN_ATTENTE' | 'VALIDE';

export interface CategoryResponse {
  id: number;
  name: string;
  slug: string;
  status: CategoryStatus;
}
