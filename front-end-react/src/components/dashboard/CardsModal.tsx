import { motion } from 'framer-motion';
import { CreditCard } from 'lucide-react';
import Modal from '../Modal';
import { Card } from '../../types/dashboard';
import Skeleton from '../Skeleton';

const CARD_COLORS: Record<string, string> = {
  VISA: 'bg-zinc-900',
  MC: 'bg-blue-600',
  MASTERCARD: 'bg-blue-600',
  AMEX: 'bg-emerald-600',
};

interface CardsModalProps {
  isOpen: boolean;
  onClose: () => void;
  cards: Card[];
  loading: boolean;
}

export default function CardsModal({ isOpen, onClose, cards, loading }: CardsModalProps) {
  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Active Cards">
      <div className="space-y-6">
        {loading
          ? Array.from({ length: 2 }).map((_, i) => (
              <Skeleton key={i} height={160} borderRadius="1.5rem" />
            ))
          : cards.map((card) => (
              <motion.div
                key={card.id}
                whileHover={{ scale: 1.02 }}
                className={`${CARD_COLORS[card.brand.toUpperCase()] || 'bg-zinc-800'} p-6 rounded-3xl text-white space-y-8 shadow-xl relative overflow-hidden group cursor-pointer`}
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
  );
}
