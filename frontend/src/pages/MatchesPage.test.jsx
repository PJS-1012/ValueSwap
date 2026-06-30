import { render, screen, within } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { matchesApi } from '../api/matches.js'
import MatchesPage from './MatchesPage.jsx'

vi.mock('../api/matches.js', () => ({ matchesApi: { mine: vi.fn(), detail: vi.fn() } }))

const match = (id, score, createdAt) => ({
  id, score, createdAt, status: 'PROPOSED', matchType: 'THREE_PARTY',
  participants: [{ userId: id, nickname: `사용자${id}`, postId: id, postTitle: `글${id}`, orderIndex: 0, acceptStatus: 'PENDING' }],
  edges: [{ orderIndex: 0, fromNickname: `사용자${id}`, toNickname: '상대', provideItemName: `항목${id}`, score }],
})

describe('내 매칭 화면', () => {
  beforeEach(() => vi.clearAllMocks())

  it('점수 내림차순, 동점이면 최신 생성일 순으로 표시한다', async () => {
    matchesApi.mine.mockResolvedValue([
      match(1, 80, '2026-06-29T10:00:00'),
      match(2, 95, '2026-06-28T10:00:00'),
      match(3, 95, '2026-06-30T10:00:00'),
    ])
    render(<MemoryRouter><MatchesPage /></MemoryRouter>)

    const cards = await screen.findAllByTestId('match-card')
    expect(within(cards[0]).getByLabelText('매칭 점수')).toHaveTextContent('95점')
    expect(within(cards[0]).getByText('사용자3')).toBeInTheDocument()
    expect(within(cards[1]).getByText('사용자2')).toBeInTheDocument()
    expect(within(cards[2]).getByLabelText('매칭 점수')).toHaveTextContent('80점')
  })
})
