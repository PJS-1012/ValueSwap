import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import CycleFlow from './CycleFlow.jsx'

describe('순환 교환 흐름', () => {
  it('각 교환 방향과 제공 항목, 점수를 순서대로 보여 준다', () => {
    render(<CycleFlow edges={[
      { orderIndex: 1, fromNickname: '농부', toNickname: '식당', provideItemName: '감자 10kg', score: 92 },
      { orderIndex: 0, fromNickname: '식당', toNickname: '디자이너', provideItemName: '식사권', score: 88 },
      { orderIndex: 2, fromNickname: '디자이너', toNickname: '농부', provideItemName: '로고 디자인', score: 85 },
    ]} />)

    const steps = screen.getAllByTestId('cycle-edge')
    expect(steps).toHaveLength(3)
    expect(steps[0]).toHaveTextContent('식당 → 디자이너')
    expect(steps[0]).toHaveTextContent('식사권')
    expect(steps[0]).toHaveTextContent('88점')
    expect(steps[2]).toHaveTextContent('디자이너 → 농부')
  })
})
