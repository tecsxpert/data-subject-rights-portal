// src/pages/DashboardPage.jsx
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { dsrApi } from '../services/api';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend
} from 'recharts';

const STATUS_COLORS = {
  PENDING:     '#f59e0b',
  IN_PROGRESS: '#3b82f6',
  COMPLETED:   '#10b981',
  REJECTED:    '#ef4444',
  CANCELLED:   '#6b7280',
};

const PRIORITY_COLORS = {
  LOW:      '#6b7280',
  MEDIUM:   '#3b82f6',
  HIGH:     '#f59e0b',
  CRITICAL: '#ef4444',
};

function KpiCard({ label, value, sub, color, icon }) {
  return (
    <div className={`bg-slate-800 border border-slate-700 rounded-xl p-5 flex items-start gap-4`}>
      <div className={`w-12 h-12 rounded-xl flex items-center justify-center text-xl flex-shrink-0 ${color}`}>
        {icon}
      </div>
      <div>
        <p className="text-slate-400 text-sm">{label}</p>
        <p className="text-white text-3xl font-bold mt-0.5">{value ?? '—'}</p>
        {sub && <p className="text-slate-500 text-xs mt-0.5">{sub}</p>}
      </div>
    </div>
  );
}

function SkeletonCard() {
  return (
    <div className="bg-slate-800 border border-slate-700 rounded-xl p-5 animate-pulse">
      <div className="flex gap-4">
        <div className="w-12 h-12 bg-slate-700 rounded-xl" />
        <div className="flex-1 space-y-2 pt-1">
          <div className="h-3 bg-slate-700 rounded w-1/2" />
          <div className="h-8 bg-slate-700 rounded w-1/3" />
        </div>
      </div>
    </div>
  );
}

export default function DashboardPage() {
  const [stats,   setStats]   = useState(null);
  const [loading, setLoading] = useState(true);
  const [period,  setPeriod]  = useState('month'); // 'week' | 'month'

  useEffect(() => {
    dsrApi.getStats()
      .then(setStats)
      .finally(() => setLoading(false));
  }, []);

  const statusChartData = stats?.byStatus
    ? Object.entries(stats.byStatus).map(([name, value]) => ({ name, value }))
    : [];

  const typeChartData = stats?.byType
    ? Object.entries(stats.byType).map(([name, value]) => ({ name, value }))
    : [];

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">Dashboard</h1>
          <p className="text-slate-400 text-sm mt-1">Overview of all Data Subject Rights requests</p>
        </div>
        <Link
          to="/requests/new"
          className="inline-flex items-center gap-2 px-4 py-2.5 bg-blue-600 hover:bg-blue-500 text-white text-sm font-semibold rounded-lg transition-colors"
        >
          <span>+</span> New Request
        </Link>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {loading ? (
          Array.from({ length: 8 }).map((_, i) => <SkeletonCard key={i} />)
        ) : (
          <>
            <KpiCard label="Total Requests"   value={stats?.total}        color="bg-slate-700"     icon="📋" />
            <KpiCard label="Pending"          value={stats?.pending}      color="bg-amber-500/20"  icon="⏳" />
            <KpiCard label="In Progress"      value={stats?.inProgress}   color="bg-blue-500/20"   icon="🔄" />
            <KpiCard label="Completed"        value={stats?.completed}    color="bg-emerald-500/20" icon="✅" />
            <KpiCard label="Overdue"          value={stats?.overdue}      color="bg-red-500/20"    icon="🚨"
                     sub={stats?.overdue > 0 ? 'Requires immediate action' : 'All on track'} />
            <KpiCard label="New This Week"    value={stats?.newThisWeek}  color="bg-purple-500/20" icon="📈" />
            <KpiCard label="New This Month"   value={stats?.newThisMonth} color="bg-cyan-500/20"   icon="📅" />
            <KpiCard label="Avg Resolution"
                     value={stats?.avgResolutionHours ? `${Math.round(stats.avgResolutionHours)}h` : '—'}
                     color="bg-teal-500/20" icon="⚡"
                     sub="Average hours to resolve" />
          </>
        )}
      </div>

      {/* Charts */}
      {!loading && stats && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Status distribution */}
          <div className="bg-slate-800 border border-slate-700 rounded-xl p-6">
            <h3 className="text-white font-semibold mb-4">Requests by Status</h3>
            {statusChartData.length > 0 ? (
              <ResponsiveContainer width="100%" height={240}>
                <PieChart>
                  <Pie data={statusChartData} dataKey="value" nameKey="name"
                       cx="50%" cy="50%" outerRadius={90} label={({ name, percent }) =>
                         `${name} ${(percent * 100).toFixed(0)}%`}>
                    {statusChartData.map((entry) => (
                      <Cell key={entry.name} fill={STATUS_COLORS[entry.name] || '#6b7280'} />
                    ))}
                  </Pie>
                  <Tooltip contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: 8 }} />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <p className="text-slate-500 text-sm text-center py-16">No data yet</p>
            )}
          </div>

          {/* Request type bar chart */}
          <div className="bg-slate-800 border border-slate-700 rounded-xl p-6">
            <h3 className="text-white font-semibold mb-4">Requests by Type (this month)</h3>
            {typeChartData.length > 0 ? (
              <ResponsiveContainer width="100%" height={240}>
                <BarChart data={typeChartData} margin={{ top: 5, right: 10, left: -20, bottom: 5 }}>
                  <XAxis dataKey="name" tick={{ fill: '#94a3b8', fontSize: 11 }} />
                  <YAxis tick={{ fill: '#94a3b8', fontSize: 11 }} />
                  <Tooltip contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: 8 }} />
                  <Bar dataKey="value" fill="#3b82f6" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <p className="text-slate-500 text-sm text-center py-16">No data yet</p>
            )}
          </div>
        </div>
      )}

      {/* Quick links */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        {[
          { to: '/requests?status=PENDING',     label: 'View Pending Requests', icon: '⏳', color: 'border-amber-500/30 hover:border-amber-500' },
          { to: '/requests?status=IN_PROGRESS', label: 'View In-Progress',      icon: '🔄', color: 'border-blue-500/30 hover:border-blue-500' },
          { to: '/requests',                    label: 'All Requests',           icon: '📋', color: 'border-slate-600 hover:border-slate-400' },
        ].map(({ to, label, icon, color }) => (
          <Link key={to} to={to}
            className={`bg-slate-800 border ${color} rounded-xl p-4 flex items-center gap-3 text-slate-300 hover:text-white transition-all duration-200`}>
            <span className="text-xl">{icon}</span>
            <span className="text-sm font-medium">{label}</span>
            <span className="ml-auto text-slate-500">→</span>
          </Link>
        ))}
      </div>
    </div>
  );
}
