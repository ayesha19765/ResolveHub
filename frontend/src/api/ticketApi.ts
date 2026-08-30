import { request } from './client';
import {
  TicketResponse,
  CreateTicketRequest,
  UpdateTicketRequest,
  AssignTicketRequest,
  UpdateTicketStatusRequest,
  TicketStatus
} from '../types/ticket';
import { Page, TicketFilterParams } from '../types/api';

export const ticketApi = {
  searchTickets: async (params: TicketFilterParams = {}): Promise<Page<TicketResponse>> => {
    const query = new URLSearchParams();
    if (params.status) query.append('status', params.status);
    if (params.priority) query.append('priority', params.priority);
    if (params.projectId !== undefined) query.append('projectId', params.projectId.toString());
    if (params.assigneeId !== undefined) query.append('assigneeId', params.assigneeId.toString());
    if (params.reporterId !== undefined) query.append('reporterId', params.reporterId.toString());
    if (params.search) query.append('search', params.search);
    if (params.createdAfter) query.append('createdAfter', params.createdAfter);
    if (params.createdBefore) query.append('createdBefore', params.createdBefore);
    if (params.page !== undefined) query.append('page', params.page.toString());
    if (params.size !== undefined) query.append('size', params.size.toString());
    if (params.sort) query.append('sort', params.sort);
    if (params.direction) query.append('direction', params.direction);

    const queryString = query.toString();
    return request<Page<TicketResponse>>(`/api/tickets${queryString ? `?${queryString}` : ''}`);
  },

  getTicketById: async (id: number): Promise<TicketResponse> => {
    return request<TicketResponse>(`/api/tickets/${id}`);
  },

  createTicket: async (data: CreateTicketRequest): Promise<TicketResponse> => {
    return request<TicketResponse>('/api/tickets', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  updateTicket: async (id: number, data: UpdateTicketRequest): Promise<TicketResponse> => {
    return request<TicketResponse>(`/api/tickets/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  },

  assignTicket: async (id: number, assigneeId: number): Promise<TicketResponse> => {
    const body: AssignTicketRequest = { assigneeId };
    return request<TicketResponse>(`/api/tickets/${id}/assignee`, {
      method: 'PATCH',
      body: JSON.stringify(body),
    });
  },

  updateStatus: async (id: number, status: TicketStatus): Promise<TicketResponse> => {
    const body: UpdateTicketStatusRequest = { status };
    return request<TicketResponse>(`/api/tickets/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify(body),
    });
  },

  assignAndStart: async (id: number, assigneeId: number): Promise<TicketResponse> => {
    const body: AssignTicketRequest = { assigneeId };
    return request<TicketResponse>(`/api/tickets/${id}/assign-and-start`, {
      method: 'PATCH',
      body: JSON.stringify(body),
    });
  },

  deleteTicket: async (id: number): Promise<void> => {
    return request<void>(`/api/tickets/${id}`, {
      method: 'DELETE',
    });
  },
};
