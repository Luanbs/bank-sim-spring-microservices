export interface Account {
  id: string;
  userId: string;
  ownerName: string;
  balance: number;
}

export type Profile = {
  email: string;
  location: string;
}

export interface RecentContact {
  contactName: string;
  email: string;
}

export interface RecipientAccount {
  ownerName: string;
  email: string;
}