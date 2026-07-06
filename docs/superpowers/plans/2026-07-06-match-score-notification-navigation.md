# Match Score and Notification Navigation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 매칭 점수를 평가 가능한 항목 기준 100점으로 환산하고 매칭 알림 카드에서 상세 화면으로 이동하게 한다.

**Architecture:** 점수 환산은 백엔드 `ItemMatchScorer`가 단일 기준으로 수행하고 프론트는 공용 적합도 표시 유틸을 사용한다. 알림 이동은 기존 `referenceId`를 사용하며 읽음 처리 성공 후 라우팅한다.

**Tech Stack:** Java 17, Spring Boot, JUnit 5, React, React Router, Vitest, Testing Library

---

### Task 1: 100점 적합도 계산

**Files:**
- Modify: `backend/src/test/java/com/valueswap/matching/score/ItemMatchScorerTest.java`
- Modify: `backend/src/main/java/com/valueswap/matching/score/ItemMatchScorer.java`

- [ ] 선택 항목이 모두 평가 가능한 경우 최고 조합이 100점인지 실패 테스트를 작성한다.
- [ ] 세부 카테고리와 가치가 평가 불가능한 경우 해당 최대점을 분모에서 제외하는 실패 테스트를 작성한다.
- [ ] 두 테스트를 실행해 기존 140점 원점수 때문에 실패하는지 확인한다.
- [ ] 원점수와 적용 가능한 최대점을 함께 계산해 반올림한 0~100 점수를 반환한다.
- [ ] 점수 단위 테스트를 다시 실행한다.

### Task 2: 적합도 등급과 기준 안내

**Files:**
- Create: `frontend/src/matching/matchScore.js`
- Create: `frontend/src/matching/matchScore.test.js`
- Modify: `frontend/src/pages/MatchesPage.jsx`
- Modify: `frontend/src/pages/MatchDetailPage.jsx`
- Modify: `frontend/src/styles.css`

- [ ] 85·70·55·40점 경계의 한글 등급을 검증하는 실패 테스트를 작성한다.
- [ ] `getMatchFitLabel(score)`를 최소 구현해 테스트를 통과시킨다.
- [ ] 목록과 상세에 `점수 · 등급`을 표시하고 100점 기준 설명을 추가한다.
- [ ] 관련 화면 테스트를 갱신하고 실행한다.

### Task 3: 알림 카드 전체 이동

**Files:**
- Modify: `frontend/src/pages/NotificationsPage.test.jsx`
- Modify: `frontend/src/pages/NotificationsPage.jsx`
- Modify: `frontend/src/styles.css`

- [ ] `referenceId`가 있는 알림 카드 클릭 시 읽음 처리 후 상세 경로가 렌더링되는 실패 테스트를 작성한다.
- [ ] Enter/Space 키와 읽음 처리 버튼의 이벤트 분리를 검증하는 실패 테스트를 작성한다.
- [ ] 카드 접근성 속성과 공용 열기 함수를 구현한다.
- [ ] 알림 화면 테스트를 다시 실행한다.

### Task 4: 전체 검증

**Files:**
- Modify: `README.md`
- Modify: `docs/api.md`

- [ ] 사용자 문서와 API 문서에 100점 환산 및 적합도 기준을 기록한다.
- [ ] `backend\\gradlew.bat test`를 실행해 전체 백엔드 테스트 통과를 확인한다.
- [ ] `npm.cmd test -- --run`과 `npm.cmd run build`를 실행한다.
- [ ] `git diff --check`와 `.env` 무시 상태를 확인한다.
- [ ] 사용자의 최종 확인 전에는 커밋하거나 푸시하지 않는다.
