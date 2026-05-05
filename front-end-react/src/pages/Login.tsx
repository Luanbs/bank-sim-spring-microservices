import React, { useState } from "react";
import { useLocation, Navigate, Link } from "react-router-dom";
import Alert from "../components/Alert";
import { getErrorMessage } from "../services/errorhandler";
import { useAuth } from "../context/AuthContext";
import { motion } from "framer-motion";
import { Lock, User, ArrowRight, ShieldCheck } from "lucide-react";

export default function Login() {
  const location = useLocation();
  const successMsg = location.state?.successMsg || "";
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [errorMsg, setErrorMsg] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const { login, token } = useAuth();

  if (token) {
    return <Navigate to="/" replace />;
  }

  const handleSubmit = async (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!username || !password) {
      setErrorMsg("Please fill in all fields.");
      return;
    }
    
    try {
      setIsLoading(true);
      setErrorMsg("");
      await login(username, password);
    } catch (error) {
      const msg = getErrorMessage(error);
      setErrorMsg(msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-brand-bg p-6">
      <motion.div 
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="w-full max-w-md"
      >
        <div className="text-center mb-10">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-brand-primary text-white rounded-2xl mb-6 shadow-xl shadow-brand-primary/20">
            <ShieldCheck size={32} />
          </div>
          <h1 className="text-3xl font-bold tracking-tight mb-2">Welcome Back</h1>
          <p className="text-brand-text-muted">Securely access your bank simulator account.</p>
        </div>

        <div className="glass-card p-8 lg:p-10">
          <Alert message={errorMsg} type="error" onClose={() => setErrorMsg("")} />
          <Alert message={successMsg} type="success" />

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="space-y-2">
              <label className="text-sm font-semibold text-brand-primary ml-1">Username</label>
              <div className="relative group">
                <User className="absolute left-4 top-1/2 -translate-y-1/2 text-brand-text-muted group-focus-within:text-brand-primary transition-colors" size={18} />
                <input
                  type="text"
                  placeholder="Enter your username"
                  className="w-full pl-12 pr-4 py-3 bg-transparent border border-brand-border rounded-xl focus:ring-2 focus:ring-brand-primary/10 focus:border-brand-primary transition-all outline-none text-brand-primary"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                />
              </div>
            </div>

            <div className="space-y-2">
              <div className="flex items-center justify-between ml-1">
                <label className="text-sm font-semibold text-brand-primary">Password</label>
                <button type="button" className="text-xs font-semibold text-brand-secondary hover:underline">Forgot password?</button>
              </div>
              <div className="relative group">
                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-brand-text-muted group-focus-within:text-brand-primary transition-colors" size={18} />
                <input
                  type="password"
                  placeholder="••••••••"
                  className="w-full pl-12 pr-4 py-3 bg-transparent border border-brand-border rounded-xl focus:ring-2 focus:ring-brand-primary/10 focus:border-brand-primary transition-all outline-none text-brand-primary"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
              </div>
            </div>

            <button 
              type="submit" 
              disabled={isLoading}
              className="btn-primary w-full flex items-center justify-center gap-2 py-4 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? (
                <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <>
                  <span>Sign In</span>
                  <ArrowRight size={18} />
                </>
              )}
            </button>
          </form>

          <div className="mt-8 pt-8 border-t border-brand-border text-center">
            <p className="text-brand-text-muted text-sm">
              Don't have an account?{" "}
              <Link to="/register" className="text-brand-secondary font-bold hover:underline">
                Create an account
              </Link>
            </p>
          </div>
        </div>

        <p className="text-center mt-10 text-xs text-brand-text-muted font-medium uppercase tracking-widest">
          Protected by BankSim Security
        </p>
      </motion.div>
    </div>
  );
}
