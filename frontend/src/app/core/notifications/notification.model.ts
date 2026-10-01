/** Cf. docs/03-modele-donnees.md §2.24 (table `notifications`). */
export interface AppNotification {
  id: number;
  type: string;
  title: string;
  message: string;
  linkUrl: string | null;
  readAt: string | null;
  createdAt: string;
}
