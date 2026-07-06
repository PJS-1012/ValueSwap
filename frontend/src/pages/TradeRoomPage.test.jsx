import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { tradeRoomsApi } from '../api/tradeRooms.js'
import { useTradeRooms } from '../trades/TradeRoomProvider.jsx'
import TradeRoomPage from './TradeRoomPage.jsx'

vi.mock('../api/tradeRooms.js', () => ({ tradeRoomsApi: { detail: vi.fn(), messages: vi.fn(), complete: vi.fn() } }))
vi.mock('../auth/AuthProvider.jsx', () => ({ useAuth: () => ({ user: { id: 1 } }) }))
vi.mock('../trades/TradeRoomProvider.jsx', () => ({ useTradeRooms: vi.fn() }))

describe('거래방', () => {
  const send = vi.fn()
  const markRoomRead = vi.fn().mockResolvedValue(undefined)
  beforeEach(() => {
    vi.clearAllMocks()
    useTradeRooms.mockReturnValue({ send, subscribe: () => () => {}, markRoomRead, connectionState: 'connected' })
    tradeRoomsApi.detail.mockResolvedValue({ id: 2, status: 'IN_EXCHANGE', members: [
      { userId: 1, nickname: '나', completedAt: null }, { userId: 3, nickname: '상대', completedAt: null },
    ] })
    tradeRoomsApi.messages.mockResolvedValue({ messages: [
      { id: 10, senderId: 3, senderNickname: '상대', content: '안녕하세요', createdAt: new Date().toISOString() },
    ], hasMore: false })
  })

  it('과거 메시지의 발신자·본문·전송 시각을 표시하고 새 메시지를 전송한다', async () => {
    const user = userEvent.setup()
    render(<MemoryRouter initialEntries={['/trades/2']}><Routes><Route path="/trades/:id" element={<TradeRoomPage />} /></Routes></MemoryRouter>)

    expect(await screen.findByText('안녕하세요')).toBeInTheDocument()
    expect(screen.getByText('상대')).toBeInTheDocument()
    expect(screen.getByText(/오전|오후/)).toBeInTheDocument()
    await user.type(screen.getByLabelText('메시지 입력'), '거래 장소는 어디인가요?')
    await user.click(screen.getByRole('button', { name: '전송' }))

    expect(send).toHaveBeenCalledWith('2', expect.objectContaining({ content: '거래 장소는 어디인가요?' }))
  })

  it('완료된 거래방은 메시지를 입력할 수 없다', async () => {
    tradeRoomsApi.detail.mockResolvedValue({ id: 2, status: 'COMPLETED', members: [] })
    render(<MemoryRouter initialEntries={['/trades/2']}><Routes><Route path="/trades/:id" element={<TradeRoomPage />} /></Routes></MemoryRouter>)
    expect(await screen.findByText('완료된 거래는 대화 내용을 읽을 수만 있습니다.')).toBeInTheDocument()
    expect(screen.queryByLabelText('메시지 입력')).not.toBeInTheDocument()
  })
})
