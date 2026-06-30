import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { notificationsApi } from '../api/notifications.js'
import NotificationsPage from './NotificationsPage.jsx'

vi.mock('../api/notifications.js', () => ({ notificationsApi: { list: vi.fn(), markRead: vi.fn() } }))

describe('알림 화면', () => {
  beforeEach(() => vi.clearAllMocks())

  it('읽지 않은 알림을 먼저 표시하고 서버 성공 후 읽음으로 바꾼다', async () => {
    const user = userEvent.setup()
    notificationsApi.list.mockResolvedValue([
      { id: 1, title: '읽은 알림', message: '이전 소식', read: true, createdAt: '2026-06-30T12:00:00' },
      { id: 2, title: '새 매칭', message: '3자 교환이 연결됐어요.', read: false, createdAt: '2026-06-29T12:00:00' },
    ])
    notificationsApi.markRead.mockResolvedValue({ id: 2, read: true })
    render(<NotificationsPage />)

    const items = await screen.findAllByTestId('notification-item')
    expect(within(items[0]).getByText('새 매칭')).toBeInTheDocument()
    expect(items[0]).toHaveClass('unread')
    await user.click(within(items[0]).getByRole('button', { name: '읽음 처리' }))

    expect(notificationsApi.markRead).toHaveBeenCalledWith(2)
    expect(items[0]).not.toHaveClass('unread')
    expect(within(items[0]).queryByRole('button', { name: '읽음 처리' })).not.toBeInTheDocument()
  })
})
