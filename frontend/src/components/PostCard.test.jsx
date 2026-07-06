import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import PostCard from './PostCard.jsx'

describe('교환 글 카드', () => {
  it('요약 API에 없는 항목 수를 0개라고 오해하게 표시하지 않는다', () => {
    render(<MemoryRouter><PostCard post={{
      id: 1,
      title: '식사권 교환',
      status: 'ACTIVE',
      region: '서울 마포구',
      createdAt: '2026-06-30T12:00:00',
      author: { nickname: '교환왕', trustScore: 50 },
    }} /></MemoryRouter>)

    expect(screen.queryByText('제공 0')).not.toBeInTheDocument()
    expect(screen.getByText(/등록$/)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /식사권 교환/ })).toHaveAttribute('href', '/posts/1')
    expect(screen.getByTestId('post-card')).toBe(screen.getByRole('link', { name: /식사권 교환/ }))
  })
})
