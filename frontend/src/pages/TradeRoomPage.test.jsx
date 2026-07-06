import { fireEvent, render, screen } from '@testing-library/react'
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

  it('내 메시지와 상대 메시지 및 참여자 상태를 시각적으로 구분한다', async () => {
    tradeRoomsApi.messages.mockResolvedValue({ messages: [
      { id: 10, senderId: 3, senderNickname: '상대', content: '안녕하세요', createdAt: new Date().toISOString() },
      { id: 11, senderId: 1, senderNickname: '나', content: '내 메시지', createdAt: new Date().toISOString() },
    ], hasMore: false })
    render(<MemoryRouter initialEntries={['/trades/2']}><Routes><Route path="/trades/:id" element={<TradeRoomPage />} /></Routes></MemoryRouter>)

    expect((await screen.findByText('내 메시지')).closest('article')).toHaveClass('mine')
    expect(screen.getByText('안녕하세요').closest('article')).toHaveClass('theirs')
    expect(screen.getAllByText('진행 중')[0].closest('.trade-member-chip')).toHaveClass('in-progress')
  })

  it('입력창은 최대 5줄까지 늘어나고 전송 후 기본 높이로 돌아간다', async () => {
    render(<MemoryRouter initialEntries={['/trades/2']}><Routes><Route path="/trades/:id" element={<TradeRoomPage />} /></Routes></MemoryRouter>)
    const input = await screen.findByLabelText('메시지 입력')
    Object.defineProperty(input, 'scrollHeight', { configurable: true, value: 160 })

    fireEvent.change(input, { target: { value: '1\n2\n3\n4\n5\n6' } })

    expect(input.style.height).toBe('148px')
    expect(input.style.overflowY).toBe('auto')
    fireEvent.click(screen.getByRole('button', { name: '전송' }))
    expect(input.style.height).toBe('')
    expect(input.style.overflowY).toBe('hidden')
  })

  it('Enter는 전송하고 Shift Enter와 IME 조합 중 Enter는 전송하지 않는다', async () => {
    render(<MemoryRouter initialEntries={['/trades/2']}><Routes><Route path="/trades/:id" element={<TradeRoomPage />} /></Routes></MemoryRouter>)
    const input = await screen.findByLabelText('메시지 입력')

    fireEvent.change(input, { target: { value: '바로 전송' } })
    fireEvent.keyDown(input, { key: 'Enter', shiftKey: false, isComposing: false })
    expect(send).toHaveBeenCalledWith('2', expect.objectContaining({ content: '바로 전송' }))

    send.mockClear()
    fireEvent.change(input, { target: { value: '줄바꿈' } })
    fireEvent.keyDown(input, { key: 'Enter', shiftKey: true })
    expect(send).not.toHaveBeenCalled()

    fireEvent.change(input, { target: { value: '조합 중' } })
    fireEvent.keyDown(input, { key: 'Enter', isComposing: true })
    expect(send).not.toHaveBeenCalled()
  })

  it('과거 메시지의 발신자·본문·전송 시각을 표시하고 새 메시지를 전송한다', async () => {
    const user = userEvent.setup()
    render(<MemoryRouter initialEntries={['/trades/2']}><Routes><Route path="/trades/:id" element={<TradeRoomPage />} /></Routes></MemoryRouter>)

    expect(await screen.findByText('안녕하세요')).toBeInTheDocument()
    expect(screen.getAllByText('상대').length).toBeGreaterThan(0)
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
