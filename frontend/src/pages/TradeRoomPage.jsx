import { useEffect, useRef, useState } from 'react'
import { useParams } from 'react-router-dom'
import { tradeRoomsApi } from '../api/tradeRooms.js'
import { useAuth } from '../auth/AuthProvider.jsx'
import { useTradeRooms } from '../trades/TradeRoomProvider.jsx'
import { formatMessageTime } from '../trades/tradeFormat.js'

const MESSAGE_INPUT_MIN_HEIGHT = 52
const MESSAGE_INPUT_MAX_HEIGHT = 148

export default function TradeRoomPage() {
  const { id } = useParams()
  const { user } = useAuth()
  const { send, subscribe, markRoomRead, connectionState } = useTradeRooms()
  const [room, setRoom] = useState(null)
  const [messages, setMessages] = useState([])
  const [content, setContent] = useState('')
  const [error, setError] = useState('')
  const [updating, setUpdating] = useState(false)
  const endRef = useRef(null)
  const messageInputRef = useRef(null)

  useEffect(() => {
    let active = true
    Promise.all([tradeRoomsApi.detail(id), tradeRoomsApi.messages(id)]).then(([detail, page]) => {
      if (!active) return
      setRoom(detail); setMessages(page.messages); markRoomRead(id).catch(() => {})
    }).catch(() => setError('거래방을 불러오지 못했습니다.'))
    const unsubscribe = subscribe(id, (message) => {
      setMessages((items) => items.some((item) => item.id === message.id) ? items : [...items, message])
      markRoomRead(id).catch(() => {})
    })
    return () => { active = false; unsubscribe() }
  }, [id, subscribe, markRoomRead])

  useEffect(() => { endRef.current?.scrollIntoView?.({ behavior: 'smooth' }) }, [messages.length])
  const resizeMessageInput = (element) => {
    element.style.height = 'auto'
    element.style.height = `${Math.max(MESSAGE_INPUT_MIN_HEIGHT, Math.min(element.scrollHeight, MESSAGE_INPUT_MAX_HEIGHT))}px`
    element.style.overflowY = element.scrollHeight > MESSAGE_INPUT_MAX_HEIGHT ? 'auto' : 'hidden'
  }
  const submit = (event) => {
    event.preventDefault()
    const normalized = content.trim()
    if (!normalized) return
    const clientMessageId = globalThis.crypto?.randomUUID?.() || `${Date.now()}-${Math.random()}`
    send(id, { clientMessageId, content: normalized }); setContent('')
    if (messageInputRef.current) {
      messageInputRef.current.style.height = ''
      messageInputRef.current.style.overflowY = 'hidden'
    }
  }
  const complete = async () => {
    setUpdating(true); setError('')
    try { setRoom(await tradeRoomsApi.complete(id)) }
    catch (requestError) { setError(requestError.response?.data?.message || '거래 완료를 처리하지 못했습니다.') }
    finally { setUpdating(false) }
  }
  if (error && !room) return <p role="alert" className="error-message content-section">{error}</p>
  if (!room) return <p className="status-message">거래방을 불러오고 있어요.</p>
  const mine = room.members.find((member) => member.userId === user?.id)
  const completed = room.status === 'COMPLETED'
  return <section className="content-section trade-room-page">
    <header className="page-heading"><div><span className="eyebrow">TRADE ROOM</span><h1>{room.members.map((member) => member.nickname).join(', ')}</h1></div><p>{completed ? '완료된 거래' : connectionState === 'connected' ? '실시간 연결됨' : '연결을 복구하고 있습니다.'}</p></header>
    {error && <p role="alert" className="error-message">{error}</p>}
    <section className="trade-member-status panel"><h2>거래 완료 상태</h2><div className="trade-member-chips">{room.members.map((member) => <span className={`trade-member-chip ${member.completedAt ? 'completed' : 'in-progress'}`} key={member.userId}><strong>{member.nickname}</strong><span>{member.completedAt ? '완료' : '진행 중'}</span></span>)}</div></section>
    <div className="message-list" aria-live="polite">{messages.map((message) => <article className={`chat-message ${message.senderId === user?.id ? 'mine' : 'theirs'}`} key={message.id || message.clientMessageId}>
      <strong>{message.senderNickname}</strong><p>{message.content}</p><time>{formatMessageTime(message.createdAt)}</time>
    </article>)}<div ref={endRef} /></div>
    {completed ? <p className="readonly-message">완료된 거래는 대화 내용을 읽을 수만 있습니다.</p> : <>
      <form className="message-form" onSubmit={submit}><label className="sr-only" htmlFor="trade-message">메시지 입력</label><textarea id="trade-message" ref={messageInputRef} value={content} maxLength={1000} onChange={(event) => { setContent(event.target.value); resizeMessageInput(event.target) }} placeholder="메시지를 입력하세요" /><button className="primary-button" type="submit">전송</button></form>
      {!mine?.completedAt && <button className="secondary-button complete-trade-button" disabled={updating} onClick={complete}>거래 완료</button>}
    </>}
  </section>
}
