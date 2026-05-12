import { useState, useMemo } from 'react';
import { RefreshCw } from 'lucide-react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { SpendingDataPoint } from '../../types/dashboard';
import Skeleton from '../Skeleton';

function getStartOfWeek(): Date {
  const now = new Date();
  const day = now.getDay();
  const diff = day === 0 ? -6 : 1 - day;
  const start = new Date(now);
  start.setDate(now.getDate() + diff);
  start.setHours(0, 0, 0, 0);
  return start;
}

interface SpendingOverviewProps {
  data: SpendingDataPoint[];
  loading: boolean;
  onRefresh: () => Promise<void>;
}

export default function SpendingOverview({ data, loading, onRefresh }: SpendingOverviewProps) {
  const [period, setPeriod] = useState<'week' | 'month'>('week');
  const [refreshing, setRefreshing] = useState(false);

  const filteredData = useMemo(() => {
    if (period === 'month') return data;
    const start = getStartOfWeek();
    const end = new Date(start);
    end.setDate(start.getDate() + 7);
    return data.filter(p => {
      const d = new Date(p.date);
      return d >= start && d < end;
    });
  }, [data, period]);

  const handleRefresh = async () => {
    if (refreshing) return;
    setRefreshing(true);
    try { await onRefresh(); }
    catch { }
    finally { setRefreshing(false); }
  };

  return (
    <div className="lg:col-span-2 bg-brand-card p-8 rounded-[2.5rem] border border-brand-border shadow-sm transition-colors duration-300">
      <div className="flex justify-between items-center mb-6">
        <div className="flex items-center gap-2">
          <h3 className="font-bold text-brand-primary text-xl">Spending Overview</h3>
          {!loading && (
            <button
              onClick={handleRefresh}
              disabled={refreshing}
              className="p-1.5 rounded-lg hover:bg-brand-primary/5 text-brand-text-muted hover:text-brand-primary transition-all disabled:opacity-50"
            >
              <RefreshCw size={16} className={refreshing ? 'animate-spin' : ''} />
            </button>
          )}
        </div>
        <select
          value={period}
          onChange={(e) => setPeriod(e.target.value as 'week' | 'month')}
          className="bg-brand-bg border border-brand-border text-brand-primary text-sm rounded-lg px-3 py-2 outline-none"
        >
          <option value="week">This Week</option>
          <option value="month">This Month</option>
        </select>
      </div>
      <div className="h-64 w-full">
        {loading ? (
          <div className="flex items-end gap-3 h-full px-4 pb-4">
            {[65, 40, 80, 55, 90, 35, 70].map((h, i) => (
              <Skeleton
                key={i}
                height={`${h}%`}
                borderRadius="0.5rem 0.5rem 0 0"
                style={{ flex: 1 }}
              />
            ))}
          </div>
        ) : (
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={filteredData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
              <defs>
                <linearGradient id="colorSpent" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#141414" stopOpacity={0.3} className="dark:stopColor-white" />
                  <stop offset="95%" stopColor="#141414" stopOpacity={0} className="dark:stopColor-white" />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e5e7eb" className="dark:stroke-zinc-800" />
              <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: '#9ca3af', fontSize: 12 }} dy={10} />
              <YAxis axisLine={false} tickLine={false} tick={{ fill: '#9ca3af', fontSize: 12 }} />
              <Tooltip
                contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                cursor={{ stroke: '#9ca3af', strokeWidth: 1, strokeDasharray: '3 3' }}
              />
              <Area type="monotone" dataKey="spent" stroke="#141414" strokeWidth={3} fillOpacity={1} fill="url(#colorSpent)" className="dark:stroke-white" />
            </AreaChart>
          </ResponsiveContainer>
        )}
      </div>
    </div>
  );
}
