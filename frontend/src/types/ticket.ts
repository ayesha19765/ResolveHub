export type TicketStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface TicketResponse {
  id: number;
  title: string;
  description: string;
  status: TicketStatus;
  priority: TicketPriority;
  projectId: number;
  projectName: string;
  reporterId: number;
  reporterName: string;
  assigneeId: number | null;
  assigneeName: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTicketRequest {
  title: string;
  description: string;
  priority: TicketPriority;
  projectId: number;
  reporterId: number;
}

export interface UpdateTicketRequest {
  title: string;
  description: string;
  priority: TicketPriority;
}

export interface AssignTicketRequest {
  assigneeId: number;
}

export interface UpdateTicketStatusRequest {
  status: TicketStatus;
}
