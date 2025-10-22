import React, { createContext, useState, useContext, useEffect } from 'react';
import { userService } from '../services/userService';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const userId = localStorage.getItem('userId');
    if (userId) {
      loadCurrentUser();
    } else {
      setLoading(false);
    }
  }, []);

  const loadCurrentUser = async () => {
    try {
      const user = await userService.getCurrentUser();
      setCurrentUser(user);
    } catch (error) {
      console.error('Failed to load user:', error);
      localStorage.removeItem('userId');
    } finally {
      setLoading(false);
    }
  };

  const register = async (userData) => {
    const user = await userService.register(userData);
    localStorage.setItem('userId', user.id);
    setCurrentUser(user);
    return user;
  };

  const login = async (userId) => {
    localStorage.setItem('userId', userId);
    await loadCurrentUser();
  };

  const logout = () => {
    localStorage.removeItem('userId');
    setCurrentUser(null);
  };

  const value = {
    currentUser,
    loading,
    register,
    login,
    logout,
    isAuthenticated: !!currentUser
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
