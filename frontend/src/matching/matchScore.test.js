import { describe, expect, it } from 'vitest'
import { getMatchFitLabel } from './matchScore.js'

describe('매칭 적합도 등급', () => {
  it.each([
    [100, '매우 높은 적합도'], [85, '매우 높은 적합도'],
    [84, '높은 적합도'], [70, '높은 적합도'],
    [69, '보통 적합도'], [55, '보통 적합도'],
    [54, '낮은 적합도'], [40, '낮은 적합도'],
    [39, '매우 낮은 적합도'], [0, '매우 낮은 적합도'],
  ])('%i점의 등급을 %s로 표시한다', (score, expected) => {
    expect(getMatchFitLabel(score)).toBe(expected)
  })
})
