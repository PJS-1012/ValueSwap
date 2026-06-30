import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { authApi } from '../api/auth.js'
import { TOKEN_KEY } from '../api/client.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  const logout = () => {
    localStorage.removeItem(TOKEN_KEY)
    setUser(null)
  }

  useEffect(() => {
    const restore = async () => {
      if (!localStorage.getItem(TOKEN_KEY)) {
        setLoading(false)
        return
      }
      try {
        setUser(await authApi.me())
      } catch {
        logout()
      } finally {
        setLoading(false)
      }
    }
    restore()
    window.addEventListener('valueswap:unauthorized', logout)
    return () => window.removeEventListener('valueswap:unauthorized', logout)
  }, [])

  const login = async (credentials) => {
    const auth = await authApi.login(credentials)
    localStorage.setItem(TOKEN_KEY, auth.accessToken)
    const me = await authApi.me()
    setUser(me)
    return me
  }

  const signup = (payload) => authApi.signup(payload)

  const value = useMemo(() => ({ user, loading, login, signup, logout }), [user, loading])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth는 AuthProvider 안에서 사용해야 합니다.')
  return context
}
