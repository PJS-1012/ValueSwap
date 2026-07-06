# Trade Room Visual Polish Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 거래방의 메시지 구분, 완료 상태 카드, 여백, 최대 5줄 자동 증가 입력창을 개선한다.

**Architecture:** 기존 `TradeRoomPage`의 데이터 흐름은 유지한다. 입력창 높이 계산만 작은 유틸 함수와 `textarea` ref로 분리하고, 시각 구분은 의미 있는 클래스와 CSS로 처리한다.

**Tech Stack:** React 19, Vitest, Testing Library, CSS

---

### Task 1: 자동 높이 입력창 동작

**Files:**
- Modify: `frontend/src/pages/TradeRoomPage.test.jsx`
- Modify: `frontend/src/pages/TradeRoomPage.jsx`

- [ ] **Step 1: 실패 테스트 작성**

`TradeRoomPage.test.jsx`에 `scrollHeight`가 5줄 기준 높이보다 작은 경우 해당 높이로 증가하고, 큰 경우 5줄 높이에서 제한되는 테스트를 추가한다. 전송 후 높이가 빈 문자열로 초기화되는지도 확인한다.

```jsx
const input = await screen.findByLabelText('메시지 입력')
Object.defineProperty(input, 'scrollHeight', { configurable: true, value: 160 })
fireEvent.change(input, { target: { value: '1\n2\n3\n4\n5\n6' } })
expect(input.style.height).toBe('120px')
expect(input.style.overflowY).toBe('auto')
fireEvent.click(screen.getByRole('button', { name: '전송' }))
expect(input.style.height).toBe('')
```

- [ ] **Step 2: 실패 확인**

Run: `npm.cmd test -- --run src/pages/TradeRoomPage.test.jsx`

Expected: 입력창 높이가 설정되지 않아 FAIL.

- [ ] **Step 3: 최소 구현**

`TradeRoomPage.jsx`에 textarea ref와 높이 조절 함수를 추가한다. CSS의 5줄 제한과 맞춰 최대 높이는 120px로 둔다.

```jsx
const messageInputRef = useRef(null)
const resizeMessageInput = (element) => {
  element.style.height = 'auto'
  const height = Math.min(element.scrollHeight, 120)
  element.style.height = `${height}px`
  element.style.overflowY = element.scrollHeight > 120 ? 'auto' : 'hidden'
}
```

`onChange`에서 값을 저장한 뒤 높이를 조절하고, 전송 성공 시 `style.height`와 `style.overflowY`를 초기화한다.

- [ ] **Step 4: 대상 테스트 통과 확인**

Run: `npm.cmd test -- --run src/pages/TradeRoomPage.test.jsx`

Expected: PASS.

### Task 2: 메시지와 상태 카드 시각 개선

**Files:**
- Modify: `frontend/src/pages/TradeRoomPage.test.jsx`
- Modify: `frontend/src/pages/TradeRoomPage.jsx`
- Modify: `frontend/src/styles.css`

- [ ] **Step 1: 실패 테스트 작성**

내 메시지에는 `mine`, 상대 메시지에는 `theirs` 클래스가 적용되고, 참여자 상태에는 `completed` 또는 `in-progress` 클래스가 붙는지 검증한다.

```jsx
expect(screen.getByText('내 메시지').closest('article')).toHaveClass('mine')
expect(screen.getByText('상대 메시지').closest('article')).toHaveClass('theirs')
expect(screen.getByText('진행 중').closest('.trade-member-chip')).toHaveClass('in-progress')
```

- [ ] **Step 2: 실패 확인**

Run: `npm.cmd test -- --run src/pages/TradeRoomPage.test.jsx`

Expected: `theirs`, `trade-member-chip` 클래스가 없어 FAIL.

- [ ] **Step 3: JSX와 CSS 구현**

상대 메시지에 `theirs` 클래스를 명시하고 참여자 상태를 칩 마크업으로 변경한다. CSS는 다음 기준을 적용한다.

```css
.trade-member-status { padding: 24px; margin: 28px 0; background: var(--soft); }
.trade-member-chip { display: inline-flex; gap: 8px; padding: 8px 12px; border-radius: 999px; }
.trade-member-chip.in-progress { background: #eef3e7; color: var(--green); }
.trade-member-chip.completed { background: var(--green); color: white; }
.chat-message.theirs { background: #ece9df; color: var(--ink); }
.message-form textarea { resize: none; min-height: 52px; max-height: 120px; overflow-y: hidden; }
```

메시지 목록과 입력·완료 버튼 사이의 세로 여백도 20~28px 범위로 확대한다.

- [ ] **Step 4: 대상 테스트 통과 확인**

Run: `npm.cmd test -- --run src/pages/TradeRoomPage.test.jsx`

Expected: PASS.

### Task 3: 전체 검증과 커밋

**Files:**
- Verify: `frontend/src/pages/TradeRoomPage.jsx`
- Verify: `frontend/src/pages/TradeRoomPage.test.jsx`
- Verify: `frontend/src/styles.css`

- [ ] **Step 1: 전체 프론트 테스트 실행**

Run: `npm.cmd test -- --run`

Expected: 모든 테스트 PASS.

- [ ] **Step 2: 프로덕션 빌드 실행**

Run: `npm.cmd run build`

Expected: Vite build exit code 0.

- [ ] **Step 3: 변경 범위 확인**

Run: `git diff --check && git status --short`

Expected: 공백 오류 없음. 사용자 소유 `backend/src/main/java/com/valueswap/ValueSwapApplication.java`는 스테이징하지 않는다.

- [ ] **Step 4: 구현 커밋**

```powershell
git add frontend/src/pages/TradeRoomPage.jsx frontend/src/pages/TradeRoomPage.test.jsx frontend/src/styles.css
git commit -m "style: 거래방 대화와 입력 영역 개선"
```
