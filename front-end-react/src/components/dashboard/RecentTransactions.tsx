import { useState } from 'react';
import { motion } from 'framer-motion';
import { Zap, DollarSign, Coffee, ShoppingBag, RefreshCw } from 'lucide-react';
import { Transaction } from '../../types/dashboard';
import Skeleton from '../Skeleton';

function getTransactionIcon(category: string) {
  switch (category.toLowerCase()) {
    case 'technology':   return <Zap className="text-blue-500" />;
    case 'income':       return <DollarSign className="text-emerald-500" />;
    case 'food & drink': return <Coffee className="text-orange-500" />;
    case 'shopping':     return <ShoppingBag className="text-purple-500" />;
    default:             return <DollarSign className="text-brand-text-muted" />;
  }
}

interface RecentTransactionsProps {
  transactions: Transaction[];
  loading: boolean;
  onRefresh: () => Promise<void>;
}

export default function RecentTransactions({ transactions, loading, onRefresh }: RecentTransactionsProps) {
  const [refreshing, setRefreshing] = useState(false);

  const handleRefresh = async () => {
    if (refreshing) return;
    setRefreshing(true);
    try { await onRefresh(); }
    catch { }
    finally { setRefreshing(false); }
  };

  return (
    <div className="bg-brand-card rounded-[2.5rem] border border-brand-border overflow-hidden shadow-sm transition-colors duration-300">
      <div className="p-8 border-b border-brand-border flex justify-between items-center">
        <div className="flex items-center gap-2">
          <h3 className="font-bold text-brand-primary text-xl">Recent Transactions</h3>
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
        <button className="text-sm font-bold text-brand-text-muted hover:text-brand-primary transition-colors">
          View All
        </button>
      </div>
      <div className="divide-y divide-brand-border">
        {loading
          ? Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="p-6 px-8 flex items-center gap-4">
                <Skeleton width={48} height={48} borderRadius="1rem" />
                <div className="flex-1 space-y-2">
                  <Skeleton width={140} height={16} borderRadius="0.5rem" />
                  <Skeleton width={200} height={12} borderRadius="0.5rem" />
                </div>
                <Skeleton width={80} height={20} borderRadius="0.5rem" />
              </div>
            ))
          : transactions.map((tx, idx) => (
              <motion.div
                key={tx.id}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: idx * 0.1 }}
                className="p-6 px-8 flex items-center gap-4 hover:bg-brand-primary/5 transition-colors cursor-pointer"
              >
                <div className="w-12 h-12 rounded-2xl bg-brand-bg flex items-center justify-center">
                  {getTransactionIcon(tx.category)}
                </div>
                <div className="flex-1">
                  <div className="font-bold text-brand-primary">{tx.title}</div>
                  <div className="text-sm text-brand-text-muted">{tx.category} • {tx.date}</div>
                </div>
                <div className={`font-bold text-lg ${tx.type === 'income' ? 'text-emerald-600' : 'text-brand-primary'}`}>
                  {tx.type === 'income' ? '+' : ''}{tx.amount.toLocaleString('en-US', { style: 'currency', currency: 'USD' })}
                </div>
              </motion.div>
            ))}
      </div>
    </div>
  );
}
