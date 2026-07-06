import { render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { tradeRoomsApi } from '../api/tradeRooms.js'
import { TradeRoomProvider, useTradeRooms } from './TradeRoomProvider.jsx'

vi.mock('../api/tradeRooms.js', () => ({ tradeRoomsApi: { list: vi.fn() } }))
vi.mock('../auth/AuthProvider.jsx', () => ({ useAuth: () => ({ user: { id: 1 } }) }))
vi.mock('@stomp/stompjs', () => ({ Client: vi.fn(() => ({ activate: vi.fn(), deactivate: vi.fn() })) }))

function Probe() {
  const { rooms, totalUnread } = useTradeRooms()
  return <p>{rooms.length}개 방 / {totalUnread}개 안 읽음</p>
}

describe('거래방 전역 상태', () => {
  beforeEach(() => vi.clearAllMocks())

  it('내 거래방과 전체 안 읽은 메시지 수를 제공한다', async () => {
    tradeRoomsApi.list.mockResolvedValue([
      { id: 1, unreadCount: 99 },
      { id: 2, unreadCount: 43 },
    ])

    render(<TradeRoomProvider><Probe /></TradeRoomProvider>)

    expect(await screen.findByText('2개 방 / 142개 안 읽음')).toBeInTheDocument()
  })
})
