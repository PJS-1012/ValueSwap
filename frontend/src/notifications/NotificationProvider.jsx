import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react'
import { notificationsApi } from '../api/notifications.js'
import { useAuth } from '../auth/AuthProvider.jsx'

const NotificationContext = createContext(null)

export function NotificationProvider({ children }) {
  const { user } = useAuth()
  const [notifications, setNotifications] = useState([])
  const [toast, setToast] = useState(null)
  const knownIds = useRef(null)

  const refresh = useCallback(async () => {
    if (!user) { setNotifications([]); knownIds.current = null; return }
    try {
      const next = await notificationsApi.list()
      if (knownIds.current) {
        const incoming = next.find((item) => !item.read && !knownIds.current.has(item.id))
        if (incoming) { setToast(incoming); window.setTimeout(() => setToast(null), 5000) }
      }
      knownIds.current = new Set(next.map((item) => item.id))
      setNotifications(next)
    } catch { /* 화면 사용을 막지 않고 다음 polling에서 재시도 */ }
  }, [user?.id])

  useEffect(() => {
    refresh()
    if (!user) return undefined
    const timer = window.setInterval(refresh, 15000)
    const onVisible = () => { if (document.visibilityState === 'visible') refresh() }
    document.addEventListener('visibilitychange', onVisible)
    return () => { window.clearInterval(timer); document.removeEventListener('visibilitychange', onVisible) }
  }, [refresh, user?.id])

  const markRead = async (id) => {
    await notificationsApi.markRead(id)
    setNotifications((items) => items.map((item) => item.id === id ? { ...item, read: true } : item))
  }
  const unread = notifications.filter((item) => !item.read)
  const value = useMemo(() => ({ notifications, unreadCount: unread.length,
    hasUnreadMatches: unread.some((item) => item.type?.startsWith('MATCH_')), refresh, markRead }), [notifications, refresh])

  return <NotificationContext.Provider value={value}>{children}{toast && <div className="notification-toast" role="status"><strong>{toast.title}</strong><span>{toast.message}</span></div>}</NotificationContext.Provider>
}

export function useNotifications() {
  const value = useContext(NotificationContext)
  if (!value) throw new Error('useNotifications는 NotificationProvider 안에서 사용해야 합니다.')
  return value
}
