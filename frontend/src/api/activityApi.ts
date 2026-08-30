import { request } from './client';
import { TicketActivityResponse } from '../types/activity';

export const activityApi = {
  getActivities: async (ticketId: number): Promise<TicketActivityResponse[]> => {
    return request<TicketActivityResponse[]>(`/api/tickets/${ticketId}/activities`);
  },
};
