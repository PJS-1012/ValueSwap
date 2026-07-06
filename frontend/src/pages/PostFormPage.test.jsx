import { beforeEach, describe, expect, it, vi } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { postsApi } from '../api/posts.js'
import { authApi } from '../api/auth.js'
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

vi.mock('../api/notifications.js', () => ({ notificationsApi: { list: vi.fn().mockResolvedValue([]), markRead: vi.fn() } }))
vi.mock('../api/tradeRooms.js', () => ({ tradeRoomsApi: { list: vi.fn().mockResolvedValue([]) } }))

describe('교환 글 등록 화면', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.setItem('valueswap_token', 'test-token')
    authApi.me.mockResolvedValue({ id: 1, nickname: '교환왕', role: 'USER' })
    postsApi.detail.mockResolvedValue({
      id: 10,
      title: '동네 식사권 교환',
      description: '서로 필요한 것을 바꿔요',
      region: '서울 마포구',
      status: 'ACTIVE',
      author: { nickname: '교환왕', trustScore: 50, role: 'USER' },
      provideItems: [],
      wantItems: [],
    })
  })

  it('항목을 추가·삭제하고 태그를 구조화된 요청으로 전송한다', async () => {
    const user = userEvent.setup()
    postsApi.create.mockResolvedValue({ id: 10 })
    renderApp('/posts/new')

    await screen.findByRole('heading', { name: '교환 글 등록' })
    await user.type(screen.getByLabelText('제목'), '동네 식사권 교환')
    await user.type(screen.getByLabelText('설명'), '서로 필요한 것을 바꿔요')
    await user.selectOptions(screen.getByLabelText('거래 지역'), '서울특별시')

    await user.click(screen.getByRole('button', { name: '제공 항목 추가' }))
    expect(screen.getAllByTestId('provide-item')).toHaveLength(2)
    await user.click(screen.getAllByRole('button', { name: '제공 항목 삭제' })[1])
    expect(screen.getAllByTestId('provide-item')).toHaveLength(1)

    const provide = screen.getByTestId('provide-item')
    await user.selectOptions(within(provide).getByLabelText('제공 카테고리'), 'COUPON')
    await user.type(within(provide).getByLabelText('제공 세부 카테고리 (선택)'), '식사권')
    await user.type(within(provide).getByLabelText('제공 물품·서비스'), '돈까스 식사권')
    await user.type(within(provide).getByLabelText('제공 가치'), '12000')
    await user.type(within(provide).getByLabelText('제공 태그'), '돈까스, 점심,  마포 ')

    const want = screen.getByTestId('want-item')
    await user.selectOptions(within(want).getByLabelText('희망 카테고리'), 'FOOD_MATERIAL')
    await user.type(within(want).getByLabelText('희망 세부 카테고리 (선택)'), '농산물')
    await user.type(within(want).getByLabelText('희망 물품·서비스'), '감자')
    await user.type(within(want).getByLabelText('희망 최소 가치'), '8000')
    await user.type(within(want).getByLabelText('희망 최대 가치'), '15000')

    await user.click(screen.getByRole('button', { name: '교환 글 등록' }))

    await waitFor(() => expect(postsApi.create).toHaveBeenCalledOnce())
    expect(postsApi.create).toHaveBeenCalledWith(expect.objectContaining({
      provideItems: [expect.objectContaining({
        name: '돈까스 식사권',
        tags: ['돈까스', '점심', '마포'],
      })],
      wantItems: [expect.objectContaining({ name: '감자', minValue: 8000, maxValue: 15000 })],
    }))
  })

  it('희망 최소 가치가 최대 가치보다 크면 요청하지 않는다', async () => {
    const user = userEvent.setup()
    renderApp('/posts/new')
    await screen.findByRole('heading', { name: '교환 글 등록' })

    const want = screen.getByTestId('want-item')
    await user.type(within(want).getByLabelText('희망 최소 가치'), '20000')
    await user.type(within(want).getByLabelText('희망 최대 가치'), '10000')
    await user.click(screen.getByRole('button', { name: '교환 글 등록' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('최소 가치는 최대 가치보다 클 수 없습니다.')
    expect(postsApi.create).not.toHaveBeenCalled()
  })

  it('지역과 가치 방식을 선택하고 누락 필드를 강조한다', async () => {
    const user = userEvent.setup()
    renderApp('/posts/new')
    await screen.findByRole('heading', { name: '교환 글 등록' })

    expect(screen.getByLabelText('거래 지역').tagName).toBe('SELECT')
    expect(screen.getByLabelText('희망 물품·서비스')).toBeInTheDocument()
    await user.selectOptions(screen.getByLabelText('제공 가치 방식'), 'NEGOTIABLE')
    expect(screen.queryByLabelText('제공 가치')).not.toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: '교환 글 등록' }))

    expect(screen.getByLabelText(/^제목/)).toHaveAttribute('aria-invalid', 'true')
    expect(screen.getByLabelText(/^거래 지역/)).toHaveAttribute('aria-invalid', 'true')
  })
})
