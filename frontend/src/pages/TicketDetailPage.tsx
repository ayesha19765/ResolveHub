import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ticketApi } from '../api/ticketApi';
import { commentApi } from '../api/commentApi';
import { activityApi } from '../api/activityApi';
import { TicketResponse, TicketStatus } from '../types/ticket';
import { CommentResponse } from '../types/comment';
import { TicketActivityResponse } from '../types/activity';
import { Page } from '../types/api';
import { StatusBadge } from '../components/StatusBadge';
import { PriorityBadge } from '../components/PriorityBadge';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ConfirmationModal } from '../components/ConfirmationModal';
import { Pagination } from '../components/Pagination';
import { useAuth } from '../context/AuthContext';
import {
  ArrowLeft,
  Trash2,
  UserCheck,
  PlayCircle,
  MessageSquare,
  History,
  Send,
  AlertCircle,
  CheckCircle2,
  Clock,
  User
} from 'lucide-react';

export const TicketDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const ticketId = Number(id);
  const navigate = useNavigate();
  const { user } = useAuth();

  const [ticket, setTicket] = useState<TicketResponse | null>(null);
  const [commentsPage, setCommentsPage] = useState<Page<CommentResponse> | null>(null);
  const [activities, setActivities] = useState<TicketActivityResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionSuccess, setActionSuccess] = useState<string | null>(null);

  // Status & Assign state
  const [statusUpdateLoading, setStatusUpdateLoading] = useState(false);
  const [assigneeInput, setAssigneeInput] = useState('');
  const [assignLoading, setAssignLoading] = useState(false);

  // Comment form state
  const [commentContent, setCommentContent] = useState('');
  const [commentLoading, setCommentLoading] = useState(false);
  const [commentPage, setCommentPage] = useState(0);

  // Delete modal state
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);

  const loadTicketData = useCallback(async () => {
    if (!ticketId || isNaN(ticketId)) {
      setError('Invalid ticket ID');
      setIsLoading(false);
      return;
    }

    try {
      setIsLoading(true);
      setError(null);

      const [ticketData, commentsData, activitiesData] = await Promise.all([
        ticketApi.getTicketById(ticketId),
        commentApi.getComments(ticketId, commentPage, 10),
        activityApi.getActivities(ticketId),
      ]);

      setTicket(ticketData);
      setCommentsPage(commentsData);
      setActivities(activitiesData);
      if (ticketData.assigneeId) {
        setAssigneeInput(ticketData.assigneeId.toString());
      }
    } catch (err: any) {
      setError(err.message || 'Failed to load ticket details');
    } finally {
      setIsLoading(false);
    }
  }, [ticketId, commentPage]);

  useEffect(() => {
    loadTicketData();
  }, [loadTicketData]);

  // Status Transition Handler
  const handleStatusTransition = async (newStatus: TicketStatus) => {
    if (!ticket) return;
    setStatusUpdateLoading(true);
    setError(null);
    setActionSuccess(null);

    try {
      const updated = await ticketApi.updateStatus(ticket.id, newStatus);
      setTicket(updated);
      setActionSuccess(`Status transitioned to ${newStatus}`);
      // Refresh activities
      const acts = await activityApi.getActivities(ticket.id);
      setActivities(acts);
    } catch (err: any) {
      setError(err.data?.message || err.message || 'Status transition failed');
    } finally {
      setStatusUpdateLoading(false);
    }
  };

  // Assignment Handler
  const handleAssign = async (andStart = false) => {
    if (!ticket || !assigneeInput.trim() || isNaN(Number(assigneeInput))) {
      setError('Please enter a valid numeric Assignee User ID.');
      return;
    }

    setAssignLoading(true);
    setError(null);
    setActionSuccess(null);

    try {
      const updated = andStart
        ? await ticketApi.assignAndStart(ticket.id, Number(assigneeInput))
        : await ticketApi.assignTicket(ticket.id, Number(assigneeInput));

      setTicket(updated);
      setActionSuccess(
        andStart
          ? `Assigned to user #${assigneeInput} and started (IN_PROGRESS)`
          : `Assigned to user #${assigneeInput}`
      );
      // Refresh activities
      const acts = await activityApi.getActivities(ticket.id);
      setActivities(acts);
    } catch (err: any) {
      setError(err.data?.message || err.message || 'Assignment failed');
    } finally {
      setAssignLoading(false);
    }
  };

  // Create Comment Handler
  const handleAddComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!ticket || !commentContent.trim()) return;

    setCommentLoading(true);
    setError(null);

    try {
      await commentApi.createComment(ticket.id, {
        authorId: user?.id || 1,
        content: commentContent.trim(),
      });
      setCommentContent('');
      setActionSuccess('Comment added successfully');
      // Reload comments
      const commentsData = await commentApi.getComments(ticket.id, 0, 10);
      setCommentsPage(commentsData);
      setCommentPage(0);
    } catch (err: any) {
      setError(err.data?.message || err.message || 'Failed to post comment');
    } finally {
      setCommentLoading(false);
    }
  };

  // Delete Ticket Handler
  const handleDeleteTicket = async () => {
    if (!ticket) return;
    setDeleteLoading(true);
    setError(null);

    try {
      await ticketApi.deleteTicket(ticket.id);
      setIsDeleteModalOpen(false);
      navigate('/tickets', { replace: true });
    } catch (err: any) {
      setError(err.data?.message || err.message || 'Delete operation failed');
      setIsDeleteModalOpen(false);
    } finally {
      setDeleteLoading(false);
    }
  };

  if (isLoading) {
    return <LoadingSpinner message="Loading ticket details..." />;
  }

  if (error && !ticket) {
    return (
      <div className="space-y-4">
        <Link to="/tickets" className="inline-flex items-center gap-2 text-sm text-blue-600 hover:text-blue-700">
          <ArrowLeft className="w-4 h-4" /> Back to tickets
        </Link>
        <div className="p-5 bg-rose-50 border border-rose-200 rounded-xl text-rose-700 flex items-center gap-3">
          <AlertCircle className="w-6 h-6 shrink-0" />
          <div>
            <h3 className="font-semibold">Error Loading Ticket</h3>
            <p className="text-sm">{error}</p>
          </div>
        </div>
      </div>
    );
  }

  if (!ticket) return null;

  const isAgentOrAdmin = user?.role === 'AGENT' || user?.role === 'ADMIN';
  const isAdmin = user?.role === 'ADMIN';

  return (
    <div className="space-y-6 max-w-5xl mx-auto pb-12">
      {/* Breadcrumb & Navigation */}
      <div className="flex items-center justify-between">
        <Link
          to="/tickets"
          className="inline-flex items-center gap-1.5 text-sm font-medium text-slate-600 hover:text-slate-900 transition"
        >
          <ArrowLeft className="w-4 h-4" />
          Back to Tickets
        </Link>

        {/* ADMIN Delete Button */}
        {isAdmin && (
          <button
            type="button"
            onClick={() => setIsDeleteModalOpen(true)}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-rose-700 bg-rose-50 border border-rose-200 rounded-lg hover:bg-rose-100 transition"
          >
            <Trash2 className="w-3.5 h-3.5" />
            Delete Ticket
          </button>
        )}
      </div>

      {/* Action Messages */}
      {actionSuccess && (
        <div className="p-3.5 bg-emerald-50 border border-emerald-200 rounded-lg text-emerald-800 text-sm flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 shrink-0 text-emerald-600" />
          <span>{actionSuccess}</span>
        </div>
      )}

      {error && (
        <div className="p-3.5 bg-rose-50 border border-rose-200 rounded-lg text-rose-700 text-sm flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Main Ticket Header Card */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 space-y-4">
        <div className="flex flex-wrap items-center gap-3">
          <span className="font-mono text-xs font-bold text-slate-400">#{ticket.id}</span>
          <StatusBadge status={ticket.status} size="md" />
          <PriorityBadge priority={ticket.priority} />
          <span className="text-xs text-slate-400 ml-auto flex items-center gap-1">
            <Clock className="w-3.5 h-3.5" />
            Created {new Date(ticket.createdAt).toLocaleString()}
          </span>
        </div>

        <h1 className="text-xl sm:text-2xl font-bold text-slate-900 leading-snug">
          {ticket.title}
        </h1>

        <div className="pt-3 border-t border-slate-100">
          <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-2">Description</h3>
          <p className="text-sm text-slate-700 leading-relaxed whitespace-pre-wrap">
            {ticket.description || 'No description provided.'}
          </p>
        </div>
      </div>

      {/* Role-Aware Actions (Status & Assignment) for AGENT & ADMIN */}
      {isAgentOrAdmin && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Status Workflow Card */}
          <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm space-y-3">
            <div className="flex items-center gap-2 text-slate-900 font-bold text-sm">
              <PlayCircle className="w-4 h-4 text-blue-600" />
              <h3>Status Workflow Transition</h3>
            </div>
            <p className="text-xs text-slate-500">
              Valid transitions: OPEN &rarr; IN_PROGRESS &rarr; RESOLVED &rarr; CLOSED
            </p>

            <div className="flex flex-wrap gap-2 pt-1">
              {ticket.status === 'OPEN' && (
                <button
                  type="button"
                  disabled={statusUpdateLoading}
                  onClick={() => handleStatusTransition('IN_PROGRESS')}
                  className="px-3 py-1.5 text-xs font-semibold text-amber-700 bg-amber-50 border border-amber-300 rounded-lg hover:bg-amber-100 transition disabled:opacity-50"
                >
                  Start Progress &rarr;
                </button>
              )}

              {ticket.status === 'IN_PROGRESS' && (
                <button
                  type="button"
                  disabled={statusUpdateLoading}
                  onClick={() => handleStatusTransition('RESOLVED')}
                  className="px-3 py-1.5 text-xs font-semibold text-emerald-700 bg-emerald-50 border border-emerald-300 rounded-lg hover:bg-emerald-100 transition disabled:opacity-50"
                >
                  Mark Resolved &rarr;
                </button>
              )}

              {ticket.status === 'RESOLVED' && (
                <button
                  type="button"
                  disabled={statusUpdateLoading}
                  onClick={() => handleStatusTransition('CLOSED')}
                  className="px-3 py-1.5 text-xs font-semibold text-slate-700 bg-slate-100 border border-slate-300 rounded-lg hover:bg-slate-200 transition disabled:opacity-50"
                >
                  Close Ticket &rarr;
                </button>
              )}

              {ticket.status === 'CLOSED' && (
                <span className="text-xs text-slate-400 italic">Ticket is CLOSED (final state)</span>
              )}
            </div>
          </div>

          {/* Assignment Card */}
          <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm space-y-3">
            <div className="flex items-center gap-2 text-slate-900 font-bold text-sm">
              <UserCheck className="w-4 h-4 text-blue-600" />
              <h3>Assign Ticket</h3>
            </div>
            <p className="text-xs text-slate-500">Assign to support agent user ID</p>

            <div className="flex items-center gap-2 pt-1">
              <input
                type="number"
                placeholder="Agent User ID (e.g. 2)"
                value={assigneeInput}
                onChange={(e) => setAssigneeInput(e.target.value)}
                className="w-44 px-3 py-1.5 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-blue-500"
              />
              <button
                type="button"
                disabled={assignLoading}
                onClick={() => handleAssign(false)}
                className="px-3 py-1.5 text-xs font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition disabled:opacity-50"
              >
                Assign
              </button>
              {ticket.status === 'OPEN' && (
                <button
                  type="button"
                  disabled={assignLoading}
                  onClick={() => handleAssign(true)}
                  className="px-3 py-1.5 text-xs font-semibold text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition disabled:opacity-50"
                >
                  Assign &amp; Start
                </button>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Metadata Overview Card */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 bg-white p-5 rounded-xl border border-slate-200 shadow-sm text-xs">
        <div>
          <span className="text-slate-400 font-medium uppercase tracking-wider block mb-1">Project</span>
          <span className="font-semibold text-slate-900">{ticket.projectName || `Project #${ticket.projectId}`}</span>
        </div>
        <div>
          <span className="text-slate-400 font-medium uppercase tracking-wider block mb-1">Reporter</span>
          <span className="font-semibold text-slate-900">{ticket.reporterName || `User #${ticket.reporterId}`}</span>
        </div>
        <div>
          <span className="text-slate-400 font-medium uppercase tracking-wider block mb-1">Assignee</span>
          <span className="font-semibold text-slate-900">{ticket.assigneeName || 'Unassigned'}</span>
        </div>
        <div>
          <span className="text-slate-400 font-medium uppercase tracking-wider block mb-1">Last Updated</span>
          <span className="font-semibold text-slate-900">{new Date(ticket.updatedAt).toLocaleDateString()}</span>
        </div>
      </div>

      {/* Two Column Layout: Comments Thread & Activity Timeline */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Comments Section */}
        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-5">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100">
            <div className="flex items-center gap-2 font-bold text-slate-900 text-sm">
              <MessageSquare className="w-4 h-4 text-blue-600" />
              <h3>Discussion Comments ({commentsPage?.totalElements || 0})</h3>
            </div>
          </div>

          {/* Comment Form */}
          <form onSubmit={handleAddComment} className="space-y-3">
            <textarea
              rows={3}
              placeholder="Write a comment..."
              value={commentContent}
              onChange={(e) => setCommentContent(e.target.value)}
              className="w-full p-3 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
            />
            <div className="flex items-center justify-between">
              <span className="text-[11px] text-slate-400">
                Posting as <span className="font-semibold text-slate-700">{user?.name || user?.email}</span>
              </span>
              <button
                type="submit"
                disabled={commentLoading || !commentContent.trim()}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-white bg-blue-600 rounded-lg hover:bg-blue-700 disabled:opacity-50 transition"
              >
                <Send className="w-3.5 h-3.5" />
                {commentLoading ? 'Posting...' : 'Post Comment'}
              </button>
            </div>
          </form>

          {/* Comments List */}
          <div className="space-y-3 pt-2">
            {commentsPage && commentsPage.content.length > 0 ? (
              commentsPage.content.map((comment) => (
                <div key={comment.id} className="p-3.5 bg-slate-50 border border-slate-200 rounded-lg space-y-1.5">
                  <div className="flex items-center justify-between text-xs">
                    <div className="flex items-center gap-1.5 font-semibold text-slate-900">
                      <User className="w-3.5 h-3.5 text-slate-400" />
                      {comment.authorName}
                    </div>
                    <span className="text-[11px] text-slate-400">
                      {new Date(comment.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>
                  <p className="text-xs text-slate-700 leading-relaxed whitespace-pre-wrap">{comment.content}</p>
                </div>
              ))
            ) : (
              <p className="text-xs text-slate-400 italic text-center py-4">No comments on this ticket yet.</p>
            )}

            {commentsPage && commentsPage.totalPages > 1 && (
              <Pagination
                currentPage={commentsPage.number}
                totalPages={commentsPage.totalPages}
                totalElements={commentsPage.totalElements}
                pageSize={commentsPage.size}
                onPageChange={(p) => setCommentPage(p)}
              />
            )}
          </div>
        </div>

        {/* Activity Timeline Section */}
        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
          <div className="flex items-center gap-2 pb-3 border-b border-slate-100 font-bold text-slate-900 text-sm">
            <History className="w-4 h-4 text-blue-600" />
            <h3>Activity History Log</h3>
          </div>

          <div className="space-y-4 pt-2">
            {activities.length > 0 ? (
              <div className="relative pl-6 space-y-6 before:absolute before:left-2 before:top-2 before:bottom-2 before:w-0.5 before:bg-slate-200">
                {activities.map((act) => (
                  <div key={act.id} className="relative text-xs">
                    <div className="absolute -left-6 top-0.5 w-4 h-4 rounded-full bg-blue-600 border-2 border-white shadow-sm flex items-center justify-center" />
                    <div>
                      <div className="font-semibold text-slate-900">
                        {act.action.replace('_', ' ')}
                      </div>
                      <div className="text-slate-600 mt-0.5">
                        {act.description}
                      </div>
                      {act.oldValue && act.newValue && (
                        <div className="text-[11px] font-mono text-slate-500 bg-slate-100 px-2 py-0.5 rounded inline-block mt-1">
                          {act.oldValue} &rarr; {act.newValue}
                        </div>
                      )}
                      <div className="text-[10px] text-slate-400 mt-1">
                        {new Date(act.createdAt).toLocaleString()}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-xs text-slate-400 italic text-center py-4">No activity logged.</p>
            )}
          </div>
        </div>
      </div>

      {/* Confirmation Modal for Ticket Deletion */}
      <ConfirmationModal
        isOpen={isDeleteModalOpen}
        title="Delete Ticket"
        message={`Are you sure you want to permanently delete ticket #${ticket.id} (${ticket.title})? This will cascade removal to all activities and comments.`}
        confirmLabel={deleteLoading ? 'Deleting...' : 'Delete Ticket'}
        cancelLabel="Cancel"
        isDangerous={true}
        onConfirm={handleDeleteTicket}
        onCancel={() => setIsDeleteModalOpen(false)}
      />
    </div>
  );
};
