// src/components/ui/StatusBadge.jsx
export default function StatusBadge({ status, large }) {
  const map = {
    PENDING:     'bg-amber-500/20 text-amber-400 border-amber-500/30',
    IN_PROGRESS: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
    COMPLETED:   'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
    REJECTED:    'bg-red-500/20 text-red-400 border-red-500/30',
    CANCELLED:   'bg-slate-600/40 text-slate-400 border-slate-600',
  };
  const base = map[status] || 'bg-slate-700 text-slate-300';
  return (
    <span className={`inline-flex items-center border rounded-full font-medium ${large ? 'px-3 py-1.5 text-sm' : 'px-2.5 py-0.5 text-xs'} ${base}`}>
      {status?.replace('_', ' ')}
    </span>
  );
}
