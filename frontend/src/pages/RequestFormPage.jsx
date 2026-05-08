// src/pages/RequestFormPage.jsx
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { dsrApi } from '../services/api';
import { useAuth } from '../context/AuthContext';

const REQUEST_TYPES = ['ACCESS','ERASURE','RECTIFICATION','PORTABILITY','RESTRICTION','OBJECTION'];
const PRIORITIES    = ['LOW','MEDIUM','HIGH','CRITICAL'];
const STATUSES      = ['PENDING','IN_PROGRESS','COMPLETED','REJECTED','CANCELLED'];

const TYPE_DESCRIPTIONS = {
  ACCESS:        'Request a copy of all personal data held about the subject.',
  ERASURE:       'Request deletion of all personal data (Right to be Forgotten).',
  RECTIFICATION: 'Request correction of inaccurate or incomplete data.',
  PORTABILITY:   'Request data in a machine-readable format for transfer.',
  RESTRICTION:   'Request that processing be paused pending a dispute.',
  OBJECTION:     'Object to processing for marketing, profiling, or research.',
};

export default function RequestFormPage() {
  const { id }    = useParams();
  const navigate  = useNavigate();
  const { isManager } = useAuth();
  const isEdit    = Boolean(id);

  const [form, setForm] = useState({
    subjectName:  '',
    subjectEmail: '',
    requestType:  'ACCESS',
    description:  '',
    priority:     'MEDIUM',
    status:       'PENDING',
    deadlineDate: '',
    assignedToId: '',
  });
  const [errors,  setErrors]  = useState({});
  const [loading, setLoading] = useState(false);
  const [fetching,setFetching]= useState(isEdit);
  const [file,    setFile]    = useState(null);

  useEffect(() => {
    if (!isEdit) return;
    dsrApi.getById(id).then((data) => {
      setForm({
        subjectName:  data.subjectName  || '',
        subjectEmail: data.subjectEmail || '',
        requestType:  data.requestType  || 'ACCESS',
        description:  data.description  || '',
        priority:     data.priority     || 'MEDIUM',
        status:       data.status       || 'PENDING',
        deadlineDate: data.deadlineDate || '',
        assignedToId: data.assignedToId || '',
      });
    }).finally(() => setFetching(false));
  }, [id, isEdit]);

  const validate = () => {
    const e = {};
    if (!form.subjectName.trim())  e.subjectName  = 'Subject name is required';
    if (!form.subjectEmail.trim()) e.subjectEmail = 'Email is required';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.subjectEmail))
      e.subjectEmail = 'Invalid email format';
    if (!form.requestType) e.requestType = 'Request type is required';
    if (form.description && form.description.length > 5000)
      e.description = 'Description must be under 5000 characters';
    return e;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length > 0) { setErrors(errs); return; }

    setLoading(true);
    try {
      const payload = {
        ...form,
        deadlineDate: form.deadlineDate || undefined,
        assignedToId: form.assignedToId ? Number(form.assignedToId) : undefined,
      };

      let saved;
      if (isEdit) {
        saved = await dsrApi.update(id, payload);
      } else {
        saved = await dsrApi.create(payload);
      }

      // Upload file if selected
      if (file && saved?.id) {
        await dsrApi.uploadFile(saved.id, file);
      }

      navigate(`/requests/${saved.id}`);
    } catch (err) {
      const msg = err.response?.data?.message || 'Something went wrong. Please try again.';
      setErrors({ _global: msg });
    } finally {
      setLoading(false);
    }
  };

  const set = (field) => (e) => {
    setForm((f) => ({ ...f, [field]: e.target.value }));
    setErrors((er) => ({ ...er, [field]: undefined }));
  };

  if (fetching) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full" />
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <button onClick={() => navigate(-1)}
          className="p-2 text-slate-400 hover:text-white border border-slate-700 rounded-lg transition">
          ←
        </button>
        <div>
          <h1 className="text-2xl font-bold text-white">
            {isEdit ? 'Edit DSR Request' : 'New DSR Request'}
          </h1>
          <p className="text-slate-400 text-sm mt-0.5">
            {isEdit ? `Editing request #${id}` : 'Submit a new Data Subject Rights request'}
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Global error */}
        {errors._global && (
          <div className="p-4 bg-red-500/10 border border-red-500/30 rounded-xl text-red-400 text-sm">
            {errors._global}
          </div>
        )}

        {/* Subject Information */}
        <div className="bg-slate-800 border border-slate-700 rounded-xl p-6 space-y-4">
          <h2 className="text-white font-semibold text-sm uppercase tracking-wider opacity-60">
            Subject Information
          </h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Field label="Subject Name *" error={errors.subjectName}>
              <input value={form.subjectName} onChange={set('subjectName')}
                placeholder="Full legal name"
                className={input(errors.subjectName)} />
            </Field>
            <Field label="Subject Email *" error={errors.subjectEmail}>
              <input type="email" value={form.subjectEmail} onChange={set('subjectEmail')}
                placeholder="email@example.com"
                className={input(errors.subjectEmail)} />
            </Field>
          </div>
        </div>

        {/* Request Details */}
        <div className="bg-slate-800 border border-slate-700 rounded-xl p-6 space-y-4">
          <h2 className="text-white font-semibold text-sm uppercase tracking-wider opacity-60">
            Request Details
          </h2>

          <Field label="Request Type *" error={errors.requestType}>
            <select value={form.requestType} onChange={set('requestType')} className={input()}>
              {REQUEST_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
            </select>
            {form.requestType && (
              <p className="mt-1.5 text-slate-500 text-xs">{TYPE_DESCRIPTIONS[form.requestType]}</p>
            )}
          </Field>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Field label="Priority">
              <select value={form.priority} onChange={set('priority')} className={input()}>
                {PRIORITIES.map((p) => <option key={p} value={p}>{p}</option>)}
              </select>
            </Field>

            {isEdit && (
              <Field label="Status">
                <select value={form.status} onChange={set('status')} className={input()}>
                  {STATUSES.map((s) => <option key={s} value={s}>{s.replace('_',' ')}</option>)}
                </select>
              </Field>
            )}
          </div>

          <Field label="Description" error={errors.description}>
            <textarea value={form.description} onChange={set('description')} rows={5}
              placeholder="Describe the request in detail. Include any relevant account identifiers, dates, or specific data categories."
              className={input(errors.description) + ' resize-none'} />
            <p className="mt-1 text-slate-500 text-xs text-right">
              {form.description.length}/5000
            </p>
          </Field>

          <Field label="Deadline Date">
            <input type="date" value={form.deadlineDate} onChange={set('deadlineDate')}
              min={new Date().toISOString().split('T')[0]}
              className={input()} />
          </Field>
        </div>

        {/* Supporting Document */}
        <div className="bg-slate-800 border border-slate-700 rounded-xl p-6 space-y-4">
          <h2 className="text-white font-semibold text-sm uppercase tracking-wider opacity-60">
            Supporting Document (optional)
          </h2>
          <div className="border-2 border-dashed border-slate-600 rounded-lg p-6 text-center hover:border-slate-500 transition cursor-pointer"
            onClick={() => document.getElementById('file-input').click()}>
            <input id="file-input" type="file" className="hidden"
              accept=".pdf,.jpg,.jpeg,.png,.doc,.docx"
              onChange={(e) => setFile(e.target.files[0])} />
            {file ? (
              <div className="flex items-center justify-center gap-2 text-green-400">
                <span>📎</span>
                <span className="text-sm font-medium">{file.name}</span>
                <button type="button" onClick={(e) => { e.stopPropagation(); setFile(null); }}
                  className="text-slate-400 hover:text-red-400 ml-2">✕</button>
              </div>
            ) : (
              <div className="text-slate-500 space-y-1">
                <p className="text-2xl">📤</p>
                <p className="text-sm">Click to upload or drag & drop</p>
                <p className="text-xs">PDF, JPEG, PNG, DOC, DOCX · Max 10 MB</p>
              </div>
            )}
          </div>
        </div>

        {/* Actions */}
        <div className="flex gap-3 justify-end">
          <button type="button" onClick={() => navigate(-1)}
            className="px-5 py-2.5 text-sm text-slate-300 border border-slate-600 rounded-lg hover:border-slate-400 hover:text-white transition">
            Cancel
          </button>
          <button type="submit" disabled={loading}
            className="px-6 py-2.5 text-sm bg-blue-600 hover:bg-blue-500 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold rounded-lg transition flex items-center gap-2">
            {loading ? (
              <><svg className="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
              </svg>Saving…</>
            ) : isEdit ? 'Save Changes' : 'Submit Request'}
          </button>
        </div>
      </form>
    </div>
  );
}

// ── Small helpers ─────────────────────────────────────────────
function Field({ label, error, children }) {
  return (
    <div>
      <label className="block text-sm font-medium text-slate-300 mb-1.5">{label}</label>
      {children}
      {error && <p className="mt-1 text-red-400 text-xs">{error}</p>}
    </div>
  );
}

function input(error) {
  return `w-full px-3 py-2.5 bg-slate-900/60 border rounded-lg text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-2 transition
    ${error
      ? 'border-red-500/60 focus:ring-red-500'
      : 'border-slate-600 focus:ring-blue-500 focus:border-transparent'}`;
}
