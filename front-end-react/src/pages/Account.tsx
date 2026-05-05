import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Shield, Bell, ChevronRight, Mail, MapPin } from "lucide-react";
import { motion } from "framer-motion";
import { getErrorMessage } from "../services/errorhandler";
import Modal from "../components/Modal";
import { api } from "../services/api";
import { Profile } from "../types/account";


export default function Account() {
  const navigate = useNavigate();
  const [userData, setUserData] = useState<Profile | null>(null);
  const [formData, setFormData] = useState<Profile | null>(null);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [isEditOpen, setIsEditOpen] = useState(false);

  useEffect(() => {
    async function fetchUserProfile() {
      try {
        const response = await api.get("/auth/user/profile");
        setUserData(response.data);
      } catch (error) {
        const msg = getErrorMessage(error);
        setErrorMsg(msg);
      } finally {
        setLoading(false);
      }
    }

    fetchUserProfile();
  }, []);

  useEffect(() => {
    if (userData) {
      setFormData(userData);
    }
  }, [userData]);

  if (loading) return <div>Loading...</div>;
  if (errorMsg) return <div>{errorMsg}</div>;
  if (!formData) return null;

 

  const handleSave: (React.SubmitEventHandler<HTMLFormElement>) = (e) => {
    e.preventDefault();
    setUserData(formData);
    setIsEditOpen(false);
  };

  const sections = [
    {
      title: "Security",
      items: [
        {
          id: "security",
          icon: <Shield className="text-amber-500" />,
          label: "Security & Privacy",
          desc: "Two-factor authentication and privacy",
          path: "/settings",
        },
        {
          id: "notifications",
          icon: <Bell className="text-purple-500" />,
          label: "Notifications",
          desc: "Configure how you receive alerts",
          path: "/settings",
        },
      ],
    },
  ];

  return (
    <div className="space-y-8">
      <header>
        <h2 className="text-3xl font-bold text-brand-primary ition-colors duration-300">
          Account
        </h2>
        <p className="text-brand-text-muted mt-1 transition-colors duration-300">
          Manage your profile and account settings.
        </p>
      </header>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="bg-brand-card p-8 rounded-3xl border border-brand-border shadow-sm flex flex-col md:flex-row gap-8 items-center md:items-start transition-colors duration-300"
      >
        <div className="flex-1 space-y-4 text-center md:text-left">
          <div>
            <h3 className="text-2xl font-bold text-brand-primary transition-colors duration-300">
              {localStorage.getItem('ownerName')}
            </h3>
            <p className="text-brand-text-muted font-medium transition-colors duration-300">
              Premium Member
            </p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="flex items-center justify-center md:justify-start gap-2 text-brand-text-muted transition-colors duration-300">
              <Mail size={18} className="text-brand-text-muted" />
              <span>{userData?.email}</span>
            </div>
            <div className="flex items-center justify-center md:justify-start gap-2 text-brand-text-muted transition-colors duration-300">
              <MapPin size={18} className="text-brand-text-muted" />
              <span>{userData?.location}</span>
            </div>
          </div>

          <button
            onClick={() => {
              setFormData(userData);
              setIsEditOpen(true);
            }}
            className="px-6 py-2 bg-brand-primary text-brand-bg rounded-xl font-medium hover:opacity-90 transition-all active:scale-95"
          >
            Edit Profile
          </button>
        </div>
      </motion.div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {sections.map((section, idx) => (
          <div key={idx} className="space-y-4">
            <h4 className="text-sm font-bold text-brand-text-muted uppercase tracking-wider px-2 transition-colors duration-300">
              {section.title}
            </h4>
            <div className="bg-brand-card rounded-3xl border border-brand-border divide-y divide-brand-border overflow-hidden shadow-sm transition-colors duration-300">
              {section.items.map((item) => (
                <button
                  key={item.id}
                  onClick={() => navigate(item.path)}
                  className="w-full flex items-center gap-4 p-5 hover:bg-brand-primary/5 transition-colors text-left group"
                >
                  <div className="w-12 h-12 rounded-2xl bg-brand-bg flex items-center justify-center group-hover:bg-brand-card transition-colors duration-300">
                    {item.icon}
                  </div>
                  <div className="flex-1">
                    <div className="font-bold text-brand-primary transition-colors duration-300">
                      {item.label}
                    </div>
                    <div className="text-sm text-brand-text-muted transition-colors duration-300">
                      {item.desc}
                    </div>
                  </div>
                  <ChevronRight
                    size={20}
                    className="text-brand-border group-hover:text-brand-primary transition-colors duration-300"
                  />
                </button>
              ))}
            </div>
          </div>
        ))}
      </div>

      <Modal
        isOpen={isEditOpen}
        onClose={() => setIsEditOpen(false)}
        title="Edit Profile"
      >
        <form onSubmit={handleSave} className="space-y-6">
          <div className="space-y-4">
            <div className="space-y-2">
              <label className="text-sm font-bold text-brand-text-muted uppercase tracking-wider">
                Email Address
              </label>
              <input
                type="email"
                value={formData?.email}
                onChange={(e) =>
                  setFormData({ ...formData, email: e.target.value })
                }
                className="w-full bg-brand-bg border border-brand-border rounded-xl px-4 py-3 text-brand-primary focus:ring-2 focus:ring-brand-primary focus:outline-none transition-all"
                required
              />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-bold text-brand-text-muted uppercase tracking-wider">
                Location
              </label>
              <input
                type="text"
                value={formData.location}
                onChange={(e) =>
                  setFormData({ ...formData, location: e.target.value })
                }
                className="w-full bg-brand-bg border border-brand-border rounded-xl px-4 py-3 text-brand-primary focus:ring-2 focus:ring-brand-primary focus:outline-none transition-all"
                required
              />
            </div>
          </div>
          <button
            type="submit"
            className="w-full bg-brand-primary text-brand-bg py-4 rounded-xl font-bold text-lg hover:opacity-90 transition-all active:scale-95"
          >
            Save Changes
          </button>
        </form>
      </Modal>
    </div>
  );
}
