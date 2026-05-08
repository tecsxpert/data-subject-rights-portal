// src/pages/RequestDetailPage.jsx
import { useEffect, useState } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { dsrApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import StatusBadge from '../components/ui/StatusBadge';
import PriorityBadge from '../components/ui/PriorityBadge';

export default function RequestDetailPage() {
  const { id }       = useParams();
  const navigate     = useNavigate();
  const { isManager }= useAuth();

  const [request, setRequest] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setTab]   = useState('details'); // 'details' | 'ai' | 'audit'

  useEffect(() => {
    dsrApi.getById(id)
      .then(setRequest)
      .catch(() => navigate('/requests'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleDelete = async () => {
    if (!window.confirm('Soft-delete this request?')) return;
    await dsrApi.delete(id);
    navigate('/requests');
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full" />
      </div>
    );
  }

  if (!request) return null;

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Back + Actions */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <button onClick={() => navigate('/requests')}
            className="p-2 text-slate-400 hover:text-white border border-slate-700 rounded-lg transition">
            ←
          </button>
          <div>
            <h1 className="text-xl font-bold text-white">DSR Request #{request.id}</h1>
            <p className="text-slate-400 text-xs mt-0.5">
              Submitted {new Date(request.createdAt).toLocaleDateString('en-IN', {
                day:'numeric', month:'short', year:'numeric'
              })}
            </p>
          </div>
        </div>
        <div className="flex gap-2">
          <Link to={`/requests/${id}/edit`}
            className="px-4 py-2 text-sm text-blue-400 border border-blue-500/30 rounded-lg hover:bg-blue-500/10 transition">
            Edit
          </Link>
          {isManager && (
            <button onClick={handleDelete}
              className="px-4 py-2 text-sm text-red-400 border border-red-500/30 rounded-lg hover:bg-red-500/10 transition">
              Delete
            </button>
          )}
        </div>
      </div>

      {/* Overdue banner */}
      {request.isOverdue && (
        <div className="p-3 bg-red-500/10 border border-red-500/30 rounded-xl text-red-400 text-sm font-semibold">
          ⚠ This request is overdue. Deadline was {new Date(request.deadlineDate).toLocaleDateString()}.
        </div>
      )}

      {/* AI Fallback notice */}
      {request.isAiFallback && (
        <div className="p-3 bg-amber-500/10 border border-amber-500/30 rounded-xl text-amber-400 text-sm">
          ⚡ AI service was unavailable — showing fallback template. AI enrichment will retry shortly.
        </div>
      )}

      {/* Status row */}
      <div className="flex flex-wrap gap-3">
        <StatusBadge status={request.status} large />
        <PriorityBadge priority={request.priority} large />
        <span className="px-3 py-1.5 text-sm rounded-full bg-slate-700 text-slate-300">
          {request.requestType}
        </span>
        {request.hasFile && (
          <span className="px-3 py-1.5 text-sm rounded-full bg-emerald-500/20 text-emerald-400">
            📎 Document attached
          </span>
        )}
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-700">
        {[['details','Details'], ['ai','AI Insights'], ['timeline','Timeline']].map(([key, label]) => (
          <button key={key} onClick={() => setTab(key)}
            className={`px-5 py-3 text-sm font-medium border-b-2 transition ${
              activeTab === key
                ? 'border-blue-500 text-blue-400'
                : 'border-transparent text-slate-400 hover:text-white'
            }`}>
            {label}
          </button>
        ))}
      </div>

      {/* Tab: Details */}
      {activeTab === 'details' && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="bg-slate-800 border border-slate-700 rounded-xl p-6 space-y-4">
            <h3 className="text-slate-400 text-xs font-semibold uppercase tracking-wider">Subject</h3>
            <DataRow label="Name"  value={request.subjectName} />
            <DataRow label="Email" value={request.subjectEmail} />
          </div>

          <div className="bg-slate-800 border border-slate-700 rounded-xl p-6 space-y-4">
            <h3 className="text-slate-400 text-xs font-semibold uppercase tracking-wider">Request</h3>
            <DataRow label="Type"        value={request.requestType} />
            <DataRow label="Assigned To" value={request.assignedToName || 'Unassigned'} />
            <DataRow label="Deadline"    value={request.deadlineDate
              ? new Date(request.deadlineDate).toLocaleDateString() : '—'} />
            <DataRow label="Resolved"    value={request.resolvedAt
              ? new Date(request.resolvedAt).toLocaleString() : '—'} />
          </div>

          {request.description && (
            <div className="md:col-span-2 bg-slate-800 border border-slate-700 rounded-xl p-6">
              <h3 className="text-slate-400 text-xs font-semibold uppercase tracking-wider mb-3">Description</h3>
              <p className="text-slate-300 text-sm leading-relaxed whitespace-pre-wrap">{request.description}</p>
            </div>
          )}
        </div>
      )}

      {/* Tab: AI Insights */}
      {activeTab === 'ai' && (
        <div className="space-y-6">
          {/* AI Description */}
          <div className="bg-slate-800 border border-slate-700 rounded-xl p-6">
            <div className="flex items-center gap-2 mb-4">
              <span className="text-xl">🤖</span>
              <h3 className="text-white font-semibold">AI Description</h3>
              {request.isAiFallback && (
                <span className="text-xs px-2 py-0.5 bg-amber-500/20 text-amber-400 rounded-full">Fallback</span>
              )}
            </div>
            {request.aiDescription ? (
              <p className="text-slate-300 text-sm leading-relaxed">{request.aiDescription}</p>
            ) : (
              <div className="flex items-center gap-2 text-slate-500 text-sm">
                <svg className="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
                AI enrichment in progress…
              </div>
            )}
          </div>

          {/* AI Recommendations */}
          {request.aiRecommendations && (
            <div className="bg-slate-800 border border-slate-700 rounded-xl p-6">
              <div className="flex items-center gap-2 mb-4">
                <span className="text-xl">💡</span>
                <h3 className="text-white font-semibold">AI Recommendations</h3>
              </div>
              <div className="space-y-3">
                {(Array.isArray(request.aiRecommendations)
                  ? request.aiRecommendations
                  : [request.aiRecommendations]
                ).map((rec, i) => (
                  <div key={i} className="p-4 bg-slate-900/40 rounded-lg border border-slate-700">
                    <div className="flex items-start gap-3">
                      <span className={`flex-shrink-0 px-2 py-0.5 text-xs rounded-full font-semibold
                        ${rec.priority === 'HIGH' ? 'bg-red-500/20 text-red-400' :
                          rec.priority === 'MEDIUM' ? 'bg-amber-500/20 text-amber-400' :
                          'bg-slate-600 text-slate-300'}`}>
                        {rec.priority || 'MEDIUM'}
                      </span>
                      <div>
                        <p className="text-white text-sm font-medium">{rec.action_type}</p>
                        <p className="text-slate-400 text-sm mt-0.5">{rec.description}</p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* AI Report */}
          {request.aiReport && (
            <div className="bg-slate-800 border border-slate-700 rounded-xl p-6">
              <div className="flex items-center gap-2 mb-4">
                <span className="text-xl">📄</span>
                <h3 className="text-white font-semibold">AI Report</h3>
              </div>
              <div className="space-y-3 text-sm text-slate-300">
                {request.aiReport.summary && (
                  <div>
                    <p className="text-slate-400 text-xs uppercase tracking-wide mb-1">Summary</p>
                    <p className="leading-relaxed">{request.aiReport.summary}</p>
                  </div>
                )}
                {request.aiReport.key_items && (
                  <div>
                    <p className="text-slate-400 text-xs uppercase tracking-wide mb-1">Key Items</p>
                    <ul className="list-disc pl-4 space-y-1">
                      {request.aiReport.key_items.map((item, i) => (
                        <li key={i}>{item}</li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            </div>
          )}

          {!request.aiDescription && !request.aiRecommendations && (
            <div className="text-center py-12 text-slate-500">
              <p className="text-4xl mb-2">⏳</p>
              <p>AI insights are being generated. Refresh in a moment.</p>
            </div>
          )}
        </div>
      )}

      {/* Tab: Timeline */}
      {activeTab === 'timeline' && (
        <div className="bg-slate-800 border border-slate-700 rounded-xl p-6">
          <div className="relative pl-6 border-l-2 border-slate-700 space-y-6">
            {[
              { label: 'Request submitted', time: request.createdAt,  icon: '📋', color: 'bg-blue-500' },
              request.resolvedAt && { label: `Marked as ${request.status.replace('_',' ')}`, time: request.resolvedAt, icon: '✅', color: 'bg-emerald-500' },
            ].filter(Boolean).map((event, i) => (
              <div key={i} className="relative flex items-start gap-3">
                <div className={`absolute -left-[1.375rem] w-4 h-4 rounded-full ${event.color} flex items-center justify-center text-xs`} />
                <div className="pt-0.5">
                  <p className="text-white text-sm font-medium">{event.label}</p>
                  <p className="text-slate-500 text-xs">{new Date(event.time).toLocaleString()}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

function DataRow({ label, value }) {
  return (
    <div className="flex justify-between items-start gap-4">
      <span className="text-slate-500 text-sm flex-shrink-0">{label}</span>
      <span className="text-white text-sm text-right">{value}</span>
    </div>
  );
}
