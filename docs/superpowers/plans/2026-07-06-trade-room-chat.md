# Trade Room Chat Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 매칭 전원 수락 후 참여자 전용 거래방을 만들고 실시간 텍스트 채팅, 안 읽은 메시지, 전원 거래 완료 흐름을 제공한다.

**Architecture:** 백엔드 `trade` 기능 패키지가 거래방·구성원·메시지를 소유하고, REST는 목록·과거 내역·읽음·완료를 담당하며 STOMP는 새 메시지 전달을 담당한다. 프론트는 REST로 초기·복구 상태를 만들고 STOMP 구독으로 실시간 메시지를 합치며 `대화` 메뉴에서 전체 방을 탐색한다.

**Tech Stack:** Java 17, Spring Boot 3.5, Spring WebSocket/STOMP, Spring Data JPA, Flyway, MySQL/H2, React 19, React Router, `@stomp/stompjs`, Vitest

---

### Task 1: 거래방 스키마와 도메인

**Files:**
- Modify: `backend/build.gradle`
- Create: `backend/src/main/resources/db/migration/V7__trade_rooms_and_messages.sql`
- Create: `backend/src/main/java/com/valueswap/trade/domain/TradeRoomStatus.java`
- Create: `backend/src/main/java/com/valueswap/trade/domain/TradeRoom.java`
- Create: `backend/src/main/java/com/valueswap/trade/domain/TradeRoomMember.java`
- Create: `backend/src/main/java/com/valueswap/trade/domain/ChatMessage.java`
- Create: `backend/src/test/java/com/valueswap/trade/domain/TradeRoomTest.java`

- [ ] **Step 1: 거래 완료 도메인 실패 테스트 작성**

```java
@Test
void everyMemberCompletionClosesRoom() {
    TradeRoom room = TradeRoom.create(candidate, List.of(user1, user2));
    room.complete(user1.getId());
    assertThat(room.getStatus()).isEqualTo(TradeRoomStatus.IN_EXCHANGE);
    room.complete(user2.getId());
    assertThat(room.getStatus()).isEqualTo(TradeRoomStatus.COMPLETED);
}
```

- [ ] **Step 2: 실패 확인**

Run: `cd backend; .\gradlew.bat test --tests com.valueswap.trade.domain.TradeRoomTest`
Expected: `TradeRoom` 타입 부재로 FAIL.

- [ ] **Step 3: WebSocket 의존성과 스키마 추가**

```gradle
implementation 'org.springframework.boot:spring-boot-starter-websocket'
```

```sql
CREATE TABLE trade_rooms (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  match_candidate_id BIGINT NOT NULL UNIQUE,
  status VARCHAR(30) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL,
  completed_at TIMESTAMP(6),
  CONSTRAINT fk_trade_room_match FOREIGN KEY (match_candidate_id) REFERENCES match_candidates(id)
);
CREATE TABLE trade_room_members (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  trade_room_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  last_read_message_id BIGINT,
  completed_at TIMESTAMP(6),
  CONSTRAINT uk_trade_room_member UNIQUE (trade_room_id, user_id),
  CONSTRAINT fk_trade_member_room FOREIGN KEY (trade_room_id) REFERENCES trade_rooms(id),
  CONSTRAINT fk_trade_member_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE TABLE chat_messages (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  trade_room_id BIGINT NOT NULL,
  sender_id BIGINT NOT NULL,
  client_message_id VARCHAR(36) NOT NULL,
  content VARCHAR(1000) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT uk_chat_message_client UNIQUE (trade_room_id, sender_id, client_message_id),
  CONSTRAINT fk_chat_message_room FOREIGN KEY (trade_room_id) REFERENCES trade_rooms(id),
  CONSTRAINT fk_chat_message_sender FOREIGN KEY (sender_id) REFERENCES users(id)
);
CREATE INDEX idx_chat_message_room_id ON chat_messages(trade_room_id, id);
```

- [ ] **Step 4: 최소 도메인 구현**

`TradeRoom.create(candidate, users)`는 구성원을 만들고, `complete(userId)`는 해당 구성원의 완료 시각을 한 번만 기록하며 전원 완료 시 방 상태와 완료 시각을 변경한다. `ChatMessage.create`는 `trim()` 후 빈 문자열과 1000자 초과를 거부한다.

- [ ] **Step 5: 도메인 테스트 통과 확인**

Run: `cd backend; .\gradlew.bat test --tests com.valueswap.trade.domain.TradeRoomTest`
Expected: PASS.

### Task 2: 전원 수락과 거래 완료 연결

**Files:**
- Create: `backend/src/main/java/com/valueswap/trade/TradeRoomRepository.java`
- Create: `backend/src/main/java/com/valueswap/trade/TradeRoomService.java`
- Modify: `backend/src/main/java/com/valueswap/matching/MatchingService.java`
- Modify: `backend/src/main/java/com/valueswap/matching/domain/MatchCandidate.java`
- Modify: `backend/src/main/java/com/valueswap/post/domain/ExchangePost.java`
- Modify: `backend/src/test/java/com/valueswap/matching/MatchingServiceIntegrationTest.java`
- Create: `backend/src/test/java/com/valueswap/trade/TradeRoomServiceIntegrationTest.java`

- [ ] **Step 1: 마지막 수락 시 방 하나 생성 실패 테스트 작성**

```java
matchingService.accept(matchId, firstUserId);
matchingService.accept(matchId, secondUserId);
assertThat(tradeRoomRepository.findByMatchCandidateId(matchId)).isPresent();
assertThat(tradeRoomRepository.count()).isEqualTo(1);
matchingService.accept(matchId, secondUserId);
assertThat(tradeRoomRepository.count()).isEqualTo(1);
```

- [ ] **Step 2: 전원 완료 상태 전환 실패 테스트 작성**

```java
tradeRoomService.complete(roomId, firstUserId);
var result = tradeRoomService.complete(roomId, secondUserId);
assertThat(result.status()).isEqualTo(TradeRoomStatus.COMPLETED);
assertThat(candidateRepository.findById(matchId).orElseThrow().getStatus()).isEqualTo(MatchStatus.COMPLETED);
assertThat(postRepository.findAll()).allMatch(post -> post.getStatus() == PostStatus.COMPLETED);
```

- [ ] **Step 3: 실패 확인**

Run: `cd backend; .\gradlew.bat test --tests '*TradeRoomServiceIntegrationTest' --tests '*MatchingServiceIntegrationTest'`
Expected: 거래방 생성과 완료 API 부재로 FAIL.

- [ ] **Step 4: 서비스 구현**

`MatchingService`의 전원 수락 블록에서 `tradeRoomService.createIfAbsent(candidate)`를 호출한다. `TradeRoomService.complete`는 구성원 확인 후 멱등 완료 처리하고, 전원 완료 시 후보·게시글을 완료하며 `EXCHANGE_COMPLETED` 알림을 참여자별로 생성한다.

- [ ] **Step 5: 통합 테스트 통과 확인**

Run: `cd backend; .\gradlew.bat test --tests '*TradeRoomServiceIntegrationTest' --tests '*MatchingServiceIntegrationTest'`
Expected: PASS.

### Task 3: 거래방 REST API와 읽지 않은 수

**Files:**
- Create: `backend/src/main/java/com/valueswap/trade/ChatMessageRepository.java`
- Create: `backend/src/main/java/com/valueswap/trade/TradeRoomController.java`
- Create: `backend/src/main/java/com/valueswap/trade/dto/TradeRoomSummaryResponse.java`
- Create: `backend/src/main/java/com/valueswap/trade/dto/TradeRoomDetailResponse.java`
- Create: `backend/src/main/java/com/valueswap/trade/dto/ChatMessageResponse.java`
- Create: `backend/src/main/java/com/valueswap/trade/dto/ChatMessagePageResponse.java`
- Create: `backend/src/test/java/com/valueswap/trade/TradeRoomControllerTest.java`

- [ ] **Step 1: API 실패 테스트 작성**

```java
mvc.perform(get("/api/trade-rooms").header("Authorization", bearer(token)))
   .andExpect(status().isOk())
   .andExpect(jsonPath("$[0].unreadCount").value(2));
mvc.perform(get("/api/trade-rooms/{id}/messages", roomId).param("afterId", "10")
   .header("Authorization", bearer(token))).andExpect(status().isOk());
mvc.perform(patch("/api/trade-rooms/{id}/read", roomId).header("Authorization", bearer(token)))
   .andExpect(status().isOk());
```

- [ ] **Step 2: 비참여자 403 실패 테스트 작성**

```java
mvc.perform(get("/api/trade-rooms/{id}", roomId).header("Authorization", bearer(strangerToken)))
   .andExpect(status().isForbidden());
```

- [ ] **Step 3: 실패 확인**

Run: `cd backend; .\gradlew.bat test --tests com.valueswap.trade.TradeRoomControllerTest`
Expected: 엔드포인트 부재로 FAIL.

- [ ] **Step 4: REST 구현**

```text
GET   /api/trade-rooms
GET   /api/trade-rooms/{roomId}
GET   /api/trade-rooms/{roomId}/messages?beforeId=&afterId=&size=50
PATCH /api/trade-rooms/{roomId}/read
POST  /api/trade-rooms/{roomId}/complete
```

모든 조회는 구성원 조건을 쿼리에 포함한다. 안 읽은 수는 `lastReadMessageId`보다 ID가 큰 다른 사용자의 메시지 수이며, 읽음 처리는 현재 방의 최신 메시지 ID까지만 기록한다.

- [ ] **Step 5: 컨트롤러 테스트 통과 확인**

Run: `cd backend; .\gradlew.bat test --tests com.valueswap.trade.TradeRoomControllerTest`
Expected: PASS.

### Task 4: JWT WebSocket과 STOMP 메시지

**Files:**
- Create: `backend/src/main/java/com/valueswap/config/WebSocketConfig.java`
- Create: `backend/src/main/java/com/valueswap/trade/TradeRoomStompInterceptor.java`
- Create: `backend/src/main/java/com/valueswap/trade/TradeRoomMessageController.java`
- Create: `backend/src/main/java/com/valueswap/trade/dto/ChatMessageRequest.java`
- Create: `backend/src/test/java/com/valueswap/trade/TradeRoomWebSocketIntegrationTest.java`

- [ ] **Step 1: 저장·전달 실패 테스트 작성**

```java
stompSession.subscribe("/topic/trade-rooms/" + roomId, handler);
stompSession.send("/app/trade-rooms/" + roomId + "/messages",
    new ChatMessageRequest(clientMessageId, "안녕하세요"));
assertThat(received.get(5, SECONDS).content()).isEqualTo("안녕하세요");
assertThat(chatMessageRepository.count()).isEqualTo(1);
```

- [ ] **Step 2: 인증·권한·중복 전송 실패 테스트 작성**

유효하지 않은 JWT 연결, 비참여자 구독·발행, 완료 방 발행이 거부되는지 검증하고 같은 `clientMessageId` 재전송 시 메시지 수가 증가하지 않는지 확인한다.

- [ ] **Step 3: 실패 확인**

Run: `cd backend; .\gradlew.bat test --tests com.valueswap.trade.TradeRoomWebSocketIntegrationTest`
Expected: STOMP 엔드포인트 부재로 FAIL.

- [ ] **Step 4: WebSocket 구현**

`/ws`, 애플리케이션 prefix `/app`, 단순 브로커 `/topic`을 설정한다. 인바운드 인터셉터는 `CONNECT`의 `Authorization: Bearer ...`를 `JwtTokenProvider`로 파싱해 Principal을 넣고 `SUBSCRIBE`와 `SEND` 목적지의 roomId를 추출해 구성원 여부를 검사한다. 메시지 컨트롤러는 저장된 `ChatMessageResponse`를 `/topic/trade-rooms/{roomId}`로 반환한다.

- [ ] **Step 5: WebSocket 테스트 통과 확인**

Run: `cd backend; .\gradlew.bat test --tests com.valueswap.trade.TradeRoomWebSocketIntegrationTest`
Expected: PASS.

### Task 5: 프론트 공용 채팅 상태와 포맷

**Files:**
- Modify: `frontend/package.json`
- Modify: `frontend/package-lock.json`
- Create: `frontend/src/api/tradeRooms.js`
- Create: `frontend/src/trades/tradeFormat.js`
- Create: `frontend/src/trades/tradeFormat.test.js`
- Create: `frontend/src/trades/TradeRoomProvider.jsx`
- Create: `frontend/src/trades/TradeRoomProvider.test.jsx`

- [ ] **Step 1: 포맷 실패 테스트 작성**

```javascript
expect(formatUnreadCount(99)).toBe('99')
expect(formatUnreadCount(100)).toBe('99+')
expect(formatMessageTime(today, now)).toBe('오후 2:35')
expect(formatMessageTime(thisYear, now)).toBe('7월 6일 오후 2:35')
expect(formatMessageTime(lastYear, now)).toBe('2025년 12월 31일 오후 11:20')
```

- [ ] **Step 2: Provider 실패 테스트 작성**

목록 응답의 안 읽은 수 합계를 제공하고, 새 STOMP 메시지를 받은 방의 최근 메시지·시각·안 읽은 수를 갱신하며, 연결 종료 후 재연결 상태를 노출하는지 검증한다.

- [ ] **Step 3: 실패 확인**

Run: `cd frontend; npm.cmd test -- --run src/trades`
Expected: 모듈 부재로 FAIL.

- [ ] **Step 4: 의존성과 최소 구현 추가**

Run: `cd frontend; npm.cmd install @stomp/stompjs`

`TradeRoomProvider`는 로그인 시 방 목록을 조회하고 STOMP 클라이언트를 활성화한다. `reconnectDelay`는 1000ms에서 시작해 최대 30000ms로 제한하며, 화면에서 열린 방은 마지막 수신 ID 이후를 REST로 복구한다.

- [ ] **Step 5: 공용 상태 테스트 통과 확인**

Run: `cd frontend; npm.cmd test -- --run src/trades`
Expected: PASS.

### Task 6: 대화 메뉴·목록·거래방 화면

**Files:**
- Modify: `frontend/src/App.jsx`
- Modify: `frontend/src/components/AppShell.jsx`
- Create: `frontend/src/pages/TradeRoomsPage.jsx`
- Create: `frontend/src/pages/TradeRoomsPage.test.jsx`
- Create: `frontend/src/pages/TradeRoomPage.jsx`
- Create: `frontend/src/pages/TradeRoomPage.test.jsx`
- Modify: `frontend/src/pages/MatchDetailPage.jsx`
- Modify: `frontend/src/styles.css`

- [ ] **Step 1: 대화 메뉴와 목록 실패 테스트 작성**

```javascript
expect(screen.getByRole('link', { name: /대화/ })).toHaveTextContent('99+')
expect((await screen.findAllByTestId('trade-room-card'))[0]).toHaveTextContent('최근 메시지')
```

- [ ] **Step 2: 거래방 화면 실패 테스트 작성**

메시지별 발신자·본문·전송 시각, 전송 후 임시 메시지 치환, 새 메시지 수신, 읽음 처리, 완료 버튼, 완료 방 입력창 미표시를 검증한다.

- [ ] **Step 3: 실패 확인**

Run: `cd frontend; npm.cmd test -- --run src/pages/TradeRoomsPage.test.jsx src/pages/TradeRoomPage.test.jsx`
Expected: 화면과 라우트 부재로 FAIL.

- [ ] **Step 4: 화면 구현**

`/trades`와 `/trades/:roomId` 보호 라우트를 추가한다. 상단 `대화` 메뉴에는 `formatUnreadCount(totalUnread)` 배지를 붙인다. 목록은 최근 활동순 카드를 렌더링한다. 거래방은 과거 메시지를 불러오고 STOMP를 구독하며 전송 시 UUID 임시 ID를 사용한다. 상세 화면의 수락 완료 상태에는 생성된 거래방으로 이동하는 버튼을 표시한다.

- [ ] **Step 5: 화면 테스트 통과 확인**

Run: `cd frontend; npm.cmd test -- --run src/pages/TradeRoomsPage.test.jsx src/pages/TradeRoomPage.test.jsx`
Expected: PASS.

### Task 7: 문서와 전체 검증

**Files:**
- Modify: `README.md`
- Modify: `docs/api.md`

- [ ] **Step 1: 문서 갱신**

REST 엔드포인트, STOMP 연결·구독·발행 경로, JWT 헤더, 메시지 제한, 99+ 배지, 거래 완료 상태를 기록한다.

- [ ] **Step 2: 전체 백엔드 검증**

Run: `cd backend; .\gradlew.bat test`
Expected: 모든 테스트 PASS.

- [ ] **Step 3: 전체 프론트 검증**

Run: `cd frontend; npm.cmd test -- --run; npm.cmd run build`
Expected: 모든 테스트 PASS, Vite build 성공.

- [ ] **Step 4: 정적·비밀값 검증**

Run: `git diff --check; git check-ignore -v .env`
Expected: 공백 오류 없음, `.env`가 `.gitignore`로 제외됨.

- [ ] **Step 5: 사용자 QA 후 커밋·푸시**

여러 계정으로 실시간 송수신과 전원 완료를 확인한 뒤 관련 파일만 스테이징하고 한국어 커밋 메시지로 커밋한다. `ValueSwapApplication.java`의 기존 빈 줄 변경은 포함하지 않는다.
