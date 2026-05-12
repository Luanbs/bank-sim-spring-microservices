import { useState } from 'react';
import { ArrowDownLeft } from 'lucide-react';
import { useDashboardData } from '../hooks/useDashboardData';
import Modal from '../components/Modal';
import SendMoneyModal from '../components/SendMoneyModal';
import BalanceCard from '../components/dashboard/BalanceCard';
import SpendingOverview from '../components/dashboard/SpendingOverview';
import RecentTransactions from '../components/dashboard/RecentTransactions';
import UpcomingBills from '../components/dashboard/UpcomingBills';
import SavingsGoalCard from '../components/dashboard/SavingsGoalCard';
import ActiveCardsButton from '../components/dashboard/ActiveCardsButton';
import CardsModal from '../components/dashboard/CardsModal';
import Skeleton from '../components/Skeleton';

export default function Dashboard() {
  const [isSendOpen, setIsSendOpen] = useState(false);
  const [isRequestOpen, setIsRequestOpen] = useState(false);
  const [isCardsOpen, setIsCardsOpen] = useState(false);

  const {
    account, accountLoading, accountError,
    spendingData, spendingLoading, refreshSpending,
    transactions, transactionsLoading, refreshTransactions,
    bills, billsLoading, refreshBills,
    cards, cardsLoading,
    savingsGoals, savingsLoading, refreshSavings,
    refreshAccount,
  } = useDashboardData();

  if (accountError) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="text-center">
          <h1 className="text-3xl font-bold text-brand-primary mb-4">Error</h1>
          <p className="text-brand-text-muted">{accountError}</p>
        </div>
      </div>
    );
  }

  const handleSendClose = () => {
    setIsSendOpen(false);
    setTimeout(() => refreshAccount(), 350);
  };

  return (
    <div className="space-y-8">
      <header>
        {accountLoading ? (
          <div className="space-y-2">
            <Skeleton width={280} height={32} borderRadius="0.75rem" />
            <Skeleton width={340} height={18} borderRadius="0.5rem" />
          </div>
        ) : (
          <>
            <h2 className="text-3xl font-bold text-brand-primary">
              Welcome back{account?.ownerName ? `, ${account.ownerName.split(' ')[0]}` : ''}
            </h2>
            <p className="text-brand-text-muted mt-1">Here's what's happening with your money today.</p>
          </>
        )}
      </header>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <BalanceCard
          account={account}
          loading={accountLoading}
          onSendMoney={() => setIsSendOpen(true)}
          onRequestMoney={() => setIsRequestOpen(true)}
          onRefresh={refreshAccount}
        />
        <div className="space-y-8">
          <SavingsGoalCard goals={savingsGoals} loading={savingsLoading} onRefresh={refreshSavings} />
          <ActiveCardsButton cards={cards} loading={cardsLoading} onClick={() => setIsCardsOpen(true)} />
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <SpendingOverview data={spendingData} loading={spendingLoading} onRefresh={refreshSpending} />
        <UpcomingBills bills={bills} loading={billsLoading} onRefresh={refreshBills} />
      </div>

      <RecentTransactions transactions={transactions} loading={transactionsLoading} onRefresh={refreshTransactions} />

      <SendMoneyModal
        isOpen={isSendOpen}
        onClose={handleSendClose}
        currentBalance={account?.balance ?? 0}
      />

      <Modal isOpen={isRequestOpen} onClose={() => setIsRequestOpen(false)} title="Request Money">
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

      <CardsModal isOpen={isCardsOpen} onClose={() => setIsCardsOpen(false)} cards={cards} loading={cardsLoading} />
    </div>
  );
}
