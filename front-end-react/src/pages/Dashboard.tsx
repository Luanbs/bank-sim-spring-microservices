import { useState, useEffect } from 'react';
import { api } from '../services/api';
import { motion } from 'framer-motion';
import { Wallet, TrendingUp, ArrowUpRight, ArrowDownLeft, CreditCard, Calendar, Coffee, Zap, DollarSign } from 'lucide-react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import Modal from '../components/Modal';
import SendMoneyModal from '../components/SendMoneyModal';
import { Account } from '../types/account';
import { getErrorMessage } from "../services/errorhandler";

export default function Dashboard() {
  const [isSendOpen, setIsSendOpen] = useState(false);
  const [isRequestOpen, setIsRequestOpen] = useState(false);
  const [isCardsOpen, setIsCardsOpen] = useState(false);
  const [accountData, setAccountData] = useState<Account | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchAccountData() {
      setLoading(true);
      try {
        const response = await api.get("/account/me");
  
        setAccountData(response.data);
        if (response.data?.ownerName) {
          localStorage.setItem('ownerName', response.data.ownerName);
        }
      } catch (error) {
        console.error("🔥 [Dashboard] Erro na requisição:", error);
        const msg = getErrorMessage(error);
        setErrorMsg(msg);
      } finally {
        setLoading(false);
      }
    }
    
    fetchAccountData();
  }, []);





  const cards = [
    { id: 1, type: 'Visa', last4: '4242', expiry: '12/26', color: 'bg-zinc-900', brand: 'VISA' },
    { id: 2, type: 'Mastercard', last4: '8888', expiry: '09/25', color: 'bg-blue-600', brand: 'MC' },
    { id: 3, type: 'Amex', last4: '1001', expiry: '03/27', color: 'bg-emerald-600', brand: 'AMEX' },
  ];

  const chartData = [
    { name: 'Mon', spent: 120 },
    { name: 'Tue', spent: 300 },
    { name: 'Wed', spent: 150 },
    { name: 'Thu', spent: 450 },
    { name: 'Fri', spent: 200 },
    { name: 'Sat', spent: 600 },
    { name: 'Sun', spent: 100 },
  ];

  const upcomingBills = [
    { id: 1, name: 'Netflix', amount: 15.99, date: 'Tomorrow', icon: <Zap className="text-red-500" /> },
    { id: 2, name: 'Electricity', amount: 85.50, date: 'In 3 days', icon: <Zap className="text-yellow-500" /> },
    { id: 3, name: 'Internet', amount: 60.00, date: 'In 5 days', icon: <Zap className="text-blue-500" /> },
  ];

  const recentTransactions = [
    { id: 1, title: "Apple Store", category: "Technology", amount: -1299.00, date: "Today, 2:45 PM", icon: <Zap className="text-blue-500" />, type: 'expense' },
    { id: 2, title: "Salary Deposit", category: "Income", amount: 4500.00, date: "Yesterday, 9:00 AM", icon: <DollarSign className="text-emerald-500" />, type: 'income' },
    { id: 3, title: "Starbucks", category: "Food & Drink", amount: -12.50, date: "Mar 28, 2026", icon: <Coffee className="text-orange-500" />, type: 'expense' },
  ];



  if (loading) return <div>Loading...</div>;


  if (errorMsg) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="text-center">
          <h1 className="text-3xl font-bold text-brand-primary mb-4">Error</h1>
          <p className="text-brand-text-muted">{errorMsg}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <header>
        <h2 className="text-3xl font-bold text-brand-primary">Welcome back{accountData?.ownerName ? `, ${accountData.ownerName.split(' ')[0]}` : ''}</h2>
        <p className="text-brand-text-muted mt-1">Here's what's happening with your money today.</p>
      </header>


      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <motion.div 
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          className="lg:col-span-2 bg-brand-primary rounded-[2.5rem] p-10 text-brand-bg relative overflow-hidden shadow-2xl shadow-brand-primary/20 transition-colors duration-300"
        >
          <div className="relative z-10 space-y-8">
            <div className="flex justify-between items-start">
              <div className="space-y-1">
                <p className="text-brand-bg/60 font-medium">Total Balance</p>
                <h3 className="text-5xl font-bold tracking-tight">{accountData ? accountData.balance.toLocaleString('en-US', { style: 'currency', currency: 'USD' }) : '$0.00'}</h3>
              </div>
              <div className="w-14 h-14 bg-brand-bg/10 rounded-2xl flex items-center justify-center backdrop-blur-md border border-brand-bg/10">
                <Wallet size={28} />
              </div>
            </div>
            
            <div className="flex gap-4">
              <button 
                onClick={() => setIsSendOpen(true)}
                className="flex-1 bg-brand-bg text-brand-primary py-4 rounded-2xl font-bold hover:opacity-90 transition-all flex items-center justify-center gap-2 active:scale-95"
              >
                <ArrowUpRight size={20} />
                Send Money
              </button>
              <button 
                onClick={() => setIsRequestOpen(true)}
                className="flex-1 bg-brand-bg/10 text-brand-bg py-4 rounded-2xl font-bold hover:bg-brand-bg/20 transition-all backdrop-blur-md border border-brand-bg/10 flex items-center justify-center gap-2 active:scale-95"
              >
                <ArrowDownLeft size={20} />
                Request
              </button>
            </div>
          </div>
          
          <div className="absolute -right-20 -top-20 w-80 h-80 bg-brand-bg/5 rounded-full blur-3xl" />
          <div className="absolute -left-20 -bottom-20 w-60 h-60 bg-brand-secondary/10 rounded-full blur-3xl" />
        </motion.div>

        <div className="space-y-8">
          <div className="bg-brand-card p-8 rounded-[2.5rem] border border-brand-border shadow-sm space-y-4 transition-colors duration-300">
            <div className="flex items-center gap-3 text-brand-text-muted">
              <TrendingUp size={20} />
              <span className="font-bold uppercase tracking-wider text-xs">Savings Goal</span>
            </div>
            <div className="space-y-2">
              <div className="flex justify-between items-end">
                <h4 className="text-2xl font-bold text-brand-primary">New Car</h4>
                <span className="text-brand-text-muted font-medium">75%</span>
              </div>
              <div className="h-3 bg-brand-bg rounded-full overflow-hidden">
                <div className="h-full bg-brand-primary rounded-full w-3/4" />
              </div>
              <p className="text-sm text-brand-text-muted">$15,000 of $20,000 saved</p>
            </div>
          </div>

          <button 
            onClick={() => setIsCardsOpen(true)}
            className="w-full text-left bg-emerald-50 dark:bg-emerald-900/10 p-8 rounded-[2.5rem] border border-emerald-100 dark:border-emerald-900/20 shadow-sm space-y-4 transition-all hover:scale-[1.02] active:scale-95 group"
          >
            <div className="flex items-center gap-3 text-emerald-600">
              <CreditCard size={20} />
              <span className="font-bold uppercase tracking-wider text-xs">Active Cards</span>
            </div>
            <div className="flex justify-between items-end">
              <div className="flex -space-x-4">
                {cards.map(card => (
                  <div key={card.id} className={`w-12 h-12 rounded-full ${card.color} border-2 border-white dark:border-zinc-900 flex items-center justify-center text-white text-[10px] font-bold shadow-lg`}>
                    {card.brand}
                  </div>
                ))}
              </div>
              <div className="text-emerald-600 font-bold text-sm group-hover:translate-x-1 transition-transform flex items-center gap-1">
                Manage <ArrowUpRight size={14} />
              </div>
            </div>
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 bg-brand-card p-8 rounded-[2.5rem] border border-brand-border shadow-sm transition-colors duration-300">
          <div className="flex justify-between items-center mb-6">
            <h3 className="font-bold text-brand-primary text-xl">Spending Overview</h3>
            <select className="bg-brand-bg border border-brand-border text-brand-primary text-sm rounded-lg px-3 py-2 outline-none">
              <option>This Week</option>
              <option>Last Week</option>
              <option>This Month</option>
            </select>
          </div>
          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <defs>
                  <linearGradient id="colorSpent" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#141414" stopOpacity={0.3} className="dark:stopColor-white"/>
                    <stop offset="95%" stopColor="#141414" stopOpacity={0} className="dark:stopColor-white"/>
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
          </div>
        </div>

        <div className="bg-brand-card p-8 rounded-[2.5rem] border border-brand-border shadow-sm transition-colors duration-300">
          <div className="flex items-center gap-3 text-brand-text-muted mb-6">
            <Calendar size={20} />
            <span className="font-bold uppercase tracking-wider text-xs">Upcoming Bills</span>
          </div>
          <div className="space-y-6">
            {upcomingBills.map(bill => (
              <div key={bill.id} className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 rounded-2xl bg-brand-bg flex items-center justify-center">
                    {bill.icon}
                  </div>
                  <div>
                    <div className="font-bold text-brand-primary">{bill.name}</div>
                    <div className="text-sm text-brand-text-muted">{bill.date}</div>
                  </div>
                </div>
                <div className="font-bold text-brand-primary">
                  ${bill.amount.toFixed(2)}
                </div>
              </div>
            ))}
          </div>
          <button className="w-full mt-8 py-4 rounded-2xl border border-brand-border text-brand-primary font-bold hover:bg-brand-primary/5 transition-all">
            View All Bills
          </button>
        </div>
      </div>

      <div className="bg-brand-card rounded-[2.5rem] border border-brand-border overflow-hidden shadow-sm transition-colors duration-300">
        <div className="p-8 border-b border-brand-border flex justify-between items-center">
          <h3 className="font-bold text-brand-primary text-xl">Recent Transactions</h3>
          <button className="text-sm font-bold text-brand-text-muted hover:text-brand-primary transition-colors">View All</button>
        </div>
        <div className="divide-y divide-brand-border">
          {recentTransactions.map((tx, idx) => (
            <motion.div 
              key={tx.id}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: idx * 0.1 }}
              className="p-6 px-8 flex items-center gap-4 hover:bg-brand-primary/5 transition-colors cursor-pointer"
            >
              <div className="w-12 h-12 rounded-2xl bg-brand-bg flex items-center justify-center">
                {tx.icon}
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

      <SendMoneyModal
        isOpen={isSendOpen}
        onClose={() => setIsSendOpen(false)}
        currentBalance={accountData?.balance ?? 0}
      />

      <Modal
        isOpen={isRequestOpen}
        onClose={() => setIsRequestOpen(false)}
        title="Request Money"
      >
        <div className="py-12 flex flex-col items-center text-center space-y-4">
          <div className="w-20 h-20 bg-brand-primary/10 rounded-full flex items-center justify-center">
            <ArrowDownLeft size={36} className="text-brand-primary" />
          </div>
          <div className="space-y-2">
            <h4 className="text-2xl font-bold text-brand-primary">Coming Soon</h4>
            <p className="text-brand-text-muted">Request money feature is under development.</p>
          </div>
        </div>
      </Modal>

      <Modal isOpen={isCardsOpen} onClose={() => setIsCardsOpen(false)} title="Active Cards">
        <div className="space-y-6">
          {cards.map(card => (
            <motion.div 
              key={card.id}
              whileHover={{ scale: 1.02 }}
              className={`${card.color} p-6 rounded-3xl text-white space-y-8 shadow-xl relative overflow-hidden group cursor-pointer`}
            >
              <div className="flex justify-between items-start relative z-10">
                <div className="space-y-1">
                  <p className="text-white/60 text-xs font-bold uppercase tracking-widest">{card.type}</p>
                  <h5 className="text-xl font-mono tracking-widest">•••• •••• •••• {card.last4}</h5>
                </div>
                <div className="text-2xl font-black italic">{card.brand}</div>
              </div>
              
              <div className="flex justify-between items-end relative z-10">
                <div className="space-y-1">
                  <p className="text-white/60 text-[10px] font-bold uppercase tracking-widest">Expires</p>
                  <p className="font-bold">{card.expiry}</p>
                </div>
                <div className="w-10 h-10 bg-white/20 rounded-lg backdrop-blur-md flex items-center justify-center">
                  <CreditCard size={20} />
                </div>
              </div>

              <div className="absolute -right-10 -bottom-10 w-32 h-32 bg-white/10 rounded-full blur-2xl group-hover:scale-150 transition-transform duration-500" />
            </motion.div>
          ))}
          
          <button className="w-full border-2 border-dashed border-brand-border py-4 rounded-3xl text-brand-text-muted font-bold hover:border-brand-primary hover:text-brand-primary transition-all flex items-center justify-center gap-2">
            + Add New Card
          </button>
        </div>
      </Modal>
    </div>
  );
}
