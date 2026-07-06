import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useNotifications } from '../notifications/NotificationProvider.jsx'

export default function NotificationsPage() {
  const { notifications, markRead } = useNotifications()
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const [updatingId, setUpdatingId] = useState(null)
  const ordered = useMemo(() => [...notifications].sort((a, b) => Number(a.read) - Number(b.read) || new Date(b.createdAt) - new Date(a.createdAt)), [notifications])
  const read = async (notification, open = false) => {
    setUpdatingId(notification.id); setError('')
    try { if (!notification.read) await markRead(notification.id); if (open && notification.referenceId) navigate(`/matches/${notification.referenceId}`) }
    catch { setError('알림을 읽음 처리하지 못했습니다.') }
    finally { setUpdatingId(null) }
  }
  const openFromCard = (notification) => {
    if (notification.referenceId && updatingId !== notification.id) read(notification, true)
  }
  const handleCardKeyDown = (event, notification) => {
    if (!notification.referenceId || !['Enter', ' '].includes(event.key)) return
    event.preventDefault()
    openFromCard(notification)
  }
  return <section className="content-section notifications-page">
    <header className="page-heading"><div><span className="eyebrow">NOTIFICATIONS</span><h1>알림</h1></div><p>새로운 교환 경로가 생기면 이곳에서 알려드려요.</p></header>
    {error && <p role="alert" className="error-message">{error}</p>}
    {ordered.length === 0 && <div className="empty-state"><h2>새 알림이 없습니다.</h2></div>}
    <div className="notification-list">{ordered.map((notification) => <article data-testid="notification-item" className={`notification-item ${notification.read ? '' : 'unread'} ${notification.referenceId ? 'clickable' : ''}`} key={notification.id}
      role={notification.referenceId ? 'link' : undefined} tabIndex={notification.referenceId ? 0 : undefined}
      aria-label={notification.referenceId ? `${notification.title} 매칭 상세 보기` : undefined}
      onClick={() => openFromCard(notification)} onKeyDown={(event) => handleCardKeyDown(event, notification)}>
      <span className="notification-dot" aria-label={notification.read ? '읽음' : '읽지 않음'} /><div><h2>{notification.title}</h2><p>{notification.message}</p><time>{new Date(notification.createdAt).toLocaleString('ko-KR')}</time></div>
      <div className="notification-actions">{notification.referenceId && <button className="primary-button compact" onClick={(event) => { event.stopPropagation(); read(notification, true) }}>거래 확인</button>}{!notification.read && <button className="secondary-button" disabled={updatingId === notification.id} onClick={(event) => { event.stopPropagation(); read(notification) }}>읽음 처리</button>}</div>
    </article>)}</div>
  </section>
}
