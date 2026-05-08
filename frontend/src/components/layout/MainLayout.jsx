// src/components/layout/MainLayout.jsx
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const NAV = [
  { to: '/dashboard', icon: '📊', label: 'Dashboard' },
  { to: '/requests',  icon: '📋', label: 'All Requests' },
  { to: '/requests/new', icon: '➕', label: 'New Request' },
  { to: '/analytics',    icon: '📈', label: 'Analytics' },
];

export default function MainLayout() {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => { logout(); navigate('/login'); };

  return (
    <div className="min-h-screen bg-slate-950 flex">
      {/* Sidebar */}
      <aside className="w-64 flex-shrink-0 bg-slate-900 border-r border-slate-800 flex flex-col">
        {/* Logo */}
        <div className="p-5 border-b border-slate-800">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 bg-blue-600 rounded-lg flex items-center justify-center flex-shrink-0">
              <svg className="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                  d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
              </svg>
            </div>
            <div>
              <p className="text-white font-bold text-sm leading-tight">DSR Portal</p>
              <p className="text-slate-500 text-xs">Tool-48</p>
            </div>
          </div>
        </div>

        {/* Nav */}
        <nav className="flex-1 p-3 space-y-1">
          {NAV.map(({ to, icon, label }) => (
            <NavLink key={to} to={to} end={to === '/dashboard'}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors
                 ${isActive
                   ? 'bg-blue-600/20 text-blue-400 border border-blue-500/30'
                   : 'text-slate-400 hover:text-white hover:bg-slate-800'}`}>
              <span className="w-5 text-center">{icon}</span>
              {label}
            </NavLink>
          ))}

          {isAdmin && (
            <>
              <div className="pt-4 pb-1 px-3">
                <p className="text-slate-600 text-xs font-semibold uppercase tracking-wider">Admin</p>
              </div>
              <NavLink to="/audit"
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors
                   ${isActive ? 'bg-blue-600/20 text-blue-400' : 'text-slate-400 hover:text-white hover:bg-slate-800'}`}>
                <span className="w-5 text-center">📜</span>
                Audit Log
              </NavLink>
            </>
          )}
        </nav>

        {/* User */}
        <div className="p-3 border-t border-slate-800">
          <div className="flex items-center gap-3 px-3 py-2">
            <div className="w-8 h-8 rounded-full bg-blue-600/30 border border-blue-500/30 flex items-center justify-center flex-shrink-0">
              <span className="text-blue-400 text-xs font-bold">
                {user?.username?.[0]?.toUpperCase() || 'U'}
              </span>
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-white text-sm font-medium truncate">{user?.username}</p>
              <p className="text-slate-500 text-xs">{user?.role}</p>
            </div>
            <button onClick={handleLogout}
              className="text-slate-500 hover:text-red-400 transition text-lg" title="Sign out">
              ⏻
            </button>
          </div>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 overflow-auto">
        <div className="max-w-7xl mx-auto p-6 lg:p-8">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
