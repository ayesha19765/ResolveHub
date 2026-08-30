import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Ticket, PlusCircle, ExternalLink, ShieldCheck } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const Sidebar: React.FC = () => {
  const { user } = useAuth();

  const navItems = [
    { to: '/', label: 'Dashboard', icon: LayoutDashboard, exact: true },
    { to: '/tickets', label: 'Tickets', icon: Ticket },
    { to: '/tickets/new', label: 'Create Ticket', icon: PlusCircle },
  ];

  return (
    <aside className="w-64 bg-slate-900 text-slate-300 flex flex-col shrink-0 min-h-[calc(100vh-4rem)] border-r border-slate-800">
      <div className="p-4 flex-1">
        <div className="text-[11px] font-bold uppercase tracking-wider text-slate-400 px-3 mb-2">
          Navigation
        </div>
        <nav className="space-y-1">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.exact}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition ${
                  isActive
                    ? 'bg-blue-600 text-white shadow-sm'
                    : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                }`
              }
            >
              <item.icon className="w-4 h-4" />
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="mt-8 pt-4 border-t border-slate-800">
          <div className="text-[11px] font-bold uppercase tracking-wider text-slate-400 px-3 mb-2">
            System & API
          </div>
          <a
            href="http://localhost:8081/swagger-ui.html"
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center justify-between px-3 py-2 rounded-lg text-xs font-medium text-slate-400 hover:bg-slate-800 hover:text-slate-200 transition"
          >
            <span className="flex items-center gap-2">
              <ShieldCheck className="w-4 h-4 text-emerald-400" />
              OpenAPI / Swagger UI
            </span>
            <ExternalLink className="w-3.5 h-3.5" />
          </a>
        </div>
      </div>

      {/* Role permission info footer */}
      <div className="p-4 border-t border-slate-800 bg-slate-950/50">
        <div className="text-xs text-slate-400">
          <div className="font-semibold text-slate-300 mb-1">Active RBAC Session:</div>
          <div className="text-[11px] leading-relaxed">
            {user?.role === 'ADMIN' && 'Full permissions: Create, Edit, Assign, Transition & Delete.'}
            {user?.role === 'AGENT' && 'Agent permissions: Create, Edit, Assign & Status Transitions.'}
            {user?.role === 'REPORTER' && 'Reporter permissions: Create Tickets & Post Comments.'}
          </div>
        </div>
      </div>
    </aside>
  );
};
