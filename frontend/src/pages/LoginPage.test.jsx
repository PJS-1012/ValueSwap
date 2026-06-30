import { beforeEach, describe, expect, it, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { authApi } from '../api/auth.js'
import { postsApi } from '../api/posts.js'
import { renderApp } from '../test/renderApp.jsx'

vi.mock('../api/auth.js', () => ({
  authApi: {
    login: vi.fn(),
    signup: vi.fn(),
    me: vi.fn(),
  },
}))

vi.mock('../api/posts.js', () => ({
  postsApi: {
    list: vi.fn(),
    detail: vi.fn(),
    create: vi.fn(),
  },
}))

describe('로그인 화면', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    postsApi.list.mockResolvedValue([])
  })

  it('로그인 성공 시 토큰을 저장하고 홈으로 이동한다', async () => {
    const user = userEvent.setup()
    authApi.login.mockResolvedValue({ accessToken: 'test-token' })
    authApi.me.mockResolvedValue({ id: 1, nickname: '교환왕', role: 'USER' })

    renderApp('/login')
    await user.type(screen.getByLabelText('이메일'), 'user@test.com')
    await user.type(screen.getByLabelText('비밀번호'), 'Password1!')
    await user.click(screen.getByRole('button', { name: '로그인' }))

    await waitFor(() => expect(localStorage.getItem('valueswap_token')).toBe('test-token'))
    expect(await screen.findByRole('heading', { name: '최근 교환 글' })).toBeInTheDocument()
  })

  it('인증 실패 메시지를 보여 준다', async () => {
    const user = userEvent.setup()
    authApi.login.mockRejectedValue({ response: { status: 401 } })

    renderApp('/login')
    await user.type(screen.getByLabelText('이메일'), 'user@test.com')
    await user.type(screen.getByLabelText('비밀번호'), 'wrong-password')
    await user.click(screen.getByRole('button', { name: '로그인' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('이메일 또는 비밀번호를 확인해 주세요.')
    expect(localStorage.getItem('valueswap_token')).toBeNull()
  })
})
