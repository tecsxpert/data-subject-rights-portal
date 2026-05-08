// src/components/ui/PriorityBadge.jsx
export default function PriorityBadge({ priority, large }) {
  const map = {
    LOW:      'bg-slate-600/40 text-slate-400',
    MEDIUM:   'bg-blue-500/20 text-blue-400',
    HIGH:     'bg-amber-500/20 text-amber-400',
    CRITICAL: 'bg-red-500/20 text-red-400',
  };
  const dot = {
    LOW: '⚪', MEDIUM: '🔵', HIGH: '🟡', CRITICAL: '🔴'
  };
  const base = map[priority] || 'bg-slate-700 text-slate-300';
  return (
    <span className={`inline-flex items-center gap-1 rounded-full font-medium ${large ? 'px-3 py-1.5 text-sm' : 'px-2.5 py-0.5 text-xs'} ${base}`}>
      <span className="text-xs">{dot[priority]}</span>
      {priority}
    </span>
  );
}
