import { api } from './api';
import {
  DashboardResponse,
  SpendingOverviewResponse,
  RecentTransactionsResponse,
  UpcomingBillsResponse,
  CardsResponse,
  SavingsGoalsResponse,
} from '../types/dashboard';

export async function fetchDashboard(): Promise<DashboardResponse> {
  const response = await api.get('/account/me');
  return response.data;
}

export async function fetchSpendingOverview(): Promise<SpendingOverviewResponse> {
  const response = await api.get('/account/spending-overview');
  return response.data;
}

export async function fetchRecentTransactions(limit: number = 5): Promise<RecentTransactionsResponse> {
  const response = await api.get('/account/transactions/recent', { params: { limit } });
  return response.data;
}

export async function fetchUpcomingBills(): Promise<UpcomingBillsResponse> {
  const response = await api.get('/account/bills/upcoming');
  return response.data;
}

export async function fetchCards(): Promise<CardsResponse> {
  const response = await api.get('/account/cards');
  return response.data;
}

export async function fetchSavingsGoals(): Promise<SavingsGoalsResponse> {
  const response = await api.get('/account/savings-goals');
  return response.data;
}
