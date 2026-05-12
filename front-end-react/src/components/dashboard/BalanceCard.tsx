import { useState } from 'react';
import { motion } from 'framer-motion';
import { Wallet, ArrowUpRight, ArrowDownLeft, RefreshCw } from 'lucide-react';
import { Account } from '../../types/account';
import Skeleton from '../Skeleton';

interface BalanceCardProps {
  account: Account | null;
  loading: boolean;
  onSendMoney: () => void;
  onRequestMoney: () => void;
  onRefresh: () => Promise<void>;
}

export default function BalanceCard({ account, loading, onSendMoney, onRequestMoney, onRefresh }: BalanceCardProps) {
  const [refreshing, setRefreshing] = useState(false);

  const handleRefresh = async () => {
    if (refreshing) return;
    setRefreshing(true);
    try { await onRefresh(); }
    catch { }
    finally { setRefreshing(false); }
  };

  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.95 }}
      animate={{ opacity: 1, scale: 1 }}
      className="lg:col-span-2 bg-brand-primary rounded-[2.5rem] p-10 text-brand-bg relative overflow-hidden shadow-2xl shadow-brand-primary/20 transition-colors duration-300"
    >
      <div className="relative z-10 space-y-8">
        <div className="flex justify-between items-start">
          <div className="space-y-2">
            <div className="flex items-center gap-2">
              <p className="text-brand-bg/60 font-medium">Total Balance</p>
              {!loading && (
                <button
                  onClick={handleRefresh}
                  disabled={refreshing}
                  className="p-1 rounded-lg hover:bg-brand-bg/10 text-brand-bg/40 hover:text-brand-bg transition-all disabled:opacity-50"
                >
                  <RefreshCw size={14} className={refreshing ? 'animate-spin' : ''} />
                </button>
              )}
            </div>
            {loading ? (
              <Skeleton width={220} height={48} borderRadius="0.75rem" className="skeleton-shimmer-inverted" />
            ) : (
              <h3 className="text-5xl font-bold tracking-tight">
                {account ? account.balance.toLocaleString('en-US', { style: 'currency', currency: 'USD' }) : '$0.00'}
              </h3>
            )}
          </div>
          <div className="w-14 h-14 bg-brand-bg/10 rounded-2xl flex items-center justify-center backdrop-blur-md border border-brand-bg/10">
            <Wallet size={28} />
          </div>
        </div>

        <div className="flex gap-4">
          {loading ? (
            <>
              <Skeleton height={56} borderRadius="1rem" className="skeleton-shimmer-inverted" style={{ flex: 1 }} />
              <Skeleton height={56} borderRadius="1rem" className="skeleton-shimmer-inverted" style={{ flex: 1 }} />
            </>
          ) : (
            <>
              <button
                onClick={onSendMoney}
                className="flex-1 bg-brand-bg text-brand-primary py-4 rounded-2xl font-bold hover:opacity-90 transition-all flex items-center justify-center gap-2 active:scale-95"
              >
                <ArrowUpRight size={20} />
                Send Money
              </button>
              <button
                onClick={onRequestMoney}
                className="flex-1 bg-brand-bg/10 text-brand-bg py-4 rounded-2xl font-bold hover:bg-brand-bg/20 transition-all backdrop-blur-md border border-brand-bg/10 flex items-center justify-center gap-2 active:scale-95"
              >
                <ArrowDownLeft size={20} />
                Request
              </button>
            </>
          )}
        </div>
      </div>

      <div className="absolute -right-20 -top-20 w-80 h-80 bg-brand-bg/5 rounded-full blur-3xl" />
      <div className="absolute -left-20 -bottom-20 w-60 h-60 bg-brand-secondary/10 rounded-full blur-3xl" />
    </motion.div>
  );
}
