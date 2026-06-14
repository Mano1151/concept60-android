import { createContext, useContext, useEffect, useMemo, useState, useCallback } from 'react';
import { auth, googleProvider } from '../firebase';
import { getRedirectResult } from 'firebase/auth';
import {
  getIdToken,
  onAuthStateChanged,
  sendPasswordResetEmail,
  signInWithPopup,
  signInWithRedirect,
  signOut,
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
} from 'firebase/auth';

const AuthContext = createContext({
  user: null,
  loading: true,
  login: async () => {},
  emailLogin: async () => {},
  signup: async () => {},
  resetPassword: async () => {},
  logout: async () => {},
  getToken: async () => null,
});

export const useAuth = () => useContext(AuthContext);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (currentUser) => {
      setUser(currentUser);
      setLoading(false);
    });
    getRedirectResult(auth)
    .then((result) => {
      if (result?.user) {
        setUser(result.user);
      }
    })
    .catch((error) => {
      console.error('Redirect result error:', error);
    });
    return unsubscribe;
  }, []);

const login = useCallback(async () => {
  await signInWithRedirect(auth, googleProvider);
}, []);

  const emailLogin = useCallback(async (email, password) => {
    try {
      const result = await signInWithEmailAndPassword(auth, email, password);
      setUser(result.user);
      return result.user;
    } catch (error) {
      console.error('Firebase email login error:', error);
      throw new Error(error?.message || 'Unable to sign in with email and password.');
    }
  }, []);

  const signup = useCallback(async (email, password) => {
    try {
      const result = await createUserWithEmailAndPassword(auth, email, password);
      setUser(result.user);
      return result.user;
    } catch (error) {
      console.error('Firebase signup error:', error);
      throw new Error(error?.message || 'Unable to create your account.');
    }
  }, []);

  const resetPassword = useCallback(async (email) => {
    try {
      await sendPasswordResetEmail(auth, email);
    } catch (error) {
      console.error('Firebase reset password error:', error);
      throw new Error(error?.message || 'Unable to send password reset email.');
    }
  }, []);

  const logout = useCallback(async () => {
    await signOut(auth);
    setUser(null);
  }, []);

  const getToken = useCallback(async () => {
    if (!auth.currentUser) {
      return null;
    }
    return getIdToken(auth.currentUser);
  }, []);

  const contextValue = useMemo(
    () => ({ user, loading, login, emailLogin, signup, resetPassword, logout, getToken }),
    [user, loading, login, emailLogin, signup, resetPassword, logout, getToken]
  );

  return <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>;
}
