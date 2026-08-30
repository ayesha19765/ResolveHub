import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ticketApi } from '../api/ticketApi';
import { TicketResponse } from '../types/ticket';
import { StatusBadge } from '../components/StatusBadge';
import { PriorityBadge } from '../components/PriorityBadge';
import { LoadingSpinner } from '../components/LoadingSpinner';
import {
  Ticket,
  Clock,
  CheckCircle2,
  AlertCircle,
  PlusCircle,
  ArrowRight,
  TrendingUp,
  FolderKanban
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const [tickets, setTickets] = useState<TicketResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setIsLoading(true);
        // Load recent 100 tickets to calculate summary metrics
        const res = await ticketApi.searchTickets({ page: 0, size: 50, sort: 'createdAt', direction: 'desc' });
        setTickets(res.content);
      } catch (err: any) {
        setError(err.message || 'Failed to load dashboard metrics');
      } finally {
        setIsLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (isLoading) {
    return <LoadingSpinner message="Loading dashboard metrics..." />;
  }

  if (error) {
    return (
      <div className="p-4 bg-rose-50 border border-rose-200 rounded-lg text-rose-700 flex items-center gap-3">
        <AlertCircle className="w-5 h-5" />
        <span>{error}</span>
      </div>
    );
  }

  const totalCount = tickets.length;
  const openCount = tickets.filter((t) => t.status === 'OPEN').length;
  const inProgressCount = tickets.filter((t) => t.status === 'IN_PROGRESS').length;
  const resolvedCount = tickets.filter((t) => t.status === 'RESOLVED' || t.status === 'CLOSED').length;
  const criticalCount = tickets.filter((t) => t.priority === 'CRITICAL' || t.priority === 'HIGH').length;

  return (
    <div className="space-y-8">
      {/* Welcome Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-gradient-to-r from-blue-900 to-indigo-900 rounded-2xl p-6 text-white shadow-md">
        <div>
          <h1 className="text-2xl font-bold">Welcome back, {user?.name || user?.email}!</h1>
          <p className="mt-1 text-sm text-blue-200">
            ResolveHub Issue Tracking System &middot; Active Role: <span className="font-semibold text-white uppercase">{user?.role}</span>
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Link
            to="/tickets/new"
            className="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white text-sm font-semibold rounded-xl transition shadow-sm"
          >
            <PlusCircle className="w-4 h-4" />
            New Ticket
          </Link>
          <Link
            to="/tickets"
            className="inline-flex items-center gap-2 px-4 py-2 bg-white/10 hover:bg-white/20 text-white text-sm font-medium rounded-xl transition backdrop-blur-sm"
          >
            View All
            <ArrowRight className="w-4 h-4" />
          </Link>
        </div>
      </div>

      {/* Metrics Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center gap-4">
          <div className="p-3 bg-blue-50 text-blue-600 rounded-xl">
            <Ticket className="w-6 h-6" />
          </div>
          <div>
            <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Total Tickets</div>
            <div className="text-2xl font-bold text-slate-900">{totalCount}</div>
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center gap-4">
          <div className="p-3 bg-sky-50 text-sky-600 rounded-xl">
            <AlertCircle className="w-6 h-6" />
          </div>
          <div>
            <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Open Tickets</div>
            <div className="text-2xl font-bold text-slate-900">{openCount}</div>
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center gap-4">
          <div className="p-3 bg-amber-50 text-amber-600 rounded-xl">
            <Clock className="w-6 h-6" />
          </div>
          <div>
            <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider">In Progress</div>
            <div className="text-2xl font-bold text-slate-900">{inProgressCount}</div>
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center gap-4">
          <div className="p-3 bg-emerald-50 text-emerald-600 rounded-xl">
            <CheckCircle2 className="w-6 h-6" />
          </div>
          <div>
            <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Resolved / Closed</div>
            <div className="text-2xl font-bold text-slate-900">{resolvedCount}</div>
          </div>
        </div>
      </div>

      {/* Recent Tickets & System Highlights */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
          <div className="p-5 border-b border-slate-200 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <TrendingUp className="w-5 h-5 text-blue-600" />
              <h2 className="text-base font-bold text-slate-900">Recent Issues</h2>
            </div>
            <Link to="/tickets" className="text-xs font-semibold text-blue-600 hover:text-blue-700">
              View all ({totalCount}) &rarr;
            </Link>
          </div>

          <div className="divide-y divide-slate-100">
            {tickets.slice(0, 5).map((ticket) => (
              <Link
                key={ticket.id}
                to={`/tickets/${ticket.id}`}
                className="p-4 flex items-center justify-between hover:bg-slate-50 transition block"
              >
                <div className="flex items-start gap-3">
                  <span className="text-xs font-semibold text-slate-400 pt-0.5">#{ticket.id}</span>
                  <div>
                    <h3 className="text-sm font-medium text-slate-900 hover:text-blue-600 transition">
                      {ticket.title}
                    </h3>
                    <div className="flex items-center gap-2 mt-1 text-xs text-slate-500">
                      <span>{ticket.projectName}</span>
                      <span>&middot;</span>
                      <span>Reporter: {ticket.reporterName}</span>
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <PriorityBadge priority={ticket.priority} />
                  <StatusBadge status={ticket.status} />
                </div>
              </Link>
            ))}

            {tickets.length === 0 && (
              <div className="p-8 text-center text-sm text-slate-500">
                No tickets recorded yet. Create the first ticket!
              </div>
            )}
          </div>
        </div>

        {/* Backend & Architecture Card */}
        <div className="space-y-6">
          <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm space-y-4">
            <div className="flex items-center gap-2 text-slate-900 font-bold text-base">
              <FolderKanban className="w-5 h-5 text-blue-600" />
              <h3>Backend Architecture</h3>
            </div>
            <p className="text-xs text-slate-600 leading-relaxed">
              ResolveHub operates on a production Spring Boot 3 &amp; PostgreSQL 17 stack.
              Every action in this UI maps directly to authenticated, role-verified REST controllers.
            </p>
            <div className="space-y-2 pt-2 border-t border-slate-100 text-xs">
              <div className="flex justify-between text-slate-600">
                <span>Security Mode:</span>
                <span className="font-medium text-slate-900">HTTP Basic + BCrypt</span>
              </div>
              <div className="flex justify-between text-slate-600">
                <span>Persistence:</span>
                <span className="font-medium text-slate-900">PostgreSQL (Volume)</span>
              </div>
              <div className="flex justify-between text-slate-600">
                <span>Query Engine:</span>
                <span className="font-medium text-slate-900">JPA Specifications</span>
              </div>
              <div className="flex justify-between text-slate-600">
                <span>High Priority Issues:</span>
                <span className="font-semibold text-orange-600">{criticalCount}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
