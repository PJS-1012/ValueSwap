import { render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { NotificationProvider, useNotifications } from './NotificationProvider.jsx'
import { notificationsApi } from '../api/notifications.js'

vi.mock('../auth/AuthProvider.jsx', () => ({ useAuth: () => ({ user: { id: 1 } }) }))
vi.mock('../api/notifications.js', () => ({ notificationsApi: { list: vi.fn(), markRead: vi.fn() } }))

function Probe() {
  const { unreadCount, hasUnreadMatches } = useNotifications()
  return <div>{unreadCount}:{String(hasUnreadMatches)}</div>
}

describe('전역 알림', () => {
  beforeEach(() => vi.clearAllMocks())

  it('unread 개수와 매칭 점을 제공한다', async () => {
    notificationsApi.list.mockResolvedValue([
      { id: 1, title: '새 매칭', type: 'MATCH_FOUND', read: false, referenceId: 3, createdAt: '2026-07-05T00:00:00' },
      { id: 2, title: '읽은 알림', type: 'MATCH_ACCEPTED', read: true, referenceId: 3, createdAt: '2026-07-04T00:00:00' },
    ])
    render(<NotificationProvider><Probe /></NotificationProvider>)

    await waitFor(() => expect(screen.getByText('1:true')).toBeInTheDocument())
  })
})
