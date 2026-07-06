import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react'
import { Client } from '@stomp/stompjs'
import { tradeRoomsApi } from '../api/tradeRooms.js'
import { TOKEN_KEY } from '../api/client.js'
import { useAuth } from '../auth/AuthProvider.jsx'

const TradeRoomContext = createContext(null)

function webSocketUrl() {
  if (import.meta.env.VITE_WS_URL) return import.meta.env.VITE_WS_URL
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const backendHost = import.meta.env.DEV ? 'localhost:8080' : window.location.host
  return `${protocol}//${backendHost}/ws`
}

export function TradeRoomProvider({ children }) {
  const { user } = useAuth()
  const [rooms, setRooms] = useState([])
  const [connectionState, setConnectionState] = useState('disconnected')
  const clientRef = useRef(null)
  const listenersRef = useRef(new Map())

  const refresh = useCallback(async () => {
    if (!user) { setRooms([]); return }
    try { setRooms(await tradeRoomsApi.list()) } catch { /* 다음 화면 진입이나 재연결 때 재시도 */ }
  }, [user?.id])

  useEffect(() => { refresh() }, [refresh])

  useEffect(() => {
    if (!user || rooms.length === 0) return undefined
    const token = localStorage.getItem(TOKEN_KEY)
    const subscriptions = []
    const client = new Client({
      brokerURL: webSocketUrl(), connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 3000,
      onConnect: () => {
        setConnectionState('connected')
        rooms.forEach((room) => subscriptions.push(client.subscribe(`/topic/trade-rooms/${room.id}`, (frame) => {
          const message = JSON.parse(frame.body)
          setRooms((items) => items.map((item) => item.id === room.id ? {
            ...item, recentMessage: message.content, recentMessageAt: message.createdAt,
            unreadCount: message.senderId === user.id ? item.unreadCount : item.unreadCount + 1,
          } : item))
          listenersRef.current.get(String(room.id))?.forEach((listener) => listener(message))
        })))
      },
      onWebSocketClose: () => setConnectionState('reconnecting'),
      onStompError: () => setConnectionState('reconnecting'),
    })
    clientRef.current = client
    setConnectionState('connecting')
    client.activate()
    return () => { subscriptions.forEach((subscription) => subscription.unsubscribe()); client.deactivate(); clientRef.current = null }
  }, [user?.id, rooms.map((room) => room.id).join(',')])

  const subscribe = useCallback((roomId, listener) => {
    const key = String(roomId)
    const listeners = listenersRef.current.get(key) || new Set()
    listeners.add(listener); listenersRef.current.set(key, listeners)
    return () => { listeners.delete(listener); if (listeners.size === 0) listenersRef.current.delete(key) }
  }, [])
  const send = useCallback((roomId, payload) => clientRef.current?.publish({
    destination: `/app/trade-rooms/${roomId}/messages`, body: JSON.stringify(payload),
  }), [])
  const markRoomRead = useCallback(async (roomId) => {
    await tradeRoomsApi.markRead(roomId)
    setRooms((items) => items.map((room) => room.id === Number(roomId) ? { ...room, unreadCount: 0 } : room))
  }, [])
  const totalUnread = rooms.reduce((sum, room) => sum + room.unreadCount, 0)
  const value = useMemo(() => ({ rooms, totalUnread, connectionState, refresh, subscribe, send, markRoomRead }),
    [rooms, totalUnread, connectionState, refresh, subscribe, send, markRoomRead])
  return <TradeRoomContext.Provider value={value}>{children}</TradeRoomContext.Provider>
}

export function useTradeRooms() {
  const value = useContext(TradeRoomContext)
  if (!value) throw new Error('useTradeRooms는 TradeRoomProvider 안에서 사용해야 합니다.')
  return value
}
