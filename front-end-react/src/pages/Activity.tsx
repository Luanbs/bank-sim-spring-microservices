import { useState } from 'react';
import { motion } from 'framer-motion';
import { ArrowUpRight, ArrowDownLeft, ShoppingBag, Coffee, Zap, Car, DollarSign, Download, AlertCircle } from 'lucide-react';
import Modal from '../components/Modal';

export default function Activity() {
  const [selectedTx, setSelectedTx] = useState<any>(null);
  const [actionMessage, setActionMessage] = useState<string | null>(null);

  const handleAction = (message: string) => {
    setActionMessage(message);
    setTimeout(() => {
      setActionMessage(null);
    }, 3000);
  };

  const transactions = [
    { id: 1, title: "Apple Store", category: "Technology", amount: -1299.00, date: "Today, 2:45 PM", icon: <Zap className="text-blue-500" />, type: 'expense', status: 'Completed', ref: 'TXN-8923-APL', method: 'Visa •••• 4242' },
    { id: 2, title: "Salary Deposit", category: "Income", amount: 4500.00, date: "Yesterday, 9:00 AM", icon: <DollarSign className="text-emerald-500" />, type: 'income', status: 'Completed', ref: 'TXN-1002-SAL', method: 'Direct Deposit' },
    { id: 3, title: "Starbucks", category: "Food & Drink", amount: -12.50, date: "Mar 28, 2026", icon: <Coffee className="text-orange-500" />, type: 'expense', status: 'Completed', ref: 'TXN-5541-STB', method: 'Mastercard •••• 8888' },
    { id: 4, title: "Uber Trip", category: "Transport", amount: -24.80, date: "Mar 27, 2026", icon: <Car className="text-zinc-500" />, type: 'expense', status: 'Completed', ref: 'TXN-9921-UBR', method: 'Amex •••• 1001' },
    { id: 5, title: "Amazon", category: "Shopping", amount: -89.99, date: "Mar 26, 2026", icon: <ShoppingBag className="text-purple-500" />, type: 'expense', status: 'Pending', ref: 'TXN-3321-AMZ', method: 'Visa •••• 4242' },
    { id: 6, title: "Freelance Project", category: "Income", amount: 850.00, date: "Mar 25, 2026", icon: <DollarSign className="text-emerald-500" />, type: 'income', status: 'Completed', ref: 'TXN-7742-FRL', method: 'Bank Transfer' },
  ];

  return (
    <div className="space-y-8">
      <header>
        <h2 className="text-3xl font-bold text-brand-primary transition-colors duration-300">Activity</h2>
        <p className="text-brand-text-muted mt-1 transition-colors duration-300">Track your recent transactions and financial movements.</p>
      </header>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <StatCard title="Total Spent" value="$1,426.29" change="-12%" color="text-red-600" />
        <StatCard title="Total Earned" value="$5,350.00" change="+8%" color="text-emerald-600" />
        <StatCard title="Net Balance" value="$3,923.71" change="+5%" color="text-blue-600" />
      </div>

      <div className="bg-brand-card rounded-3xl border border-brand-border overflow-hidden shadow-sm transition-colors duration-300">
        <div className="p-6 border-b border-brand-border flex justify-between items-center transition-colors duration-300">
          <h3 className="font-bold text-brand-primary text-lg transition-colors duration-300">Recent Transactions</h3>
          <button className="text-sm font-bold text-brand-text-muted hover:text-brand-primary transition-colors duration-300">View All</button>
        </div>
        <div className="divide-y divide-brand-border transition-colors duration-300">
          {transactions.map((tx, idx) => (
            <motion.div 
              key={tx.id}
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: idx * 0.05 }}
              onClick={() => setSelectedTx(tx)}
              className="p-6 flex items-center gap-4 hover:bg-brand-primary/5 transition-colors cursor-pointer"
            >
              <div className="w-12 h-12 rounded-2xl bg-brand-bg flex items-center justify-center transition-colors duration-300">
                {tx.icon}
              </div>
              <div className="flex-1">
                <div className="font-bold text-brand-primary transition-colors duration-300">{tx.title}</div>
                <div className="text-sm text-brand-text-muted transition-colors duration-300">{tx.category} • {tx.date}</div>
              </div>
              <div className={`font-bold text-lg ${tx.type === 'income' ? 'text-emerald-600' : 'text-brand-primary'} transition-colors duration-300`}>
                {tx.type === 'income' ? '+' : ''}{tx.amount.toLocaleString('en-US', { style: 'currency', currency: 'USD' })}
              </div>
              <div className="text-brand-border transition-colors duration-300">
                {tx.type === 'income' ? <ArrowDownLeft size={20} /> : <ArrowUpRight size={20} />}
              </div>
            </motion.div>
          ))}
        </div>
      </div>

      {/* Transaction Details Modal */}
      <Modal isOpen={!!selectedTx} onClose={() => setSelectedTx(null)} title="Transaction Details">
        {selectedTx && (
          <div className="space-y-8">
            <div className="flex flex-col items-center justify-center text-center space-y-4">
              <div className="w-20 h-20 rounded-full bg-brand-bg flex items-center justify-center scale-150 mb-4">
                {selectedTx.icon}
              </div>
              <div>
                <h3 className="text-2xl font-bold text-brand-primary">{selectedTx.title}</h3>
                <p className="text-brand-text-muted">{selectedTx.date}</p>
              </div>
              <div className={`text-4xl font-bold tracking-tight ${selectedTx.type === 'income' ? 'text-emerald-600' : 'text-brand-primary'}`}>
                {selectedTx.type === 'income' ? '+' : ''}{selectedTx.amount.toLocaleString('en-US', { style: 'currency', currency: 'USD' })}
              </div>
              <div className={`px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider ${selectedTx.status === 'Completed' ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400' : 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400'}`}>
                {selectedTx.status}
              </div>
            </div>

            <div className="bg-brand-bg rounded-2xl p-6 space-y-4 border border-brand-border">
              <div className="flex justify-between items-center">
                <span className="text-brand-text-muted font-medium">Category</span>
                <span className="font-bold text-brand-primary">{selectedTx.category}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-brand-text-muted font-medium">Payment Method</span>
                <span className="font-bold text-brand-primary">{selectedTx.method}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-brand-text-muted font-medium">Reference</span>
                <span className="font-mono text-sm text-brand-primary">{selectedTx.ref}</span>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <button 
                onClick={() => handleAction('Receipt downloaded successfully')}
                className="flex items-center justify-center gap-2 py-4 rounded-xl border border-brand-border text-brand-primary font-bold hover:bg-brand-primary/5 transition-all active:scale-95"
              >
                <Download size={18} />
                Receipt
              </button>
              <button 
                onClick={() => handleAction('Issue reported successfully')}
                className="flex items-center justify-center gap-2 py-4 rounded-xl border border-red-200 text-red-600 font-bold hover:bg-red-50 dark:border-red-900/30 dark:hover:bg-red-900/20 transition-all active:scale-95"
              >
                <AlertCircle size={18} />
                Report Issue
              </button>
            </div>
            {actionMessage && (
              <motion.div 
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                className="text-center text-sm font-bold text-emerald-600 bg-emerald-50 dark:bg-emerald-900/20 py-3 rounded-xl"
              >
                {actionMessage}
              </motion.div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
}

function StatCard({ title, value, change, color }: { title: string; value: string; change: string; color: string }) {
  return (
    <div className="bg-brand-card p-6 rounded-3xl border border-brand-border shadow-sm transition-colors duration-300">
      <div className="text-sm font-bold text-brand-text-muted uppercase tracking-wider mb-2 transition-colors duration-300">{title}</div>
      <div className="text-3xl font-bold text-brand-primary mb-2 transition-colors duration-300">{value}</div>
      <div className={`text-sm font-bold ${color}`}>
        {change} <span className="text-brand-text-muted font-medium transition-colors duration-300">vs last month</span>
      </div>
    </div>
  );
}
