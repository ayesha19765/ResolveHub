import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { ticketApi } from '../api/ticketApi';
import { TicketResponse } from '../types/ticket';
import { Page, TicketFilterParams } from '../types/api';
import { StatusBadge } from '../components/StatusBadge';
import { PriorityBadge } from '../components/PriorityBadge';
import { Pagination } from '../components/Pagination';
import { LoadingSpinner } from '../components/LoadingSpinner';
import {
  Search,
  Filter,
  PlusCircle,
  RotateCcw,
  ArrowUpDown,
  AlertCircle,
  Ticket as TicketIcon
} from 'lucide-react';

export const TicketListPage: React.FC = () => {
  const [pageData, setPageData] = useState<Page<TicketResponse> | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Filter state
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');
  const [priority, setPriority] = useState('');
  const [projectId, setProjectId] = useState<string>('');
  const [assigneeId, setAssigneeId] = useState<string>('');
  const [reporterId, setReporterId] = useState<string>('');

  // Pagination & Sorting state
  const [page, setPage] = useState(0);
  const [pageSize] = useState(10);
  const [sortField, setSortField] = useState('createdAt');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('desc');

  const loadTickets = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const params: TicketFilterParams = {
        page,
        size: pageSize,
        sort: sortField,
        direction: sortDirection,
      };

      if (search.trim()) params.search = search.trim();
      if (status) params.status = status;
      if (priority) params.priority = priority;
      if (projectId.trim() && !isNaN(Number(projectId))) params.projectId = Number(projectId);
      if (assigneeId.trim() && !isNaN(Number(assigneeId))) params.assigneeId = Number(assigneeId);
      if (reporterId.trim() && !isNaN(Number(reporterId))) params.reporterId = Number(reporterId);

      const res = await ticketApi.searchTickets(params);
      setPageData(res);
    } catch (err: any) {
      setError(err.message || 'Failed to search tickets');
    } finally {
      setIsLoading(false);
    }
  }, [page, pageSize, sortField, sortDirection, search, status, priority, projectId, assigneeId, reporterId]);

  useEffect(() => {
    loadTickets();
  }, [loadTickets]);

  const handleResetFilters = () => {
    setSearch('');
    setStatus('');
    setPriority('');
    setProjectId('');
    setAssigneeId('');
    setReporterId('');
    setSortField('createdAt');
    setSortDirection('desc');
    setPage(0);
  };

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    loadTickets();
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Tickets</h1>
          <p className="text-sm text-slate-500 mt-1">
            Dynamic search &amp; filter powered by Spring Data JPA Specifications
          </p>
        </div>
        <Link
          to="/tickets/new"
          className="inline-flex items-center gap-2 px-4 py-2.5 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold rounded-lg transition shadow-sm self-start sm:self-auto"
        >
          <PlusCircle className="w-4 h-4" />
          Create Ticket
        </Link>
      </div>

      {/* Filter Toolbar */}
      <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm space-y-4">
        <form onSubmit={handleSearchSubmit} className="flex flex-col md:flex-row gap-3">
          <div className="relative flex-1">
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search title or description..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-10 pr-4 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          <button
            type="submit"
            className="px-4 py-2 bg-slate-900 text-white text-sm font-medium rounded-lg hover:bg-slate-800 transition"
          >
            Search
          </button>
          <button
            type="button"
            onClick={handleResetFilters}
            className="inline-flex items-center gap-1.5 px-3.5 py-2 text-sm font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            Reset
          </button>
        </form>

        {/* Filter Dropdowns */}
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3 pt-3 border-t border-slate-100">
          <div>
            <label className="block text-[11px] font-semibold uppercase tracking-wider text-slate-500 mb-1">
              Status
            </label>
            <select
              value={status}
              onChange={(e) => {
                setStatus(e.target.value);
                setPage(0);
              }}
              className="w-full py-1.5 px-2.5 text-xs border border-slate-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500"
            >
              <option value="">All Statuses</option>
              <option value="OPEN">Open</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="RESOLVED">Resolved</option>
              <option value="CLOSED">Closed</option>
            </select>
          </div>

          <div>
            <label className="block text-[11px] font-semibold uppercase tracking-wider text-slate-500 mb-1">
              Priority
            </label>
            <select
              value={priority}
              onChange={(e) => {
                setPriority(e.target.value);
                setPage(0);
              }}
              className="w-full py-1.5 px-2.5 text-xs border border-slate-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500"
            >
              <option value="">All Priorities</option>
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
              <option value="CRITICAL">Critical</option>
            </select>
          </div>

          <div>
            <label className="block text-[11px] font-semibold uppercase tracking-wider text-slate-500 mb-1">
              Project ID
            </label>
            <input
              type="number"
              placeholder="e.g. 1"
              value={projectId}
              onChange={(e) => {
                setProjectId(e.target.value);
                setPage(0);
              }}
              className="w-full py-1.5 px-2.5 text-xs border border-slate-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-[11px] font-semibold uppercase tracking-wider text-slate-500 mb-1">
              Assignee ID
            </label>
            <input
              type="number"
              placeholder="e.g. 2"
              value={assigneeId}
              onChange={(e) => {
                setAssigneeId(e.target.value);
                setPage(0);
              }}
              className="w-full py-1.5 px-2.5 text-xs border border-slate-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-[11px] font-semibold uppercase tracking-wider text-slate-500 mb-1">
              Sort Field
            </label>
            <select
              value={sortField}
              onChange={(e) => {
                setSortField(e.target.value);
                setPage(0);
              }}
              className="w-full py-1.5 px-2.5 text-xs border border-slate-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500"
            >
              <option value="createdAt">Created At</option>
              <option value="updatedAt">Updated At</option>
              <option value="priority">Priority</option>
              <option value="status">Status</option>
              <option value="title">Title</option>
              <option value="id">ID</option>
            </select>
          </div>

          <div>
            <label className="block text-[11px] font-semibold uppercase tracking-wider text-slate-500 mb-1">
              Direction
            </label>
            <select
              value={sortDirection}
              onChange={(e) => {
                setSortDirection(e.target.value as 'asc' | 'desc');
                setPage(0);
              }}
              className="w-full py-1.5 px-2.5 text-xs border border-slate-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500"
            >
              <option value="desc">Descending</option>
              <option value="asc">Ascending</option>
            </select>
          </div>
        </div>
      </div>

      {/* Error Notice */}
      {error && (
        <div className="p-4 bg-rose-50 border border-rose-200 rounded-lg text-rose-700 text-sm flex items-center gap-3">
          <AlertCircle className="w-5 h-5 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Ticket Table Card */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        {isLoading ? (
          <LoadingSpinner message="Searching tickets..." />
        ) : !pageData || pageData.content.length === 0 ? (
          <div className="p-12 text-center text-slate-500 space-y-3">
            <TicketIcon className="w-10 h-10 mx-auto text-slate-300" />
            <div className="text-base font-semibold text-slate-700">No tickets found</div>
            <p className="text-xs text-slate-400 max-w-sm mx-auto">
              No matching tickets found with active filters. Try adjusting query parameters or create a new ticket.
            </p>
            <button
              onClick={handleResetFilters}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-blue-600 bg-blue-50 border border-blue-200 rounded-lg hover:bg-blue-100 transition mt-2"
            >
              Reset Filters
            </button>
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm text-slate-600">
                <thead className="bg-slate-50 text-slate-700 text-xs uppercase font-semibold border-b border-slate-200">
                  <tr>
                    <th scope="col" className="px-6 py-3.5">ID</th>
                    <th scope="col" className="px-6 py-3.5">Title</th>
                    <th scope="col" className="px-6 py-3.5">Status</th>
                    <th scope="col" className="px-6 py-3.5">Priority</th>
                    <th scope="col" className="px-6 py-3.5">Project</th>
                    <th scope="col" className="px-6 py-3.5">Assignee</th>
                    <th scope="col" className="px-6 py-3.5">Created</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {pageData.content.map((ticket) => (
                    <tr key={ticket.id} className="hover:bg-slate-50 transition">
                      <td className="px-6 py-4 font-mono font-medium text-slate-400 text-xs">
                        #{ticket.id}
                      </td>
                      <td className="px-6 py-4">
                        <Link
                          to={`/tickets/${ticket.id}`}
                          className="font-semibold text-slate-900 hover:text-blue-600 transition"
                        >
                          {ticket.title}
                        </Link>
                        <div className="text-xs text-slate-400 line-clamp-1 mt-0.5">
                          {ticket.description}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <StatusBadge status={ticket.status} />
                      </td>
                      <td className="px-6 py-4">
                        <PriorityBadge priority={ticket.priority} />
                      </td>
                      <td className="px-6 py-4 text-xs font-medium text-slate-700">
                        {ticket.projectName || `Project #${ticket.projectId}`}
                      </td>
                      <td className="px-6 py-4 text-xs">
                        {ticket.assigneeName ? (
                          <span className="font-medium text-slate-800">{ticket.assigneeName}</span>
                        ) : (
                          <span className="text-slate-400 italic">Unassigned</span>
                        )}
                      </td>
                      <td className="px-6 py-4 text-xs text-slate-500 whitespace-nowrap">
                        {new Date(ticket.createdAt).toLocaleDateString()}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination Controls */}
            <Pagination
              currentPage={pageData.number}
              totalPages={pageData.totalPages}
              totalElements={pageData.totalElements}
              pageSize={pageData.size}
              onPageChange={(newPage) => setPage(newPage)}
            />
          </>
        )}
      </div>
    </div>
  );
};
