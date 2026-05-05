import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, ArrowLeft } from 'lucide-react';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: React.ReactNode;
  onBack?: () => void;
}

export default function Modal({ isOpen, onClose, title, children, onBack }: ModalProps) {
  return (
    <AnimatePresence>
      {isOpen && (
        <>
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4"
          />
          <motion.div
            initial={{ opacity: 0, scale: 0.95, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: 20 }}
            className="fixed inset-0 z-50 flex items-center justify-center p-4 pointer-events-none"
          >
            <div className="bg-brand-card w-full max-w-md rounded-[2.5rem] shadow-2xl border border-brand-border overflow-hidden pointer-events-auto">
              <div className="p-6 border-b border-brand-border flex justify-between items-center">
                <div className="flex items-center gap-3">
                  {onBack && (
                    <button
                      onClick={onBack}
                      className="p-2 hover:bg-brand-primary/5 rounded-full text-brand-text-muted transition-colors"
                    >
                      <ArrowLeft size={20} />
                    </button>
                  )}
                  <h3 className="text-xl font-bold text-brand-primary">{title}</h3>
                </div>
                <button
                  onClick={onClose}
                  className="p-2 hover:bg-brand-primary/5 rounded-full text-brand-text-muted transition-colors"
                >
                  <X size={20} />
                </button>
              </div>
              <div className="p-8">
                {children}
              </div>
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
}
