import { Account } from './account';

export interface SpendingDataPoint {
  name: string;
  spent: number;
  date: string;
}

export interface Transaction {
  id: number;
  title: string;
  category: string;
  amount: number;
  date: string;
  type: 'income' | 'expense';
}

export interface Bill {
  id: number;
  name: string;
  category: string;
  amount: number;
  dueDate: string;
}

export interface Card {
  id: number;
  type: string;
  last4: string;
  expiry: string;
  brand: string;
}

export interface SavingsGoal {
  id: number;
  name: string;
  currentAmount: number;
  targetAmount: number;
}

export interface DashboardResponse extends Account {
  spendingOverview?: SpendingDataPoint[];
  recentTransactions?: Transaction[];
  upcomingBills?: Bill[];
  cards?: Card[];
  savingsGoals?: SavingsGoal[];
}

export interface SpendingOverviewResponse {
  data: SpendingDataPoint[];
}

export interface RecentTransactionsResponse {
  transactions: Transaction[];
}

export interface UpcomingBillsResponse {
  bills: Bill[];
}

export interface CardsResponse {
  cards: Card[];
}

export interface SavingsGoalsResponse {
  goals: SavingsGoal[];
}
