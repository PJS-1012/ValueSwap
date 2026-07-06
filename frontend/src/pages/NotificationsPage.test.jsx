import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { notificationsApi } from '../api/notifications.js'
import NotificationsPage from './NotificationsPage.jsx'
import { NotificationProvider } from '../notifications/NotificationProvider.jsx'
import { MemoryRouter, Route, Routes, useParams } from 'react-router-dom'

vi.mock('../api/notifications.js', () => ({ notificationsApi: { list: vi.fn(), markRead: vi.fn() } }))
vi.mock('../auth/AuthProvider.jsx', () => ({ useAuth: () => ({ user: { id: 1 } }) }))

describe('알림 화면', () => {
  beforeEach(() => vi.clearAllMocks())

  it('읽지 않은 알림을 먼저 표시하고 서버 성공 후 읽음으로 바꾼다', async () => {
    const user = userEvent.setup()
    notificationsApi.list.mockResolvedValue([
      { id: 1, title: '읽은 알림', message: '이전 소식', read: true, createdAt: '2026-06-30T12:00:00' },
      { id: 2, title: '새 매칭', message: '3자 교환이 연결됐어요.', read: false, createdAt: '2026-06-29T12:00:00' },
    ])
    notificationsApi.markRead.mockResolvedValue({ id: 2, read: true })
    render(<MemoryRouter><NotificationProvider><NotificationsPage /></NotificationProvider></MemoryRouter>)

    const items = await screen.findAllByTestId('notification-item')
    expect(within(items[0]).getByText('새 매칭')).toBeInTheDocument()
    expect(items[0]).toHaveClass('unread')
    await user.click(within(items[0]).getByRole('button', { name: '읽음 처리' }))

    expect(notificationsApi.markRead).toHaveBeenCalledWith(2)
    await waitFor(() => expect(items[0]).not.toHaveClass('unread'))
    expect(within(items[0]).queryByRole('button', { name: '읽음 처리' })).not.toBeInTheDocument()
  })

  it('매칭 알림 카드 전체를 누르면 읽음 처리 후 매칭 상세로 이동한다', async () => {
    const user = userEvent.setup()
    notificationsApi.list.mockResolvedValue([
      { id: 3, title: '새 매칭', message: '교환 후보가 생겼어요.', type: 'MATCH_FOUND', read: false, referenceId: 12, createdAt: '2026-07-06T12:00:00' },
    ])
    notificationsApi.markRead.mockResolvedValue({ id: 3, read: true })
    const Target = () => <p>매칭 {useParams().id}</p>
    render(<MemoryRouter initialEntries={['/notifications']}><NotificationProvider><Routes>
      <Route path="/notifications" element={<NotificationsPage />} />
      <Route path="/matches/:id" element={<Target />} />
    </Routes></NotificationProvider></MemoryRouter>)

    await user.click(await screen.findByRole('link', { name: /새 매칭/ }))

    expect(notificationsApi.markRead).toHaveBeenCalledWith(3)
    expect(await screen.findByText('매칭 12')).toBeInTheDocument()
  })

  it('Enter 키로 매칭 알림을 열 수 있다', async () => {
    const user = userEvent.setup()
    notificationsApi.list.mockResolvedValue([
      { id: 4, title: '수락 완료', message: '거래를 확인하세요.', type: 'MATCH_ACCEPTED', read: true, referenceId: 15, createdAt: '2026-07-06T13:00:00' },
    ])
    const Target = () => <p>매칭 {useParams().id}</p>
    render(<MemoryRouter initialEntries={['/notifications']}><NotificationProvider><Routes>
      <Route path="/notifications" element={<NotificationsPage />} />
      <Route path="/matches/:id" element={<Target />} />
    </Routes></NotificationProvider></MemoryRouter>)

    const card = await screen.findByRole('link', { name: /수락 완료/ })
    card.focus()
    await user.keyboard('{Enter}')

    expect(await screen.findByText('매칭 15')).toBeInTheDocument()
  })
})
