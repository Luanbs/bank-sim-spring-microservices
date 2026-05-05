import { api } from "./api";


export async function login(username: string, password: string) {
  const response = await api.post('/auth/login', {
    username,
    password,
  });

  localStorage.setItem("access_token", response.data.token || response.data.access_token);
}

export async function register(username: string, password: string, fullName: string, email: string, location: string) {
  return await api.post('/auth/register', { 
    username, 
    password,
    fullName,
    email,
    location
  });
}

export async function logout() {
  const token = getToken();
  if (token) {
    try {
      await api.post('/auth/logout', {}, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
    } catch (error) {
      console.error("Logout failed", error);
    }
  }
  localStorage.removeItem("access_token");
}

export function getToken() {
  return localStorage.getItem("access_token");
}

export function isAuthenticated() {
  return !!getToken();
}
