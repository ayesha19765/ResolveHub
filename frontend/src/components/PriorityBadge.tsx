import React from 'react';
import { TicketPriority } from '../types/ticket';

interface PriorityBadgeProps {
  priority: TicketPriority | string;
}

const priorityConfig: Record<string, { label: string; bg: string; text: string }> = {
  LOW: {
    label: 'Low',
    bg: 'bg-slate-100 text-slate-700 border-slate-200',
    text: 'text-slate-700',
  },
  MEDIUM: {
    label: 'Medium',
    bg: 'bg-blue-50 text-blue-700 border-blue-200',
    text: 'text-blue-700',
  },
  HIGH: {
    label: 'High',
    bg: 'bg-orange-50 text-orange-700 border-orange-200',
    text: 'text-orange-700',
  },
  CRITICAL: {
    label: 'Critical',
    bg: 'bg-rose-50 text-rose-700 border-rose-200 font-semibold',
    text: 'text-rose-700',
  },
};

export const PriorityBadge: React.FC<PriorityBadgeProps> = ({ priority }) => {
  const config = priorityConfig[priority] || {
    label: priority,
    bg: 'bg-slate-100 text-slate-700 border-slate-200',
    text: 'text-slate-700',
  };

  return (
    <span className={`inline-flex items-center px-2 py-0.5 text-xs rounded border ${config.bg}`}>
      {config.label}
    </span>
  );
};
