import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { matchesApi } from '../api/matches.js'
import MatchDetailPage from './MatchDetailPage.jsx'

vi.mock('../api/matches.js', () => ({ matchesApi: { detail: vi.fn(), accept: vi.fn(), reject: vi.fn() } }))
vi.mock('../auth/AuthProvider.jsx', () => ({ useAuth: () => ({ user: { id: 7 } }) }))

const detail = { id: 3, score: 90, matchType: 'THREE_PARTY', status: 'FOUND', edges: [], participants: [
  { userId: 7, nickname: '나', postTitle: '내 글', acceptStatus: 'PENDING' },
] }

describe('매칭 상세 참여', () => {
  beforeEach(() => { vi.clearAllMocks(); matchesApi.detail.mockResolvedValue(detail) })

  it('현재 참여자가 수락할 수 있다', async () => {
    matchesApi.accept.mockResolvedValue({ ...detail, participants: [{ ...detail.participants[0], acceptStatus: 'ACCEPTED' }] })
    render(<MemoryRouter initialEntries={['/matches/3']}><Routes><Route path="/matches/:id" element={<MatchDetailPage />} /></Routes></MemoryRouter>)
    await userEvent.click(await screen.findByRole('button', { name: '참여 수락' }))
    expect(matchesApi.accept).toHaveBeenCalledWith('3')
    expect(await screen.findByText('수락 완료')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: /매우 높은 적합도/ })).toBeInTheDocument()
  })
})
