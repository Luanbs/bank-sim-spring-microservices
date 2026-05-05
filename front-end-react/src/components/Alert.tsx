import { AlertCircle, CheckCircle2, X } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";

interface AlertProps {
  message: string;
  type: "error" | "success";
  onClose?: () => void;
}

export default function Alert({ message, type, onClose }: AlertProps) {
  if (!message) return null;

  return (
    <AnimatePresence>
      <motion.div
        initial={{ opacity: 0, y: -10 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.95 }}
        className={`flex items-center gap-3 p-4 rounded-xl border mb-6 ${
          type === "error"
            ? "bg-red-50 border-red-100 text-red-700"
            : "bg-emerald-50 border-emerald-100 text-emerald-700"
        }`}
      >
        {type === "error" ? <AlertCircle size={20} /> : <CheckCircle2 size={20} />}
        <p className="text-sm font-medium flex-1">{message}</p>
        {onClose && (
          <button onClick={onClose} className="p-1 hover:bg-black/5 rounded-lg transition-colors">
            <X size={16} />
          </button>
        )}
      </motion.div>
    </AnimatePresence>
  );
}
