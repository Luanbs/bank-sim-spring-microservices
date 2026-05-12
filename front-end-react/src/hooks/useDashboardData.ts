import { useState, useEffect, useCallback } from 'react';
import * as accountService from '../services/accountService';
import { Account } from '../types/account';
import { SpendingDataPoint, Transaction, Bill, Card, SavingsGoal } from '../types/dashboard';
import { getErrorMessage } from '../services/errorhandler';

export function useDashboardData() {
  const [account, setAccount] = useState<Account | null>(null);
  const [accountLoading, setAccountLoading] = useState(true);
  const [accountError, setAccountError] = useState<string | null>(null);

  const [spendingData, setSpendingData] = useState<SpendingDataPoint[]>([]);
  const [spendingLoading, setSpendingLoading] = useState(true);

  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [transactionsLoading, setTransactionsLoading] = useState(true);

  const [bills, setBills] = useState<Bill[]>([]);
  const [billsLoading, setBillsLoading] = useState(true);

  const [cards, setCards] = useState<Card[]>([]);
  const [cardsLoading, setCardsLoading] = useState(true);

  const [savingsGoals, setSavingsGoals] = useState<SavingsGoal[]>([]);
  const [savingsLoading, setSavingsLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    async function init() {
      try {
        const data = await accountService.fetchDashboard();
        if (cancelled) return;

        setAccount({ id: data.id, userId: data.userId, ownerName: data.ownerName, balance: data.balance });
        if (data.ownerName) localStorage.setItem('ownerName', data.ownerName);
        if (data.spendingOverview) setSpendingData(data.spendingOverview);
        if (data.recentTransactions) setTransactions(data.recentTransactions);
        if (data.upcomingBills) setBills(data.upcomingBills);
        if (data.cards) setCards(data.cards);
        if (data.savingsGoals) setSavingsGoals(data.savingsGoals);
      } catch (error) {
        if (cancelled) return;
        setAccountError(getErrorMessage(error));
      } finally {
        if (cancelled) return;
        setAccountLoading(false);
        setSpendingLoading(false);
        setTransactionsLoading(false);
        setBillsLoading(false);
        setCardsLoading(false);
        setSavingsLoading(false);
      }
    }

    init();
    return () => { cancelled = true; };
  }, []);

  const refreshAccount = useCallback(async () => {
    const data = await accountService.fetchDashboard();
    setAccount({ id: data.id, userId: data.userId, ownerName: data.ownerName, balance: data.balance });
  }, []);

  const refreshSpending = useCallback(async () => {
    const res = await accountService.fetchSpendingOverview();
    setSpendingData(res.data);
  }, []);

  const refreshTransactions = useCallback(async () => {
    const res = await accountService.fetchRecentTransactions();
    setTransactions(res.transactions);
  }, []);

  const refreshBills = useCallback(async () => {
    const res = await accountService.fetchUpcomingBills();
    setBills(res.bills);
  }, []);

  const refreshSavings = useCallback(async () => {
    const res = await accountService.fetchSavingsGoals();
    setSavingsGoals(res.goals);
  }, []);

  return {
    account, accountLoading, accountError,
    spendingData, spendingLoading, refreshSpending,
    transactions, transactionsLoading, refreshTransactions,
    bills, billsLoading, refreshBills,
    cards, cardsLoading,
    savingsGoals, savingsLoading, refreshSavings,
    refreshAccount,
  };
}
