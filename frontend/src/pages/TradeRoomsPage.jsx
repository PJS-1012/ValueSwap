import { Link } from 'react-router-dom'
import { useTradeRooms } from '../trades/TradeRoomProvider.jsx'
import { formatMessageTime, formatUnreadCount } from '../trades/tradeFormat.js'

const statusLabels = { IN_EXCHANGE: '거래 진행 중', COMPLETED: '거래 완료' }

export default function TradeRoomsPage() {
  const { rooms } = useTradeRooms()
  const ordered = [...rooms].sort((a, b) => new Date(b.recentMessageAt || b.createdAt) - new Date(a.recentMessageAt || a.createdAt))
  return <section className="content-section trade-rooms-page">
    <header className="page-heading"><div><span className="eyebrow">CONVERSATIONS</span><h1>대화</h1></div><p>진행 중이거나 완료된 거래의 대화방을 확인하세요.</p></header>
    {ordered.length === 0 && <div className="empty-state"><h2>아직 거래방이 없습니다.</h2><p>매칭 참여자 전원이 수락하면 거래방이 열립니다.</p></div>}
    <div className="trade-room-list">{ordered.map((room) => <Link className="trade-room-card" data-testid="trade-room-card" to={`/trades/${room.id}`} key={room.id} aria-label={`${room.participants.join(', ')} 대화방`}>
      <div><span className="status-pill">{statusLabels[room.status] || room.status}</span><h2>{room.participants.join(', ')}</h2><p>{room.recentMessage || '거래방이 열렸습니다.'}</p></div>
      <div className="trade-room-meta">{room.recentMessageAt && <time>{formatMessageTime(room.recentMessageAt)}</time>}{room.unreadCount > 0 && <b className="notification-count">{formatUnreadCount(room.unreadCount)}</b>}</div>
    </Link>)}</div>
  </section>
}
