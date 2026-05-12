import { useState, useEffect, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Mail, Phone, AlertCircle, Share2, FileText } from 'lucide-react';
import Modal from './Modal';
import { api } from '../services/api';
import { getErrorMessage } from '../services/errorhandler';
import { RecentContact, RecipientAccount } from '../types/account';

type Step = 'form' | 'confirm' | 'receipt';

interface SendMoneyModalProps {
  isOpen: boolean;
  onClose: () => void;
  currentBalance: number;
}

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const slideVariants = {
  enterFromRight: { x: 60, opacity: 0 },
  enterFromLeft: { x: -60, opacity: 0 },
  center: { x: 0, opacity: 1 },
  exitToLeft: { x: -60, opacity: 0 },
  exitToRight: { x: 60, opacity: 0 },
};

export default function SendMoneyModal({ isOpen, onClose, currentBalance }: SendMoneyModalProps) {
  const [step, setStep] = useState<Step>('form');
  const [direction, setDirection] = useState<'forward' | 'backward'>('forward');


  const [recentContacts, setRecentContacts] = useState<RecentContact[]>([]);
  const [contactsLoading, setContactsLoading] = useState(false);
  const [selectedContactEmail, setSelectedContactEmail] = useState<string | null>(null);
  const [email, setEmail] = useState('');
  const [amount, setAmount] = useState('');
  const [formError, setFormError] = useState<string | null>(null);
  const [sendLoading, setSendLoading] = useState(false);


  const [recipient, setRecipient] = useState<RecipientAccount | null>(null);
  const [confirmError, setConfirmError] = useState<string | null>(null);
  const [confirmLoading, setConfirmLoading] = useState(false);


  useEffect(() => {
    if (!isOpen) return;

    async function fetchContacts() {
      setContactsLoading(true);
      try {
        const response = await api.get('/account/contacts/recent');
        console.log('[SendMoney] Contacts response:', response.data);
        const raw = response.data || [];
        const normalized = raw.map((c: Record<string, unknown>) => ({
          contactName: (c.contactName || c.name || c.fullName || c.email || '?') as string,
          email: (c.email || '') as string,
        }));
        setRecentContacts(normalized);
      } catch {
        setRecentContacts([]);
      } finally {
        setContactsLoading(false);
      }
    }

    fetchContacts();
  }, [isOpen]);


  const resetState = useCallback(() => {
    setStep('form');
    setDirection('forward');
    setRecentContacts([]);
    setSelectedContactEmail(null);
    setEmail('');
    setAmount('');
    setFormError(null);
    setSendLoading(false);
    setRecipient(null);
    setConfirmError(null);
    setConfirmLoading(false);
  }, []);

  const handleClose = () => {
    onClose();

    setTimeout(resetState, 300);
  };

  const handleContactSelect = (contact: RecentContact) => {
    setSelectedContactEmail(contact.email);
    setEmail(contact.email);
    setFormError(null);
  };

  const handleEmailChange = (value: string) => {
    setEmail(value);
    setFormError(null);

    if (selectedContactEmail && value !== selectedContactEmail) {
      setSelectedContactEmail(null);
    }
  };

  const isEmailValid = EMAIL_REGEX.test(email);
  const isAmountValid = parseFloat(amount) > 0;

  const handleSendNow = async () => {
    setFormError(null);

    if (!isEmailValid) {
      setFormError('Please enter a valid email address.');
      return;
    }

    const parsedAmount = parseFloat(amount);
    if (!isAmountValid || isNaN(parsedAmount)) {
      setFormError('Please enter a valid amount.');
      return;
    }


    if (parsedAmount > currentBalance) {
      setFormError(
        `Insufficient balance. Your current balance is ${currentBalance.toLocaleString('en-US', { style: 'currency', currency: 'USD' })}.`
      );
      return;
    }


    setSendLoading(true);
    try {
      const response = await api.get(`/account/${encodeURIComponent(email)}`);
      console.log('[SendMoney] Recipient response:', response.data);
      const data = response.data;
      setRecipient({
        ownerName: data.ownerName || data.name || data.fullName || email,
        email: data.email || email,
      });
      setDirection('forward');
      setStep('confirm');
    } catch (error: unknown) {
      const msg = getErrorMessage(error);
      setFormError(msg);
    } finally {
      setSendLoading(false);
    }
  };

  const handleConfirmAndSend = async () => {
    setConfirmError(null);
    setConfirmLoading(true);

    try {
      await api.post('/account/transfer', {
        recipientEmail: email,
        amount: parseFloat(amount),
      });
      setDirection('forward');
      setStep('receipt');
    } catch (error: unknown) {
      const msg = getErrorMessage(error);
      setConfirmError(msg);
    } finally {
      setConfirmLoading(false);
    }
  };

  const handleBack = () => {
    setDirection('backward');
    setConfirmError(null);
    setStep('form');
  };


  const modalTitle =
    step === 'form' ? 'Send Money' :
    step === 'confirm' ? 'Confirm Transfer' :
    'Receipt';

  const modalOnBack = step === 'confirm' ? handleBack : undefined;




  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      title={modalTitle}
      onBack={modalOnBack}
    >
      <AnimatePresence mode="wait" initial={false}>
        <motion.div
          key={step}
          variants={slideVariants}
          initial={direction === 'forward' ? 'enterFromRight' : 'enterFromLeft'}
          animate="center"
          exit={direction === 'forward' ? 'exitToLeft' : 'exitToRight'}
          transition={{ duration: 0.25, ease: 'easeInOut' }}
          className="space-y-6"
        >
          {step === 'form' && (
            <>

              {contactsLoading ? (
                <div className="space-y-3">
                  <label className="text-sm font-bold text-brand-text-muted uppercase tracking-wider">
                    Recent Contacts
                  </label>
                  <div className="flex gap-3 overflow-x-auto pb-2">
                    {Array.from({ length: 4 }).map((_, i) => (
                      <div key={i} className="flex flex-col items-center gap-2 min-w-[72px]">
                        <div className="w-14 h-14 rounded-2xl skeleton-shimmer" />
                        <div className="w-10 h-3 rounded skeleton-shimmer" />
                      </div>
                    ))}
                  </div>
                </div>
              ) : recentContacts.length > 0 ? (
                <div className="space-y-3">
                  <label className="text-sm font-bold text-brand-text-muted uppercase tracking-wider">
                    Recent Contacts
                  </label>
                  <div className="flex gap-3 overflow-x-auto pb-2">
                    {recentContacts.map((contact) => {
                      const isSelected = selectedContactEmail === contact.email;
                      const initLetter = (contact.contactName || '?').charAt(0).toUpperCase();
                      const displayName = (contact.contactName || contact.email || 'Unknown').split(' ')[0];
                      return (
                        <button
                          key={contact.email}
                          onClick={() => handleContactSelect(contact)}
                          className="flex flex-col items-center gap-2 min-w-[72px] group transition-all duration-200"
                        >
                          <div
                            className={`w-14 h-14 rounded-2xl flex items-center justify-center text-xl font-bold transition-all duration-200
                              ${isSelected
                                ? 'bg-brand-primary text-brand-bg ring-2 ring-brand-primary ring-offset-2 ring-offset-brand-card scale-105'
                                : 'bg-brand-primary/10 text-brand-primary group-hover:bg-brand-primary/20 group-hover:scale-105'
                              }`}
                          >
                            {initLetter}
                          </div>
                          <span
                            className={`text-xs font-semibold text-center leading-tight transition-colors duration-200
                              ${isSelected ? 'text-brand-primary' : 'text-brand-text-muted group-hover:text-brand-primary'}`}
                          >
                            {displayName}
                          </span>
                        </button>
                      );
                    })}
                  </div>
                </div>
              ) : null}


              <div className="space-y-3">
                <label className="text-sm font-bold text-brand-text-muted uppercase tracking-wider">
                  Select contact identification
                </label>
                <div className="flex gap-2">
                  <button className="flex-1 flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl bg-brand-primary text-brand-bg font-semibold text-sm transition-all">
                    <Mail size={16} />
                    Email
                  </button>
                  <button
                    disabled
                    className="flex-1 flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl bg-brand-bg border border-brand-border text-brand-text-muted font-semibold text-sm opacity-40 cursor-not-allowed"
                  >
                    <Phone size={16} />
                    Phone Number
                  </button>
                </div>
              </div>


              <div className="space-y-2">
                <label className="text-sm font-bold text-brand-text-muted uppercase tracking-wider">
                  Recipient Email
                </label>
                <input
                  type="email"
                  placeholder="name@example.com"
                  value={email}
                  onChange={(e) => handleEmailChange(e.target.value)}
                  className={`w-full bg-brand-bg border rounded-2xl px-5 py-4 text-brand-primary font-medium focus:ring-2 focus:ring-brand-primary focus:outline-none transition-all
                    ${email && !isEmailValid ? 'border-red-400' : 'border-brand-border'}`}
                />
                {email && !isEmailValid && (
                  <p className="text-xs text-red-500 font-medium flex items-center gap-1 mt-1">
                    <AlertCircle size={12} />
                    Please enter a valid email (e.g. name@example.com)
                  </p>
                )}
              </div>


              <div className="space-y-2">
                <label className="text-sm font-bold text-brand-text-muted uppercase tracking-wider">
                  Amount
                </label>
                <div className="relative">
                  <span className="absolute left-5 top-1/2 -translate-y-1/2 text-3xl font-bold text-brand-primary">$</span>
                  <input
                    type="number"
                    placeholder="0.00"
                    value={amount}
                    onChange={(e) => { setAmount(e.target.value); setFormError(null); }}
                    min="0.01"
                    step="0.01"
                    className="w-full bg-brand-bg border border-brand-border rounded-2xl py-6 pl-12 pr-5 text-3xl font-bold text-brand-primary focus:ring-2 focus:ring-brand-primary focus:outline-none transition-all"
                  />
                </div>
              </div>


              {formError && (
                <motion.div
                  initial={{ opacity: 0, y: -8 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="flex items-start gap-3 p-4 rounded-2xl bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800/30"
                >
                  <AlertCircle size={18} className="text-red-500 mt-0.5 flex-shrink-0" />
                  <p className="text-sm text-red-600 dark:text-red-400 font-medium">{formError}</p>
                </motion.div>
              )}


              <button
                onClick={handleSendNow}
                disabled={!isEmailValid || !isAmountValid || sendLoading}
                className="w-full bg-brand-primary text-brand-bg py-5 rounded-3xl font-bold text-lg hover:opacity-90 transition-all disabled:opacity-50 disabled:cursor-not-allowed active:scale-95 flex items-center justify-center gap-2"
              >
                {sendLoading ? (
                  <div className="w-6 h-6 border-2 border-brand-bg/30 border-t-brand-bg rounded-full animate-spin" />
                ) : (
                  'Send Now'
                )}
              </button>
            </>
          )}

          {step === 'confirm' && recipient && (
            <>

              <div className="bg-brand-bg rounded-3xl p-6 space-y-4 border border-brand-border">
                <p className="text-xs font-bold text-brand-text-muted uppercase tracking-wider">Sending to</p>
                <div className="flex items-center gap-4">
                  <div className="w-14 h-14 rounded-2xl bg-brand-primary/10 flex items-center justify-center text-xl font-bold text-brand-primary">
                    {(recipient.ownerName || '?').charAt(0).toUpperCase()}
                  </div>
                  <div className="space-y-0.5">
                    <h4 className="text-lg font-bold text-brand-primary">{recipient.ownerName || 'Unknown'}</h4>
                    <p className="text-sm text-brand-text-muted">{recipient.email || email}</p>
                  </div>
                </div>
              </div>


              <div className="bg-brand-bg rounded-3xl p-6 border border-brand-border">
                <p className="text-xs font-bold text-brand-text-muted uppercase tracking-wider mb-2">Amount</p>
                <p className="text-4xl font-bold text-brand-primary">
                  {parseFloat(amount).toLocaleString('en-US', { style: 'currency', currency: 'USD' })}
                </p>
              </div>

              {confirmError && (
                <motion.div
                  initial={{ opacity: 0, y: -8 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="flex items-start gap-3 p-4 rounded-2xl bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800/30"
                >
                  <AlertCircle size={18} className="text-red-500 mt-0.5 flex-shrink-0" />
                  <p className="text-sm text-red-600 dark:text-red-400 font-medium">{confirmError}</p>
                </motion.div>
              )}


              <button
                onClick={handleConfirmAndSend}
                disabled={confirmLoading}
                className="w-full bg-brand-primary text-brand-bg py-5 rounded-3xl font-bold text-lg hover:opacity-90 transition-all disabled:opacity-50 disabled:cursor-not-allowed active:scale-95 flex items-center justify-center gap-2"
              >
                {confirmLoading ? (
                  <div className="w-6 h-6 border-2 border-brand-bg/30 border-t-brand-bg rounded-full animate-spin" />
                ) : (
                  'Confirm and Send'
                )}
              </button>
            </>
          )}

          {step === 'receipt' && (
            <>

              <div className="flex flex-col items-center text-center space-y-4 py-6">
                <div className="w-20 h-20 bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600 rounded-full flex items-center justify-center">
                  <FileText size={36} />
                </div>
                <div className="space-y-2">
                  <h4 className="text-2xl font-bold text-brand-primary">Transfer Sent!</h4>
                  <p className="text-brand-text-muted leading-relaxed max-w-xs mx-auto">
                    This feature is currently under development. A detailed transaction receipt will be available in a future update.
                  </p>
                </div>
              </div>


              <button
                disabled
                className="w-full bg-brand-bg border border-brand-border text-brand-text-muted py-5 rounded-3xl font-bold text-lg opacity-50 cursor-not-allowed flex items-center justify-center gap-3"
                title="Coming soon"
              >
                <Share2 size={20} />
                Share Receipt
                <span className="text-xs font-medium bg-brand-primary/10 text-brand-primary px-2 py-0.5 rounded-full">Soon</span>
              </button>
            </>
          )}
        </motion.div>
      </AnimatePresence>
    </Modal>
  );
}
