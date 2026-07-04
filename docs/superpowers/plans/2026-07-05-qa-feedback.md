# QA Feedback Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 교환 글 입력 UX를 개선하고 새 매칭 알림부터 참여자 전원 수락까지의 흐름을 완성한다.

**Architecture:** Flyway V5로 선택형 가치 정책과 알림 참조를 추가한다. 기존 매칭 도메인의 참여 상태를 서비스 API로 연결하고, React에서는 NotificationProvider가 15초 polling·배지·toast를 담당한다.

**Tech Stack:** Java 17, Spring Boot, JPA, Flyway, React, Vitest, Testing Library

---

### Task 1: 선택형 항목 데이터 계약

**Files:**
- Create: `backend/src/main/java/com/valueswap/post/domain/ValuePolicy.java`
- Create: `backend/src/main/resources/db/migration/V5__qa_feedback.sql`
- Modify: `ProvideItem`, `WantItem`, 요청·응답 DTO, `ItemMatchScorer`
- Test: `ItemMatchScorerTest`, `ExchangePostControllerTest`

- [ ] 비금액 가치 정책과 선택 세부카테고리 실패 테스트를 작성한다.
- [ ] 테스트가 기존 `@NotBlank`, `@NotNull`과 점수 계산 때문에 실패하는지 확인한다.
- [ ] nullable 필드, `ValuePolicy`, V5 마이그레이션과 조건부 검증을 구현한다.
- [ ] 백엔드 관련 테스트를 통과시킨다.
- [ ] `feat: 선택형 가치 정책과 항목 입력 완화`로 커밋한다.

### Task 2: 매칭 참여 수락 API

**Files:**
- Modify: `MatchParticipant`, `MatchCandidate`, `MatchingService`, `MatchController`, `ExchangePost`
- Test: `MatchingServiceIntegrationTest`

- [ ] 참여자 수락·거절·전원 수락·비참여자 403·종료 후보 409 테스트를 작성한다.
- [ ] `PATCH /api/matches/{id}/accept`, `/reject`가 없어 실패하는지 확인한다.
- [ ] 자기 참여 상태만 변경하고 전원 수락 시 후보와 글 상태를 변경한다.
- [ ] 통합 테스트를 통과시킨다.
- [ ] `feat: 매칭 참여 수락과 거래 진행 상태 연결`로 커밋한다.

### Task 3: 알림 참조와 상태 알림

**Files:**
- Modify: `Notification`, `NotificationResponse`, `NotificationService`, `MatchingService`, V5 migration
- Test: `NotificationControllerTest`, `MatchingServiceIntegrationTest`

- [ ] 매칭 알림의 `referenceId`와 수락 결과 알림 테스트를 작성한다.
- [ ] 알림 생성 API에 optional reference를 추가하고 중복 방지 키는 유지한다.
- [ ] 상태 변경 시 모든 참여자에게 알림을 생성한다.
- [ ] 관련 테스트를 통과시킨다.
- [ ] `feat: 매칭 이동 가능한 상태 알림 추가`로 커밋한다.

### Task 4: 교환 글 폼과 카드 UX

**Files:**
- Create: `frontend/src/data/regions.js`
- Modify: `PostCard`, `PostFormPage`, `ItemCard`, `styles.css`
- Test: `PostCard.test.jsx`, `PostFormPage.test.jsx`

- [ ] 카드 전체 링크, 지역 select, 조건부 가치 입력, 선택 세부카테고리, 필드 오류 강조 테스트를 작성한다.
- [ ] 기존 UI에서 테스트가 실패하는지 확인한다.
- [ ] 17개 지역 선택과 `aria-invalid` 기반 필드 오류 UI를 구현한다.
- [ ] 프론트 관련 테스트와 build를 통과시킨다.
- [ ] `feat: 교환 글 선택 입력과 검증 UX 개선`으로 커밋한다.

### Task 5: 전역 알림 polling과 배지

**Files:**
- Create: `frontend/src/notifications/NotificationProvider.jsx`
- Modify: `main.jsx`, `AppShell`, `NotificationsPage`, `styles.css`
- Test: `NotificationProvider.test.jsx`, `NotificationsPage.test.jsx`

- [ ] unread 개수, 매칭 점, 새 알림 toast, 알림 클릭 이동 테스트를 작성한다.
- [ ] 15초 polling과 visibilitychange 즉시 갱신을 구현한다.
- [ ] 알림 카드 클릭 시 읽음 처리 후 reference 경로로 이동한다.
- [ ] 프론트 전체 테스트와 build를 통과시킨다.
- [ ] `feat: 실시간 알림 배지와 매칭 이동 추가`로 커밋한다.

### Task 6: 매칭 수락 화면과 전체 검증

**Files:**
- Modify: `frontend/src/api/matches.js`, `MatchDetailPage.jsx`, `styles.css`, `README.md`, `docs/api.md`
- Test: `MatchDetailPage.test.jsx`

- [ ] 현재 참여자의 수락·거절 버튼과 상태 갱신 테스트를 작성한다.
- [ ] 매칭 API와 상세 UI를 구현한다.
- [ ] 백엔드 전체 테스트, 프론트 전체 테스트·build, Compose·비밀정보 검사를 실행한다.
- [ ] 문서를 갱신하고 `feat: QA 피드백 거래 흐름 완성`으로 커밋한다.
- [ ] 푸시 전에 사용자에게 타이밍을 알리고 `feat/core-slice`를 푸시한다.

