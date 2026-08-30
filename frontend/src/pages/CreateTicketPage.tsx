import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ticketApi } from '../api/ticketApi';
import { TicketPriority } from '../types/ticket';
import { useAuth } from '../context/AuthContext';
import { ArrowLeft, PlusCircle, AlertCircle } from 'lucide-react';

export const CreateTicketPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<TicketPriority>('MEDIUM');
  const [projectId, setProjectId] = useState<number>(1);
  const [reporterId, setReporterId] = useState<number>(user?.id || 1);

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Client-side UX validation
    const errors: Record<string, string> = {};
    if (!title.trim()) errors.title = 'Title must not be blank';
    if (!priority) errors.priority = 'Priority is required';
    if (!projectId || projectId < 1) errors.projectId = 'Valid Project ID is required';
    if (!reporterId || reporterId < 1) errors.reporterId = 'Valid Reporter ID is required';

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    setIsLoading(true);
    setError(null);
    setFieldErrors({});

    try {
      const created = await ticketApi.createTicket({
        title: title.trim(),
        description: description.trim(),
        priority,
        projectId,
        reporterId,
      });

      navigate(`/tickets/${created.id}`);
    } catch (err: any) {
      if (err.data?.fieldErrors) {
        setFieldErrors(err.data.fieldErrors);
      }
      setError(err.data?.message || err.message || 'Failed to create ticket.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <Link
          to="/tickets"
          className="inline-flex items-center gap-1.5 text-sm font-medium text-slate-600 hover:text-slate-900 transition"
        >
          <ArrowLeft className="w-4 h-4" />
          Back to Tickets
        </Link>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 sm:p-8 space-y-6">
        <div>
          <h1 className="text-xl font-bold text-slate-900">Create New Ticket</h1>
          <p className="text-xs text-slate-500 mt-1">
            Fill in issue details. Ticket status defaults to <span className="font-semibold text-slate-700">OPEN</span> and records a creation audit activity.
          </p>
        </div>

        {error && (
          <div className="p-3.5 bg-rose-50 border border-rose-200 rounded-lg text-rose-700 text-sm flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          {/* Title */}
          <div>
            <label htmlFor="title" className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1">
              Ticket Title *
            </label>
            <input
              id="title"
              type="text"
              required
              placeholder="e.g. Payment Gateway 500 Internal Error"
              value={title}
              onChange={(e) => {
                setTitle(e.target.value);
                if (fieldErrors.title) setFieldErrors((prev) => ({ ...prev, title: '' }));
              }}
              className={`w-full px-3.5 py-2 text-sm border rounded-lg focus:outline-none focus:ring-1 ${
                fieldErrors.title
                  ? 'border-rose-400 focus:ring-rose-500'
                  : 'border-slate-300 focus:ring-blue-500 focus:border-blue-500'
              }`}
            />
            {fieldErrors.title && (
              <p className="text-xs text-rose-600 mt-1">{fieldErrors.title}</p>
            )}
          </div>

          {/* Description */}
          <div>
            <label htmlFor="description" className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1">
              Description
            </label>
            <textarea
              id="description"
              rows={4}
              placeholder="Detailed reproduction steps, logs, or context..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full px-3.5 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          {/* Priority, Project ID, Reporter ID Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label htmlFor="priority" className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1">
                Priority *
              </label>
              <select
                id="priority"
                value={priority}
                onChange={(e) => setPriority(e.target.value as TicketPriority)}
                className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-blue-500"
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="CRITICAL">Critical</option>
              </select>
            </div>

            <div>
              <label htmlFor="projectId" className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1">
                Project ID *
              </label>
              <input
                id="projectId"
                type="number"
                min={1}
                required
                value={projectId}
                onChange={(e) => setProjectId(Number(e.target.value))}
                className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-blue-500"
              />
            </div>

            <div>
              <label htmlFor="reporterId" className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1">
                Reporter ID *
              </label>
              <input
                id="reporterId"
                type="number"
                min={1}
                required
                value={reporterId}
                onChange={(e) => setReporterId(Number(e.target.value))}
                className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-blue-500"
              />
            </div>
          </div>

          {/* Submit */}
          <div className="pt-4 border-t border-slate-100 flex justify-end gap-3">
            <Link
              to="/tickets"
              className="px-4 py-2 text-sm font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition"
            >
              Cancel
            </Link>
            <button
              type="submit"
              disabled={isLoading}
              className="inline-flex items-center gap-2 px-5 py-2 text-sm font-semibold text-white bg-blue-600 rounded-lg hover:bg-blue-700 disabled:opacity-50 transition shadow-sm"
            >
              <PlusCircle className="w-4 h-4" />
              {isLoading ? 'Creating...' : 'Create Ticket'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
