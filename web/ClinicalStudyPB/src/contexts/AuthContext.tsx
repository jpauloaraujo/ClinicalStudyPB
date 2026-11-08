import { createContext, useState, type ReactNode } from 'react';

interface AuthContextType {
  token: string | null;
  login: (token: string) => void;
  logout: () => void;
  isAuthenticated: boolean;
}

export const AuthContext = createContext<AuthContextType>({} as AuthContextType);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [token, setToken] = useState<string | null>(
    localStorage.getItem('@ClinicalStudy:token')
  );

  const login = (newToken: string) => {
    localStorage.setItem('@ClinicalStudy:token', newToken);
    setToken(newToken);
  };

  const logout = () => {
    localStorage.removeItem('@ClinicalStudy:token');
    setToken(null);
  };

  return (
    <AuthContext.Provider value={{ token, login, logout, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  );
};