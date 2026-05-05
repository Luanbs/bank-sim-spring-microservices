import axios from "axios";

export const api = axios.create({
  baseURL: import.meta.env.VITE_API || "http://localhost:8080",
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("access_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const isLogin = error.config.url?.includes("/auth/login");
    const isRegisterPage = window.location.pathname === '/register';
    if (error.response?.status === 401 && !isLogin && !isRegisterPage) {
      localStorage.removeItem("access_token");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);