export interface TicketActivityResponse {
  id: number;
  action: string;
  description: string;
  oldValue: string | null;
  newValue: string | null;
  createdAt: string;
}
