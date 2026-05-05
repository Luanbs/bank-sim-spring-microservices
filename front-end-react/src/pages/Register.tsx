import React, { useState, useMemo } from "react";
import { useNavigate, Navigate, Link } from "react-router-dom";
import Alert from "../components/Alert";
import { getErrorMessage } from "../services/errorhandler";
import { register } from "../services/authService";
import { useAuth } from "../context/AuthContext";
import { motion } from "framer-motion";
import { Lock, User, ArrowRight, ShieldPlus, CheckCircle2, Mail, MapPin, Check, X } from "lucide-react";

// Matches Java: @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&^()\\-_=+]).{8,}$")
const PASSWORD_REGEX = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&^()\-_=+]).{8,}$/;

function usePasswordValidation(password: string) {
  return useMemo(() => {
    const rules = [
      { label: "At least 8 characters", met: password.length >= 8 },
      { label: "At least one letter", met: /[A-Za-z]/.test(password) },
      { label: "At least one number", met: /\d/.test(password) },
      { label: "At least one special character (@$!%*#?&^()-_=+)", met: /[@$!%*#?&^()\-_=+]/.test(password) },
    ];
    const allMet = password.length > 0 && rules.every((r) => r.met);
    return { rules, allMet };
  }, [password]);
}

export default function Register() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [location, setLocation] = useState("");
  const [errorMsg, setErrorMsg] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const { token } = useAuth();
  const navigate = useNavigate();
  const { rules, allMet: passwordValid } = usePasswordValidation(password);

  if (token) {
    return <Navigate to="/" replace />;
  }

  const handleRegister = async (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    
    if (!username || !password || !confirmPassword || !fullName || !email || !location) {
      setErrorMsg("Please fill in all fields.");
      return;
    }

    if (!PASSWORD_REGEX.test(password)) {
      setErrorMsg("Password does not meet the security requirements.");
      return;
    }

    if (password !== confirmPassword) {
      setErrorMsg("Passwords do not match.");
      return;
    }

    try {
      setIsLoading(true);
      setErrorMsg("");

      await register(username, password, fullName, email, location);

      navigate("/login", {
        state: { successMsg: "Registration successful! Please log in." },
      });
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
          <div className="inline-flex items-center justify-center w-16 h-16 bg-brand-secondary text-white rounded-2xl mb-6 shadow-xl shadow-brand-secondary/20">
            <ShieldPlus size={32} />
          </div>
          <h1 className="text-3xl font-bold tracking-tight mb-2">Create Account</h1>
          <p className="text-brand-text-muted">Join BankSim and start simulating your finances.</p>
        </div>

        <div className="glass-card p-8 lg:p-10">
          <Alert message={errorMsg} type="error" onClose={() => setErrorMsg("")} />

          <form onSubmit={handleRegister} className="space-y-6">
            <div className="space-y-2">
              <label className="text-sm font-semibold text-brand-primary ml-1">Full Name</label>
              <div className="relative group">
                <User className="absolute left-4 top-1/2 -translate-y-1/2 text-brand-text-muted group-focus-within:text-brand-primary transition-colors" size={18} />
                <input
                  type="text"
                  placeholder="Enter your full name"
                  className="w-full pl-12 pr-4 py-3 bg-transparent border border-brand-border rounded-xl focus:ring-2 focus:ring-brand-secondary/10 focus:border-brand-secondary transition-all outline-none text-brand-primary"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                />
              </div>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-semibold text-brand-primary ml-1">Email</label>
              <div className="relative group">
                <Mail className="absolute left-4 top-1/2 -translate-y-1/2 text-brand-text-muted group-focus-within:text-brand-primary transition-colors" size={18} />
                <input
                  type="email"
                  placeholder="Enter your email"
                  className="w-full pl-12 pr-4 py-3 bg-transparent border border-brand-border rounded-xl focus:ring-2 focus:ring-brand-secondary/10 focus:border-brand-secondary transition-all outline-none text-brand-primary"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-semibold text-brand-primary ml-1">Location</label>
              <div className="relative group">
                <MapPin className="absolute left-4 top-1/2 -translate-y-1/2 text-brand-text-muted group-focus-within:text-brand-primary transition-colors" size={18} />
                <input
                  type="text"
                  placeholder="Enter your location"
                  className="w-full pl-12 pr-4 py-3 bg-transparent border border-brand-border rounded-xl focus:ring-2 focus:ring-brand-secondary/10 focus:border-brand-secondary transition-all outline-none text-brand-primary"
                  value={location}
                  onChange={(e) => setLocation(e.target.value)}
                />
              </div>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-semibold text-brand-primary ml-1">Username</label>
              <div className="relative group">
                <User className="absolute left-4 top-1/2 -translate-y-1/2 text-brand-text-muted group-focus-within:text-brand-primary transition-colors" size={18} />
                <input
                  type="text"
                  placeholder="Choose a username"
                  className="w-full pl-12 pr-4 py-3 bg-transparent border border-brand-border rounded-xl focus:ring-2 focus:ring-brand-secondary/10 focus:border-brand-secondary transition-all outline-none text-brand-primary"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                />
              </div>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-semibold text-brand-primary ml-1">Password</label>
              <div className="relative group">
                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-brand-text-muted group-focus-within:text-brand-primary transition-colors" size={18} />
                <input
                  type="password"
                  placeholder="Create a strong password"
                  className="w-full pl-12 pr-4 py-3 bg-transparent border border-brand-border rounded-xl focus:ring-2 focus:ring-brand-secondary/10 focus:border-brand-secondary transition-all outline-none text-brand-primary"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
              </div>

              {/* Password strength indicators */}
              {password.length > 0 && (
                <motion.div
                  initial={{ opacity: 0, height: 0 }}
                  animate={{ opacity: 1, height: "auto" }}
                  exit={{ opacity: 0, height: 0 }}
                  className="mt-3 p-3 rounded-xl border border-brand-border bg-brand-primary/5 space-y-1.5"
                >
                  {rules.map((rule) => (
                    <div key={rule.label} className="flex items-center gap-2">
                      {rule.met ? (
                        <Check size={14} className="text-brand-accent shrink-0" />
                      ) : (
                        <X size={14} className="text-rose-500 dark:text-rose-400 shrink-0" />
                      )}
                      <span
                        className={`text-xs transition-colors ${
                          rule.met ? "text-brand-accent" : "text-brand-text-muted"
                        }`}
                      >
                        {rule.label}
                      </span>
                    </div>
                  ))}
                </motion.div>
              )}
            </div>

            <div className="space-y-2">
              <label className="text-sm font-semibold text-brand-primary ml-1">Confirm Password</label>
              <div className="relative group">
                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-brand-text-muted group-focus-within:text-brand-primary transition-colors" size={18} />
                <input
                  type="password"
                  placeholder="Confirm your password"
                  className="w-full pl-12 pr-4 py-3 bg-transparent border border-brand-border rounded-xl focus:ring-2 focus:ring-brand-secondary/10 focus:border-brand-secondary transition-all outline-none text-brand-primary"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                />
              </div>
            </div>

            <div className="space-y-4">
              <div className="flex items-start gap-3 p-3 bg-brand-primary/5 rounded-xl border border-brand-border">
                <CheckCircle2 className="text-brand-accent mt-0.5" size={16} />
                <p className="text-xs text-brand-text-muted leading-relaxed">
                  I agree to the <span className="text-brand-secondary font-semibold hover:underline cursor-pointer">Terms of Service</span> and <span className="text-brand-secondary font-semibold hover:underline cursor-pointer">Privacy Policy</span>.
                </p>
              </div>

              <button 
                type="submit" 
                disabled={isLoading || (password.length > 0 && !passwordValid)}
                className="bg-brand-secondary text-white w-full flex items-center justify-center gap-2 py-4 rounded-xl font-medium transition-all hover:opacity-90 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isLoading ? (
                  <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                ) : (
                  <>
                    <span>Create Account</span>
                    <ArrowRight size={18} />
                  </>
                )}
              </button>
            </div>
          </form>

          <div className="mt-8 pt-8 border-t border-brand-border text-center">
            <p className="text-brand-text-muted text-sm">
              Already have an account?{" "}
              <Link to="/login" className="text-brand-secondary font-bold hover:underline">
                Sign in here
              </Link>
            </p>
          </div>
        </div>
      </motion.div>
    </div>
  );
}
