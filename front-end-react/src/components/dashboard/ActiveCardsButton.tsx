import { CreditCard, ArrowUpRight } from 'lucide-react';
import { Card } from '../../types/dashboard';
import Skeleton from '../Skeleton';

const CARD_COLORS: Record<string, string> = {
  VISA: 'bg-zinc-900',
  MC: 'bg-blue-600',
  MASTERCARD: 'bg-blue-600',
  AMEX: 'bg-emerald-600',
};

interface ActiveCardsButtonProps {
  cards: Card[];
  loading: boolean;
  onClick: () => void;
}

export default function ActiveCardsButton({ cards, loading, onClick }: ActiveCardsButtonProps) {
  return (
    <button
      onClick={onClick}
      disabled={loading}
      className="w-full text-left bg-emerald-50 dark:bg-emerald-900/10 p-8 rounded-[2.5rem] border border-emerald-100 dark:border-emerald-900/20 shadow-sm space-y-4 transition-all hover:scale-[1.02] active:scale-95 group disabled:hover:scale-100 disabled:active:scale-100"
    >
      <div className="flex items-center gap-3 text-emerald-600">
        <CreditCard size={20} />
        <span className="font-bold uppercase tracking-wider text-xs">Active Cards</span>
      </div>
      <div className="flex justify-between items-end">
        <div className="flex -space-x-4">
          {loading
            ? Array.from({ length: 3 }).map((_, i) => (
                <Skeleton
                  key={i}
                  width={48}
                  height={48}
                  borderRadius="999px"
                  className="border-2 border-white dark:border-zinc-900"
                />
              ))
            : cards.map((card) => (
                <div
                  key={card.id}
                  className={`w-12 h-12 rounded-full ${CARD_COLORS[card.brand.toUpperCase()] || 'bg-zinc-800'} border-2 border-white dark:border-zinc-900 flex items-center justify-center text-white text-[10px] font-bold shadow-lg`}
                >
                  {card.brand}
                </div>
              ))}
        </div>
        <div className="text-emerald-600 font-bold text-sm group-hover:translate-x-1 transition-transform flex items-center gap-1">
          Manage <ArrowUpRight size={14} />
        </div>
      </div>
    </button>
  );
}
