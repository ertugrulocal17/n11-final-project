import { apiFetch, getStoredToken, setStoredToken } from '@/lib/api'
import type { AuthResponse, UserProfile } from '@/types/models'
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'

type AuthState = {
  token: string | null
  user: UserProfile | null
  loading: boolean
}

type AuthContextValue = AuthState & {
  login: (email: string, password: string) => Promise<void>
  register: (email: string, password: string) => Promise<void>
  logout: () => void
  refreshUser: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => getStoredToken())
  const [user, setUser] = useState<UserProfile | null>(null)
  const [loading, setLoading] = useState(true)

  const refreshUser = useCallback(async () => {
    const t = getStoredToken()
    if (!t) {
      setUser(null)
      setLoading(false)
      return
    }
    try {
      const me = await apiFetch<UserProfile>('/api/v1/me', { token: t })
      setUser(me)
    } catch {
      setStoredToken(null)
      setToken(null)
      setUser(null)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void refreshUser()
  }, [refreshUser])

  const login = useCallback(async (email: string, password: string) => {
    const res = await apiFetch<AuthResponse>('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
      token: null,
    })
    setStoredToken(res.accessToken)
    setToken(res.accessToken)
    await refreshUser()
  }, [refreshUser])

  const register = useCallback(async (email: string, password: string) => {
    const res = await apiFetch<AuthResponse>('/api/v1/auth/register', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
      token: null,
    })
    setStoredToken(res.accessToken)
    setToken(res.accessToken)
    await refreshUser()
  }, [refreshUser])

  const logout = useCallback(() => {
    setStoredToken(null)
    setToken(null)
    setUser(null)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      token,
      user,
      loading,
      login,
      register,
      logout,
      refreshUser,
    }),
    [token, user, loading, login, register, logout, refreshUser],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
