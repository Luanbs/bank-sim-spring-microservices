import { useState } from 'react';
import { Calendar, Zap, RefreshCw } from 'lucide-react';
import { Bill } from '../../types/dashboard';
import Skeleton from '../Skeleton';

const BILL_ICON_COLORS: Record<string, string> = {
  entertainment: 'text-red-500',
  utilities: 'text-yellow-500',
  internet: 'text-blue-500',
  subscription: 'text-red-500',
};

interface UpcomingBillsProps {
  bills: Bill[];
  loading: boolean;
  onRefresh: () => Promise<void>;
}

export default function UpcomingBills({ bills, loading, onRefresh }: UpcomingBillsProps) {
  const [refreshing, setRefreshing] = useState(false);

  const handleRefresh = async () => {
    if (refreshing) return;
    setRefreshing(true);
    try { await onRefresh(); }
    catch { }
    finally { setRefreshing(false); }
  };

  return (
    <div className="bg-brand-card p-8 rounded-[2.5rem] border border-brand-border shadow-sm transition-colors duration-300">
      <div className="flex items-center gap-3 text-brand-text-muted mb-6">
        <Calendar size={20} />
        <span className="font-bold uppercase tracking-wider text-xs">Upcoming Bills</span>
        {!loading && (
          <button
            onClick={handleRefresh}
            disabled={refreshing}
            className="ml-auto p-1.5 rounded-lg hover:bg-brand-primary/5 text-brand-text-muted hover:text-brand-primary transition-all disabled:opacity-50"
          >
            <RefreshCw size={14} className={refreshing ? 'animate-spin' : ''} />
          </button>
        )}
      </div>
      <div className="space-y-6">
        {loading
          ? Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                  <Skeleton width={48} height={48} borderRadius="1rem" />
                  <div className="space-y-2">
                    <Skeleton width={100} height={14} borderRadius="0.5rem" />
                    <Skeleton width={70} height={12} borderRadius="0.5rem" />
                  </div>
                </div>
                <Skeleton width={60} height={16} borderRadius="0.5rem" />
              </div>
            ))
          : bills.map((bill) => (
              <div key={bill.id} className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 rounded-2xl bg-brand-bg flex items-center justify-center">
                    <Zap className={BILL_ICON_COLORS[bill.category.toLowerCase()] || 'text-brand-text-muted'} />
                  </div>
                  <div>
                    <div className="font-bold text-brand-primary">{bill.name}</div>
                    <div className="text-sm text-brand-text-muted">{bill.dueDate}</div>
                  </div>
                </div>
                <div className="font-bold text-brand-primary">${bill.amount.toFixed(2)}</div>
              </div>
            ))}
      </div>
      <button className="w-full mt-8 py-4 rounded-2xl border border-brand-border text-brand-primary font-bold hover:bg-brand-primary/5 transition-all">
        View All Bills
      </button>
    </div>
  );
}
