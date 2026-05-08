// src/pages/RequestsListPage.jsx
import { useEffect, useState, useCallback } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { dsrApi } from '../services/api';
import { useDebounce } from '../hooks/index';
import { useAuth } from '../context/AuthContext';
import StatusBadge from '../components/ui/StatusBadge';
import PriorityBadge from '../components/ui/PriorityBadge';

const REQUEST_TYPES = ['ACCESS','ERASURE','RECTIFICATION','PORTABILITY','RESTRICTION','OBJECTION'];
const STATUSES      = ['PENDING','IN_PROGRESS','COMPLETED','REJECTED','CANCELLED'];
const PRIORITIES    = ['LOW','MEDIUM','HIGH','CRITICAL'];

function TableSkeleton() {
  return Array.from({ length: 8 }).map((_, i) => (
    <tr key={i} className="border-b border-slate-700/50 animate-pulse">
      {Array.from({ length: 7 }).map((_, j) => (
        <td key={j} className="px-4 py-3">
          <div className="h-4 bg-slate-700 rounded w-full" />
        </td>
      ))}
    </tr>
  ));
}

export default function RequestsListPage() {
  const { isManager } = useAuth();
  const navigate      = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  // Filters
  const [query,    setQuery]    = useState(searchParams.get('q')           || '');
  const [status,   setStatus]   = useState(searchParams.get('status')      || '');
  const [type,     setType]     = useState(searchParams.get('requestType') || '');
  const [priority, setPriority] = useState(searchParams.get('priority')    || '');
  const [from,     setFrom]     = useState('');
  const [to,       setTo]       = useState('');

  // Pagination
  const [page,  setPage]  = useState(0);
  const [size]            = useState(20);

  // Data
  const [data,    setData]    = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [loading, setLoading] = useState(false);

  const debouncedQuery = useDebounce(query, 400);

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const params = {
        q:           debouncedQuery || undefined,
        status:      status   || undefined,
        requestType: type     || undefined,
        priority:    priority || undefined,
        from:        from     || undefined,
        to:          to       || undefined,
        page, size,
      };
      const result = await dsrApi.search(params);
      setData(result);
    } catch (err) {
      console.error('Failed to fetch requests', err);
    } finally {
      setLoading(false);
    }
  }, [debouncedQuery, status, type, priority, from, to, page, size]);

  useEffect(() => { fetchData(); }, [fetchData]);

  // Reset to page 0 when filters change
  useEffect(() => { setPage(0); }, [debouncedQuery, status, type, priority, from, to]);

  const handleExport = async () => {
    try {
      const res = await dsrApi.export(status || undefined);
      const url  = URL.createObjectURL(new Blob([res.data], { type: 'text/csv' }));
      const link = document.createElement('a');
      link.href     = url;
      link.download = 'dsr_export.csv';
      link.click();
      URL.revokeObjectURL(url);
    } catch { alert('Export failed — please try again.'); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Soft-delete this request? It can be recovered by an administrator.')) return;
    try {
      await dsrApi.delete(id);
      fetchData();
    } catch { alert('Delete failed.'); }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-white">DSR Requests</h1>
          <p className="text-slate-400 text-sm mt-0.5">
            {data.totalElements} total request{data.totalElements !== 1 ? 's' : ''}
          </p>
        </div>
        <div className="flex gap-2">
          <button onClick={handleExport}
            className="flex items-center gap-2 px-3 py-2 text-sm text-slate-300 border border-slate-600 rounded-lg hover:border-slate-400 hover:text-white transition">
            ⬇ Export CSV
          </button>
          <Link to="/requests/new"
            className="flex items-center gap-2 px-4 py-2 text-sm bg-blue-600 hover:bg-blue-500 text-white font-semibold rounded-lg transition">
            + New Request
          </Link>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="bg-slate-800 border border-slate-700 rounded-xl p-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 xl:grid-cols-6 gap-3">
          {/* Search */}
          <div className="xl:col-span-2 relative">
            <span className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">🔍</span>
            <input
              type="text" placeholder="Search name, email, description…"
              value={query} onChange={(e) => setQuery(e.target.value)}
              className="w-full pl-9 pr-3 py-2 bg-slate-900/60 border border-slate-600 rounded-lg text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* Status */}
          <select value={status} onChange={(e) => setStatus(e.target.value)}
            className="px-3 py-2 bg-slate-900/60 border border-slate-600 rounded-lg text-sm text-white focus:outline-none focus:ring-2 focus:ring-blue-500">
            <option value="">All Statuses</option>
            {STATUSES.map((s) => <option key={s} value={s}>{s.replace('_',' ')}</option>)}
          </select>

          {/* Type */}
          <select value={type} onChange={(e) => setType(e.target.value)}
            className="px-3 py-2 bg-slate-900/60 border border-slate-600 rounded-lg text-sm text-white focus:outline-none focus:ring-2 focus:ring-blue-500">
            <option value="">All Types</option>
            {REQUEST_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
          </select>

          {/* Priority */}
          <select value={priority} onChange={(e) => setPriority(e.target.value)}
            className="px-3 py-2 bg-slate-900/60 border border-slate-600 rounded-lg text-sm text-white focus:outline-none focus:ring-2 focus:ring-blue-500">
            <option value="">All Priorities</option>
            {PRIORITIES.map((p) => <option key={p} value={p}>{p}</option>)}
          </select>

          {/* Clear */}
          <button onClick={() => { setQuery(''); setStatus(''); setType(''); setPriority(''); setFrom(''); setTo(''); }}
            className="px-3 py-2 text-sm text-slate-400 hover:text-white border border-slate-700 rounded-lg transition">
            Clear Filters
          </button>
        </div>
      </div>

      {/* Table */}
      <div className="bg-slate-800 border border-slate-700 rounded-xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-700 bg-slate-900/40">
                {['ID','Subject','Email','Type','Status','Priority','Deadline','Actions'].map((h) => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-slate-400 uppercase tracking-wider">
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <TableSkeleton />
              ) : data.content.length === 0 ? (
                <tr>
                  <td colSpan={8} className="px-4 py-16 text-center text-slate-500">
                    <div className="flex flex-col items-center gap-2">
                      <span className="text-4xl">📭</span>
                      <p>No requests found</p>
                      {(query || status || type) && (
                        <button onClick={() => { setQuery(''); setStatus(''); setType(''); setPriority(''); }}
                          className="text-blue-400 hover:text-blue-300 text-xs underline">
                          Clear filters
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ) : (
                data.content.map((r) => (
                  <tr key={r.id}
                    className={`border-b border-slate-700/50 hover:bg-slate-700/30 transition-colors cursor-pointer
                      ${r.isOverdue ? 'bg-red-900/10' : ''}`}
                    onClick={() => navigate(`/requests/${r.id}`)}>
                    <td className="px-4 py-3 text-slate-400 font-mono">#{r.id}</td>
                    <td className="px-4 py-3">
                      <div className="text-white font-medium">{r.subjectName}</div>
                      {r.isOverdue && <span className="text-red-400 text-xs font-semibold">OVERDUE</span>}
                    </td>
                    <td className="px-4 py-3 text-slate-300">{r.subjectEmail}</td>
                    <td className="px-4 py-3 text-slate-300">{r.requestType}</td>
                    <td className="px-4 py-3"><StatusBadge status={r.status} /></td>
                    <td className="px-4 py-3"><PriorityBadge priority={r.priority} /></td>
                    <td className="px-4 py-3 text-slate-400">
                      {r.deadlineDate
                        ? <span className={r.isOverdue ? 'text-red-400 font-semibold' : ''}>
                            {new Date(r.deadlineDate).toLocaleDateString()}
                          </span>
                        : '—'}
                    </td>
                    <td className="px-4 py-3" onClick={(e) => e.stopPropagation()}>
                      <div className="flex gap-1">
                        <button onClick={() => navigate(`/requests/${r.id}/edit`)}
                          className="px-2 py-1 text-xs text-blue-400 hover:text-blue-300 border border-blue-500/30 rounded hover:bg-blue-500/10 transition">
                          Edit
                        </button>
                        {isManager && (
                          <button onClick={() => handleDelete(r.id)}
                            className="px-2 py-1 text-xs text-red-400 hover:text-red-300 border border-red-500/30 rounded hover:bg-red-500/10 transition">
                            Del
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {data.totalPages > 1 && (
          <div className="border-t border-slate-700 px-4 py-3 flex items-center justify-between">
            <p className="text-slate-400 text-xs">
              Page {page + 1} of {data.totalPages} · {data.totalElements} total
            </p>
            <div className="flex gap-1">
              <button onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page === 0}
                className="px-3 py-1.5 text-xs text-slate-300 border border-slate-600 rounded disabled:opacity-40 hover:border-slate-400 transition">
                ← Prev
              </button>
              {Array.from({ length: Math.min(5, data.totalPages) }, (_, i) => {
                const pageNum = Math.max(0, Math.min(page - 2, data.totalPages - 5)) + i;
                return (
                  <button key={pageNum} onClick={() => setPage(pageNum)}
                    className={`px-3 py-1.5 text-xs border rounded transition ${
                      pageNum === page
                        ? 'bg-blue-600 border-blue-600 text-white'
                        : 'text-slate-300 border-slate-600 hover:border-slate-400'
                    }`}>
                    {pageNum + 1}
                  </button>
                );
              })}
              <button onClick={() => setPage((p) => Math.min(data.totalPages - 1, p + 1))}
                disabled={page >= data.totalPages - 1}
                className="px-3 py-1.5 text-xs text-slate-300 border border-slate-600 rounded disabled:opacity-40 hover:border-slate-400 transition">
                Next →
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
