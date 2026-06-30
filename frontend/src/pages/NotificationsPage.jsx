import { useEffect, useMemo, useState } from 'react'
import { notificationsApi } from '../api/notifications.js'

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [updatingId, setUpdatingId] = useState(null)
  useEffect(() => { notificationsApi.list().then(setNotifications).catch(() => setError('알림을 불러오지 못했습니다.')).finally(() => setLoading(false)) }, [])
  const ordered = useMemo(() => [...notifications].sort((a, b) => Number(a.read) - Number(b.read) || new Date(b.createdAt) - new Date(a.createdAt)), [notifications])

  const markRead = async (id) => {
    setUpdatingId(id)
    setError('')
    try {
      await notificationsApi.markRead(id)
      setNotifications((items) => items.map((item) => item.id === id ? { ...item, read: true } : item))
    } catch {
      setError('알림을 읽음 처리하지 못했습니다.')
    } finally {
      setUpdatingId(null)
    }
  }

  return (
    <section className="content-section notifications-page">
      <header className="page-heading"><div><span className="eyebrow">NOTIFICATIONS</span><h1>알림</h1></div><p>새로운 교환 경로가 생기면 이곳에서 알려드려요.</p></header>
      {loading && <p className="status-message">알림을 불러오고 있어요.</p>}
      {error && <p role="alert" className="error-message">{error}</p>}
      {!loading && notifications.length === 0 && <div className="empty-state"><h2>새 알림이 없습니다.</h2></div>}
      <div className="notification-list">{ordered.map((notification) => (
        <article data-testid="notification-item" className={`notification-item ${notification.read ? '' : 'unread'}`} key={notification.id}>
          <span className="notification-dot" aria-label={notification.read ? '읽음' : '읽지 않음'} />
          <div><h2>{notification.title}</h2><p>{notification.message}</p><time>{new Date(notification.createdAt).toLocaleString('ko-KR')}</time></div>
          {!notification.read && <button className="secondary-button" type="button" disabled={updatingId === notification.id} onClick={() => markRead(notification.id)}>읽음 처리</button>}
        </article>
      ))}</div>
    </section>
  )
}
