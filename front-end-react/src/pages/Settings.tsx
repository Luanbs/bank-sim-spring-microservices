import { Bell, Shield, Globe, Moon, Smartphone, HelpCircle, Sun } from 'lucide-react';
import { useTheme } from '../context/ThemeContext';

export default function Settings() {
  const { theme, toggleTheme } = useTheme();

  const settingsGroups = [
    {
      title: "General",
      items: [
        { icon: <Bell size={20} />, label: "Notifications", desc: "Email, push, and SMS alerts", toggle: true, active: true },
        { 
          icon: theme === 'light' ? <Moon size={20} /> : <Sun size={20} />, 
          label: "Dark Mode", 
          desc: "Switch between light and dark themes", 
          toggle: true, 
          active: theme === 'dark',
          onClick: toggleTheme 
        },
        { icon: <Globe size={20} />, label: "Language", desc: "English (US)", value: "Change" },
      ]
    },
    {
      title: "Security",
      items: [
        { icon: <Shield size={20} />, label: "Two-Factor Auth", desc: "Add an extra layer of security", toggle: true, active: true },
        { icon: <Smartphone size={20} />, label: "Active Sessions", desc: "Manage your logged-in devices", value: "View" },
      ]
    }
  ];

  return (
    <div className="space-y-8">
      <header>
        <h2 className="text-3xl font-bold text-brand-primary transition-colors duration-300">Settings</h2>
        <p className="text-brand-text-muted mt-1 transition-colors duration-300">Customize your experience and manage security.</p>
      </header>

      <div className="space-y-8">
        {settingsGroups.map((group, idx) => (
          <div key={idx} className="space-y-4">
            <h3 className="text-sm font-bold text-brand-text-muted uppercase tracking-wider px-2 transition-colors duration-300">{group.title}</h3>
            <div className="bg-brand-card rounded-3xl border border-brand-border divide-y divide-brand-border overflow-hidden shadow-sm transition-colors duration-300">
              {group.items.map((item, iIdx) => (
                <div key={iIdx} className="p-6 flex items-center justify-between hover:bg-brand-primary/5 transition-colors">
                  <div className="flex items-center gap-4">
                    <div className="w-10 h-10 rounded-xl bg-brand-bg flex items-center justify-center text-brand-text-muted transition-colors duration-300">
                      {item.icon}
                    </div>
                    <div>
                      <div className="font-bold text-brand-primary transition-colors duration-300">{item.label}</div>
                      <div className="text-sm text-brand-text-muted transition-colors duration-300">{item.desc}</div>
                    </div>
                  </div>
                  
                  {item.toggle ? (
                    <button 
                      onClick={item.onClick}
                      className={`w-12 h-6 rounded-full transition-colors relative ${item.active ? 'bg-brand-primary' : 'bg-brand-border'}`}
                    >
                      <div className={`absolute top-1 w-4 h-4 bg-white rounded-full transition-all ${item.active ? 'left-7' : 'left-1'}`} />
                    </button>
                  ) : (
                    <button className="text-sm font-bold text-brand-primary hover:underline transition-colors duration-300">{item.value}</button>
                  )}
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>

      <div className="bg-brand-primary text-brand-bg p-8 rounded-3xl flex items-center justify-between transition-colors duration-300">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-2xl bg-brand-bg/10 flex items-center justify-center">
            <HelpCircle size={24} />
          </div>
          <div>
            <h4 className="font-bold text-lg">Need help?</h4>
            <p className="opacity-70">Our support team is available 24/7.</p>
          </div>
        </div>
        <button className="px-6 py-2 bg-brand-bg text-brand-primary rounded-xl font-bold hover:opacity-90 transition-all active:scale-95">
          Contact Support
        </button>
      </div>
    </div>
  );
}
