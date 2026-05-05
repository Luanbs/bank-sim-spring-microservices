import React from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { Home, User, Settings, Activity, CreditCard, LogOut, Sun, Moon } from 'lucide-react';
import { useTheme } from '../context/ThemeContext';
import { useAuth } from '../context/AuthContext';

export default function Layout() {
  const { theme, toggleTheme } = useTheme();
  const { logout } = useAuth();

  return (
    <div className="h-screen overflow-hidden bg-brand-bg flex transition-colors duration-300">
      {/* Sidebar */}
      <aside className="w-64 bg-brand-card border-r border-brand-border flex flex-col transition-colors duration-300 h-full flex-shrink-0">
        <div className="p-6">
          <h1 className="text-2xl font-bold text-brand-primary flex items-center gap-2">
            <div className="w-8 h-8 bg-brand-primary rounded-lg flex items-center justify-center transition-colors duration-300">
              <CreditCard className="text-brand-bg w-5 h-5 transition-colors duration-300" />
            </div>
            BankSim
          </h1>
        </div>

        <nav className="flex-1 px-4 space-y-1">
          <NavItem to="/" icon={<Home size={20} />} label="Dashboard" />
          <NavItem to="/account" icon={<User size={20} />} label="Account" />
          <NavItem to="/activity" icon={<Activity size={20} />} label="Activity" />
          <NavItem to="/settings" icon={<Settings size={20} />} label="Settings" />
        </nav>

        <div className="p-4 border-t border-brand-border space-y-2">
          <button 
            onClick={toggleTheme}
            className="flex items-center gap-3 px-4 py-2 text-brand-text-muted hover:text-brand-primary hover:bg-brand-primary/5 rounded-lg w-full transition-all"
          >
            {theme === 'light' ? <Moon size={20} /> : <Sun size={20} />}
            <span className="font-medium">{theme === 'light' ? 'Dark Mode' : 'Light Mode'}</span>
          </button>
          
          <button 
            onClick={logout}
            className="flex items-center gap-3 px-4 py-2 text-brand-text-muted hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg w-full transition-all"
          >
            <LogOut size={20} />
            <span className="font-medium">Logout</span>
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 overflow-y-auto relative h-full">
        <div className="max-w-5xl mx-auto p-8">
          <Outlet />
        </div>

        {/* Floating Theme Toggle */}
        <button
          onClick={toggleTheme}
          className="fixed bottom-8 right-8 w-14 h-14 bg-brand-primary text-brand-bg rounded-full shadow-2xl flex items-center justify-center hover:scale-110 active:scale-95 transition-all z-40 border border-brand-bg/10"
          aria-label="Toggle Theme"
        >
          {theme === 'light' ? <Moon size={24} /> : <Sun size={24} />}
        </button>
      </main>
    </div>
  );
}

function NavItem({ to, icon, label }: { to: string; icon: React.ReactNode; label: string }) {
  return (
    <NavLink
      to={to}
      className={({ isActive }) =>
        `flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-200 ${
          isActive
            ? 'bg-brand-primary text-brand-bg shadow-lg shadow-brand-primary/10'
            : 'text-brand-text-muted hover:bg-brand-primary/5 hover:text-brand-primary'
        }`
      }
    >
      {icon}
      <span className="font-medium">{label}</span>
    </NavLink>
  );
}
