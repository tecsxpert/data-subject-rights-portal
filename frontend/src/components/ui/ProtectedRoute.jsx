// src/components/ui/ProtectedRoute.jsx
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function ProtectedRoute({ children, requiredRole }) {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="animate-spin w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full" />
      </div>
    );
  }

  if (!user) return <Navigate to="/login" replace />;

  if (requiredRole && user.role !== requiredRole && user.role !== 'ADMIN') {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}
