import { useState } from 'react';
import { TrendingUp, RefreshCw } from 'lucide-react';
import { SavingsGoal as SavingsGoalType } from '../../types/dashboard';
import Skeleton from '../Skeleton';

interface SavingsGoalProps {
  goals: SavingsGoalType[];
  loading: boolean;
  onRefresh: () => Promise<void>;
}

export default function SavingsGoalCard({ goals, loading, onRefresh }: SavingsGoalProps) {
  const [refreshing, setRefreshing] = useState(false);
  const goal = goals[0];
  const percentage = goal ? Math.round((goal.currentAmount / goal.targetAmount) * 100) : 0;

  const handleRefresh = async () => {
    if (refreshing) return;
    setRefreshing(true);
    try { await onRefresh(); }
    catch { }
    finally { setRefreshing(false); }
  };

  return (
    <div className="bg-brand-card p-8 rounded-[2.5rem] border border-brand-border shadow-sm space-y-4 transition-colors duration-300">
      <div className="flex items-center gap-3 text-brand-text-muted">
        <TrendingUp size={20} />
        <span className="font-bold uppercase tracking-wider text-xs">Savings Goal</span>
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
      <div className="space-y-2">
        {loading ? (
          <>
            <div className="flex justify-between items-end">
              <Skeleton width={120} height={24} borderRadius="0.5rem" />
              <Skeleton width={40} height={16} borderRadius="0.5rem" />
            </div>
            <Skeleton height={12} borderRadius="999px" />
            <Skeleton width={180} height={14} borderRadius="0.5rem" />
          </>
        ) : goal ? (
          <>
            <div className="flex justify-between items-end">
              <h4 className="text-2xl font-bold text-brand-primary">{goal.name}</h4>
              <span className="text-brand-text-muted font-medium">{percentage}%</span>
            </div>
            <div className="h-3 bg-brand-bg rounded-full overflow-hidden">
              <div
                className="h-full bg-brand-primary rounded-full transition-all duration-700"
                style={{ width: `${percentage}%` }}
              />
            </div>
            <p className="text-sm text-brand-text-muted">
              ${goal.currentAmount.toLocaleString()} of ${goal.targetAmount.toLocaleString()} saved
            </p>
          </>
        ) : (
          <p className="text-sm text-brand-text-muted">No savings goals yet.</p>
        )}
      </div>
    </div>
  );
}
