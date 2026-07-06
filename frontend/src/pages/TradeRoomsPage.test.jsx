import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import TradeRoomsPage from './TradeRoomsPage.jsx'

vi.mock('../trades/TradeRoomProvider.jsx', () => ({ useTradeRooms: () => ({ rooms: [
  { id: 2, status: 'IN_EXCHANGE', participants: ['나', '상대'], recentMessage: '최근 메시지', recentMessageAt: '2026-07-06T14:35:00+09:00', unreadCount: 100 },
] }) }))

describe('대화 목록', () => {
  it('최근 메시지와 99+ 안 읽음 배지를 표시한다', () => {
    render(<MemoryRouter><TradeRoomsPage /></MemoryRouter>)
    const card = screen.getByTestId('trade-room-card')
    expect(card).toHaveTextContent('최근 메시지')
    expect(card).toHaveTextContent('99+')
    expect(screen.getByRole('link', { name: /나, 상대/ })).toHaveAttribute('href', '/trades/2')
  })
})
