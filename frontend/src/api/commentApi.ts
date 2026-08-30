import { request } from './client';
import { CommentResponse, CreateCommentRequest } from '../types/comment';
import { Page } from '../types/api';

export const commentApi = {
  getComments: async (ticketId: number, page = 0, size = 10): Promise<Page<CommentResponse>> => {
    return request<Page<CommentResponse>>(`/api/tickets/${ticketId}/comments?page=${page}&size=${size}`);
  },

  createComment: async (ticketId: number, data: CreateCommentRequest): Promise<CommentResponse> => {
    return request<CommentResponse>(`/api/tickets/${ticketId}/comments`, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },
};
