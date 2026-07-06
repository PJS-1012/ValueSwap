import { describe, expect, it } from 'vitest'
import { formatMessageTime, formatUnreadCount } from './tradeFormat.js'

describe('거래방 표시 형식', () => {
  it('안 읽은 메시지가 100개 이상이면 99+로 표시한다', () => {
    expect(formatUnreadCount(0)).toBe('')
    expect(formatUnreadCount(99)).toBe('99')
    expect(formatUnreadCount(100)).toBe('99+')
    expect(formatUnreadCount(142)).toBe('99+')
  })

  it('오늘, 올해, 이전 연도 메시지 시각을 구분한다', () => {
    const now = new Date('2026-07-06T14:35:00+09:00')
    expect(formatMessageTime('2026-07-06T14:35:00+09:00', now)).toBe('오후 2:35')
    expect(formatMessageTime('2026-06-01T09:05:00+09:00', now)).toBe('6월 1일 오전 9:05')
    expect(formatMessageTime('2025-12-31T23:20:00+09:00', now)).toBe('2025년 12월 31일 오후 11:20')
  })
})
