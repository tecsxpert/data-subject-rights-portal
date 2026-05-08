// src/App.jsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { Toaster } from './components/ui/Toast';
import ProtectedRoute from './components/ui/ProtectedRoute';
import MainLayout     from './components/layout/MainLayout';
import LoginPage          from './pages/LoginPage';
import DashboardPage      from './pages/DashboardPage';
import RequestsListPage   from './pages/RequestsListPage';
import RequestDetailPage  from './pages/RequestDetailPage';
import RequestFormPage    from './pages/RequestFormPage';

function ErrorBoundaryFallback() {
  return (
    <div className="min-h-screen bg-slate-950 flex items-center justify-center text-white">
      <div className="text-center space-y-3">
        <p className="text-5xl">💥</p>
        <h2 className="text-2xl font-bold">Something went wrong</h2>
        <button onClick={() => window.location.reload()}
          className="px-4 py-2 bg-blue-600 rounded-lg text-sm hover:bg-blue-500 transition">
          Reload page
        </button>
      </div>
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/" element={<Navigate to="/dashboard" replace />} />

          {/* Protected */}
          <Route element={
            <ProtectedRoute>
              <MainLayout />
            </ProtectedRoute>
          }>
            <Route path="/dashboard"            element={<DashboardPage />} />
            <Route path="/requests"             element={<RequestsListPage />} />
            <Route path="/requests/new"         element={<RequestFormPage />} />
            <Route path="/requests/:id"         element={<RequestDetailPage />} />
            <Route path="/requests/:id/edit"    element={<RequestFormPage />} />
          </Route>

          {/* Catch-all */}
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
      <Toaster />
    </AuthProvider>
  );
}
