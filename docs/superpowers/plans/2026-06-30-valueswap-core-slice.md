# ValueSwap 핵심 수직 슬라이스 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** JWT 인증, 교환 글 CRUD, 규칙 기반 점수 계산, 2·3·4자 순환 매칭, 알림과 React 화면이 실제로 연결된 첫 번째 ValueSwap MVP 수직 슬라이스를 완성한다.

**Architecture:** `backend` Spring Boot 모듈형 모놀리스와 `frontend` React SPA를 한 저장소에서 운영한다. 백엔드는 H2 테스트와 MySQL 운영 profile을 분리하고, 매칭은 `ItemMatchScorer` → 방향 그래프 → `CycleFinder` → 후보·알림 저장 순서로 실행한다. 모든 기능은 실패 테스트부터 작성하고 작은 논리 단위로 커밋한다.

**Tech Stack:** Java 17, Gradle 8.x Wrapper, Spring Boot 3.5.15, Spring Web/Security/Data JPA/Validation, JJWT, Flyway, MySQL 8.4, H2, JUnit 5, React 19.2, Vite 8, JavaScript, Axios, React Router, Vitest, Testing Library, Docker Compose

---

## 파일 구조

```text
ValueSwap/
├─ .gitignore                     # 환경변수·키·빌드 산출물 제외
├─ .env.example                   # 안전한 변수 이름과 개발 예시
├─ compose.yml                    # MySQL 개발 컨테이너
├─ README.md
├─ docs/api.md
├─ backend/
│  ├─ build.gradle, settings.gradle, gradlew, gradlew.bat, gradle/wrapper/*
│  └─ src/{main,test}/java/com/valueswap/
│     ├─ auth/                    # JWT 인증 API
│     ├─ user/                    # User와 역할
│     ├─ post/                    # 글·제공·희망 항목 CRUD
│     ├─ matching/                # 점수, 그래프, 순환, 후보 저장
│     ├─ notification/            # 알림 조회·읽음
│     ├─ common/                  # 예외와 공통 응답
│     └─ config/                  # Security, seed, scheduler
└─ frontend/
   ├─ package.json, vite.config.js
   └─ src/
      ├─ api/                     # Axios 인스턴스와 API 함수
      ├─ auth/                    # 인증 상태와 보호 경로
      ├─ components/              # AppShell, ItemCard, CycleFlow
      └─ pages/                   # 인증·글·매칭·알림 화면
```

## Task 1: 저장소 안전장치와 실행 골격

**Files:**
- Create: `.gitignore`
- Create: `.env.example`
- Create: `compose.yml`
- Create: `backend/build.gradle`
- Create: `backend/settings.gradle`
- Create: `backend/gradlew`, `backend/gradlew.bat`, `backend/gradle/wrapper/*`
- Create: `backend/src/main/java/com/valueswap/ValueSwapApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/resources/db/migration/V1__baseline.sql`
- Create: `backend/src/test/resources/application-test.yml`
- Create: `backend/src/test/java/com/valueswap/ValueSwapApplicationTest.java`

- [ ] **Step 1: 비밀정보 추적 방지 규칙 작성**

`.gitignore`에 다음 내용을 그대로 추가한다.

```gitignore
.env
.env.*
!.env.example
**/application-local.yml
**/application-secret.yml
*.pem
*.key
*.p12
.idea/
.vscode/
backend/build/
frontend/node_modules/
frontend/dist/
```

`.env.example`에는 실제 비밀값 없이 아래 변수만 둔다.

```dotenv
MYSQL_DATABASE=valueswap
MYSQL_USER=valueswap
MYSQL_PASSWORD=change-me-locally
MYSQL_ROOT_PASSWORD=change-root-locally
JWT_SECRET=replace-with-at-least-32-byte-local-secret
```

- [ ] **Step 2: 추적 대상 비밀정보 검사**

Run: `git ls-files | Select-String -Pattern '^\.env$|application-(local|secret)\.yml$|\.(pem|key|p12)$'`

Expected: 출력 없음.

- [ ] **Step 3: Spring Initializr로 Gradle Wrapper 포함 프로젝트 생성**

Spring Initializr에서 Java 17, Gradle-Groovy, Spring Boot 3.5.15와 `web,security,data-jpa,validation,lombok,flyway,mysql,h2` 의존성을 선택해 `backend`에 생성한다. 생성된 Gradle Wrapper를 함께 추적하고 `build.gradle`의 `dependencies` 블록에 다음 의존성을 추가한다.

```groovy
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
testImplementation 'org.springframework.security:spring-security-test'
```

- [ ] **Step 4: 컨텍스트 실패 테스트 작성**

```java
@SpringBootTest
@ActiveProfiles("test")
class ValueSwapApplicationTest {
    @Test void contextLoads() {}
}
```

- [ ] **Step 5: 테스트 profile과 운영 설정 작성**

`application.yml`은 DB/JWT 값을 환경변수로만 읽고, `application-test.yml`은 H2와 고정 테스트 키를 사용한다. JPA `ddl-auto`는 `validate`, Flyway는 활성화하며 scheduler 기본 주기는 `300000`ms, 매칭 기준은 `60`으로 설정한다.

- [ ] **Step 6: 실패 후 통과 확인**

Run: `cd backend; .\gradlew.bat test`

Expected: 설정 전에는 DataSource 또는 migration 오류로 FAIL, 설정과 빈 baseline 설명을 가진 `V1__baseline.sql` 추가 후 `BUILD SUCCESS`.

- [ ] **Step 7: MySQL Compose 작성 및 구성 검증**

```yaml
services:
  mysql:
    image: mysql:8.4
    environment:
      MYSQL_DATABASE: ${MYSQL_DATABASE:-valueswap}
      MYSQL_USER: ${MYSQL_USER:-valueswap}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
    ports: ["3306:3306"]
    volumes: ["valueswap-mysql:/var/lib/mysql"]
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 5s
      timeout: 3s
      retries: 20
volumes:
  valueswap-mysql:
```

Run: `docker compose --env-file .env.example config`

Expected: 유효한 Compose YAML 출력.

- [ ] **Step 8: 커밋**

```bash
git add .gitignore .env.example compose.yml backend
git commit -m "chore: 백엔드 실행 골격과 비밀정보 보호 설정"
```

## Task 2: 사용자와 JWT 인증

**Files:**
- Create: `backend/src/main/java/com/valueswap/user/{User,UserRole,UserRepository}.java`
- Create: `backend/src/main/java/com/valueswap/auth/{AuthController,AuthService,JwtTokenProvider,JwtAuthenticationFilter}.java`
- Create: `backend/src/main/java/com/valueswap/auth/dto/{SignupRequest,LoginRequest,AuthResponse,MeResponse}.java`
- Create: `backend/src/main/java/com/valueswap/config/SecurityConfig.java`
- Create: `backend/src/main/java/com/valueswap/common/exception/{ApiException,GlobalExceptionHandler}.java`
- Create: `backend/src/main/resources/db/migration/V2__users.sql`
- Test: `backend/src/test/java/com/valueswap/auth/AuthControllerTest.java`

- [ ] **Step 1: 인증 API 실패 테스트 작성**

`AuthControllerTest`에 다음 사례를 MockMvc로 작성한다.

```java
@Test void signupLoginAndMe() throws Exception {
    mvc.perform(post("/api/auth/signup").contentType(APPLICATION_JSON)
        .content("""{"email":"user@test.com","password":"Password1!","name":"사용자","nickname":"교환왕"}"""))
        .andExpect(status().isCreated());
    String token = loginAndGetToken("user@test.com", "Password1!");
    mvc.perform(get("/api/auth/me").header(AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk()).andExpect(jsonPath("$.email").value("user@test.com"));
}
```

중복 이메일 409, 잘못된 비밀번호 401, 토큰 없는 `/me` 401, `@Email`·비밀번호 길이 검증 400도 각각 테스트한다.

- [ ] **Step 2: 실패 확인**

Run: `cd backend; .\gradlew.bat test --tests "*AuthControllerTest"`

Expected: `/api/auth/signup` 매핑 부재로 FAIL.

- [ ] **Step 3: 인증 계약 구현**

DTO 계약은 다음과 같이 고정한다.

```java
public record SignupRequest(@Email @NotBlank String email,
    @Size(min=8,max=72) String password, @NotBlank String name, @NotBlank String nickname) {}
public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
public record AuthResponse(String accessToken, String tokenType) {}
public record MeResponse(Long id, String email, String name, String nickname,
    UserRole role, BigDecimal trustScore) {}
```

`AuthService`는 이메일 소문자 정규화, BCrypt 암호화, 초기 role `USER`, trustScore `50.00` 저장을 담당한다. `JwtTokenProvider`는 userId와 role claim을 HS256으로 서명하고, filter는 유효한 Bearer token을 `SecurityContext`에 넣는다.

- [ ] **Step 4: Security 규칙 구현**

`/api/auth/signup`, `/api/auth/login`, `GET /api/posts/**`만 공개하고 나머지는 인증한다. `/api/admin/**`, `POST /api/matches/run`은 `ADMIN`만 허용한다. 세션은 `STATELESS`, CSRF는 비활성화한다.

- [ ] **Step 5: 전체 인증 테스트 통과 확인**

Run: `cd backend; .\gradlew.bat test --tests "*AuthControllerTest"`

Expected: 모든 인증 사례 PASS.

- [ ] **Step 6: 커밋**

```bash
git add backend/src/main backend/src/test
git commit -m "feat: JWT 회원가입과 로그인 구현"
```

## Task 3: 교환 글과 항목 CRUD

**Files:**
- Create: `backend/src/main/java/com/valueswap/post/domain/{ExchangePost,PostStatus,ProvideItem,WantItem,Category}.java`
- Create: `backend/src/main/java/com/valueswap/post/{ExchangePostRepository,ExchangePostService,ExchangePostController}.java`
- Create: `backend/src/main/java/com/valueswap/post/dto/{PostCreateRequest,ProvideItemRequest,WantItemRequest,PostResponse,PostSummaryResponse}.java`
- Create: `backend/src/main/resources/db/migration/V3__exchange_posts.sql`
- Test: `backend/src/test/java/com/valueswap/post/ExchangePostControllerTest.java`
- Test support: `backend/src/test/java/com/valueswap/support/{TestPosts,TestSecurity}.java`

- [ ] **Step 1: CRUD 실패 테스트 작성**

테스트는 인증 사용자의 다중 제공·희망 항목 등록, 공개 목록·상세 조회, 작성자 수정, 작성자 취소, 타 사용자 수정·취소 403, `IN_EXCHANGE` 수정 409를 검증한다. 등록 응답은 `ACTIVE`, 입력 항목 수와 태그를 그대로 반환해야 한다.

```java
@Test void ownerCreatesPostWithStructuredItems() throws Exception {
    mvc.perform(post("/api/posts").with(jwtUser(ownerId)).contentType(APPLICATION_JSON)
        .content(TestPosts.restaurantJson()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.provideItems[0].name").value("돈까스 정식 식사권"))
        .andExpect(jsonPath("$.wantItems[0].name").value("쌀"));
}
```

- [ ] **Step 2: 실패 확인**

Run: `cd backend; .\gradlew.bat test --tests "*ExchangePostControllerTest"`

Expected: post 타입과 endpoint 부재로 컴파일 또는 404 FAIL.

- [ ] **Step 3: Entity와 migration 구현**

`ExchangePost`는 owner, title, description, status, region, timestamps를 가진다. `ProvideItem`은 category, subCategory, name, description, quantity, estimatedValue, tags를, `WantItem`은 minValue/maxValue를 추가로 가진다. 자식은 cascade·orphanRemoval로 글 aggregate 안에서만 변경한다. 모든 FK, status, created_at 및 `cycle_key` 조회에 필요한 index를 migration에 명시한다.

`TestPosts.restaurantJson()`은 설계 명세의 돈까스 식사권·쌀 요청 JSON을 그대로 반환한다. `TestSecurity.jwtUser(id)`는 `userId`, `role=USER` claim을 가진 인증 요청 후처리기를 반환해 Controller 테스트의 인증 표현을 한 곳으로 모은다.

- [ ] **Step 4: 요청 검증과 Service 구현**

제목 1~100자, 설명 최대 2000자, 지역 필수, 제공·희망 목록 각각 최소 1개, quantity 1 이상, 금액 0 이상, minValue ≤ maxValue를 검증한다. update는 기존 자식을 안전하게 교체하고 delete는 `CANCELED`로 전환한다.

- [ ] **Step 5: CRUD 테스트 통과 확인**

Run: `cd backend; .\gradlew.bat test --tests "*ExchangePostControllerTest"`

Expected: PASS.

- [ ] **Step 6: 커밋**

```bash
git add backend/src/main backend/src/test
git commit -m "feat: 구조화된 교환 글 CRUD 구현"
```

## Task 4: 항목 매칭 점수 계산기

**Files:**
- Create: `backend/src/main/java/com/valueswap/matching/score/{ItemMatchScorer,ItemMatchResult,NameMatch,TextNormalizer}.java`
- Test: `backend/src/test/java/com/valueswap/matching/score/ItemMatchScorerTest.java`
- Test support: `backend/src/test/java/com/valueswap/support/MatchingFixtures.java`

- [ ] **Step 1: 모든 점수 규칙의 실패 테스트 작성**

각 규칙을 하나씩 격리해 category 25, subCategory 25, 정확한 이름 30, 양방향 포함 20, 토큰 10, 태그 최대 20, 범위 안 20, 20% 근접 범위 5, 지역 10, 신뢰도 0~10을 검증한다. 추가로 햄버거 부분 매칭은 eligible, category만 같은 음식은 ineligible이어야 한다.

```java
@Test void partialBurgerNameIsEligible() {
    var result = scorer.score(provide("FOOD","햄버거","불고기 햄버거 세트",30000,"햄버거","버거"),
        want("FOOD","햄버거","햄버거",20000,40000,"햄버거","버거"), "광주", "광주", bd("50"));
    assertThat(result.eligible()).isTrue();
    assertThat(result.score()).isGreaterThanOrEqualTo(60);
}
@Test void categoryOnlyNeverCreatesEdge() {
    assertThat(scorer.score(provide("FOOD","치킨","후라이드",20000),
        want("FOOD","한식","비빔밥",15000,25000), "광주", "서울", bd("50")).eligible()).isFalse();
}
```

- [ ] **Step 2: 실패 확인**

Run: `cd backend; .\gradlew.bat test --tests "*ItemMatchScorerTest"`

Expected: scorer 타입 부재로 FAIL.

- [ ] **Step 3: 정규화와 점수 계산 구현**

```java
public record ItemMatchResult(int score, boolean eligible) {}

public ItemMatchResult score(ProvideItem provide, WantItem want,
        String provideRegion, String wantRegion, BigDecimal trustScore) {
    boolean category = provide.getCategory() == want.getCategory();
    boolean sub = normalizer.same(provide.getSubCategory(), want.getSubCategory());
    NameMatch name = normalizer.compareNames(provide.getName(), want.getName());
    int tagCount = normalizer.overlap(provide.getTags(), want.getTags());
    int score = (category ? 25 : 0) + (sub ? 25 : 0) + name.points()
        + Math.min(tagCount * 5, 20) + valuePoints(provide.getEstimatedValue(), want)
        + (normalizer.same(provideRegion, wantRegion) ? 10 : 0)
        + trustScore.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100))
            .divide(BigDecimal.TEN, RoundingMode.DOWN).intValue();
    boolean related = sub || name.related() || tagCount > 0;
    return new ItemMatchResult(score, category && related && score >= threshold);
}
```

이름 점수는 완전 일치가 있으면 30만 부여하고 포함 또는 토큰 점수를 중복 가산하지 않는다. 포함은 20, 그 외 토큰 일치는 10이다.

- [ ] **Step 4: 점수 테스트 통과 확인**

Run: `cd backend; .\gradlew.bat test --tests "*ItemMatchScorerTest"`

Expected: PASS.

- [ ] **Step 5: 커밋**

```bash
git add backend/src/main/java/com/valueswap/matching/score backend/src/test/java/com/valueswap/matching/score
git commit -m "feat: 규칙 기반 항목 매칭 점수 계산"
```

## Task 5: 방향 그래프와 2·3·4자 순환 탐색

**Files:**
- Create: `backend/src/main/java/com/valueswap/matching/graph/{MatchGraph,MatchGraphEdge,MatchCycle,MatchGraphBuilder,CycleFinder}.java`
- Create: `backend/src/main/java/com/valueswap/matching/domain/MatchType.java`
- Test: `backend/src/test/java/com/valueswap/matching/graph/{MatchGraphBuilderTest,CycleFinderTest}.java`

- [ ] **Step 1: 그래프와 순환 실패 테스트 작성**

2자 A-B-A, 3자 A-B-C-A, 4자 A-B-C-D-A를 각각 찾고 비순환 경로, 점수 미달 간선, 동일 사용자 반복은 찾지 않아야 한다. A-B-C, B-C-A, C-A-B는 하나의 canonical key만 반환해야 한다.

```java
@Test void findsOneCanonicalThreePartyCycle() {
    MatchGraph graph = graph(edge(1,2,90), edge(2,3,80), edge(3,1,70));
    List<MatchCycle> cycles = finder.find(graph);
    assertThat(cycles).singleElement().satisfies(c -> {
        assertThat(c.type()).isEqualTo(MatchType.THREE_PARTY);
        assertThat(c.cycleKey()).isEqualTo("THREE_PARTY:1-2-3");
        assertThat(c.averageScore()).isEqualTo(80);
    });
}
```

- [ ] **Step 2: 실패 확인**

Run: `cd backend; .\gradlew.bat test --tests "*MatchGraphBuilderTest" --tests "*CycleFinderTest"`

Expected: graph 타입 부재로 FAIL.

- [ ] **Step 3: 그래프 생성 구현**

`MatchGraphBuilder`는 ACTIVE 글만 받아 순서가 있는 모든 서로 다른 게시글 쌍을 비교한다. 같은 owner는 제외하고 모든 희망·제공 항목 조합 중 eligible 점수가 가장 높은 한 쌍만 `MatchGraphEdge`로 만든다.

- [ ] **Step 4: 제한 DFS 구현**

`CycleFinder.find`는 각 노드를 시작점으로 깊이 4까지만 탐색한다. 경로의 postId와 ownerId 집합을 각각 관리하고 시작점으로 닫히는 길이 2~4 경로만 수집한다. `MatchType + ':' + 정렬한 postId`를 key로 한 `LinkedHashMap`으로 회전 중복을 제거한다.

- [ ] **Step 5: 탐색 테스트 통과 확인**

Run: `cd backend; .\gradlew.bat test --tests "*MatchGraphBuilderTest" --tests "*CycleFinderTest"`

Expected: PASS.

- [ ] **Step 6: 커밋**

```bash
git add backend/src/main/java/com/valueswap/matching/graph backend/src/test/java/com/valueswap/matching/graph
git commit -m "feat: 최대 4자 교환 순환 탐색 구현"
```

## Task 6: 매칭 후보 저장, 가중치 정렬과 알림

**Files:**
- Create: `backend/src/main/java/com/valueswap/matching/domain/{MatchCandidate,MatchParticipant,MatchEdge,MatchStatus,AcceptStatus}.java`
- Create: `backend/src/main/java/com/valueswap/matching/{MatchCandidateRepository,MatchingService,MatchController}.java`
- Create: `backend/src/main/java/com/valueswap/matching/dto/{MatchRunResponse,MatchSummaryResponse,MatchDetailResponse}.java`
- Create: `backend/src/main/java/com/valueswap/notification/{Notification,NotificationType,NotificationRepository,NotificationService,NotificationController}.java`
- Create: `backend/src/main/resources/db/migration/V4__matches_and_notifications.sql`
- Test: `backend/src/test/java/com/valueswap/matching/MatchingServiceIntegrationTest.java`
- Test: `backend/src/test/java/com/valueswap/notification/NotificationControllerTest.java`

- [ ] **Step 1: 저장·정렬·중복 알림 실패 테스트 작성**

초기 3자 순환을 실행하면 후보 하나와 알림 세 건이 저장되어야 한다. 같은 데이터로 재실행하면 후보와 알림 수가 늘지 않아야 한다. `/api/matches/my`는 평균 점수 내림차순, 동점이면 createdAt 내림차순이어야 한다.

```java
@Test void runIsIdempotentAndNotifiesEveryParticipantOnce() {
    var first = service.runMatching();
    var second = service.runMatching();
    assertThat(first.created()).isEqualTo(1);
    assertThat(second.created()).isZero();
    assertThat(candidateRepository.count()).isEqualTo(1);
    assertThat(notificationRepository.count()).isEqualTo(3);
}
```

- [ ] **Step 2: 실패 확인**

Run: `cd backend; .\gradlew.bat test --tests "*MatchingServiceIntegrationTest" --tests "*NotificationControllerTest"`

Expected: persistence 타입과 API 부재로 FAIL.

- [ ] **Step 3: 후보 aggregate와 migration 구현**

`match_candidates.cycle_key`에 유일 제약을 둔다. participant는 user/post/orderIndex/acceptStatus, edge는 from/to user/post와 선택한 provide/want item 및 score를 저장한다. 후보 score는 간선 정수 평균이다.

- [ ] **Step 4: 트랜잭션 실행 Service 구현**

`MatchingService.runMatching()`은 ACTIVE 글을 fetch join으로 읽고 그래프·순환을 만든다. 기존 cycleKey 집합을 한 번에 조회하고 새 후보만 저장한 직후 참여자별 `MATCH_FOUND` 알림을 만든다. 유일 제약 충돌은 다른 실행이 먼저 저장한 것으로 보고 해당 순환만 건너뛴다.

- [ ] **Step 5: API 구현**

`POST /api/matches/run`은 ADMIN 전용이며 scannedPosts, discovered, created 수를 반환한다. `GET /api/matches/my`와 `GET /api/matches/{id}`는 참여자만 접근할 수 있다. 알림은 `GET /api/notifications`, `PATCH /api/notifications/{id}/read`를 제공하며 본인 알림만 읽을 수 있다.

- [ ] **Step 6: 통합 테스트 통과 확인**

Run: `cd backend; .\gradlew.bat test --tests "*MatchingServiceIntegrationTest" --tests "*NotificationControllerTest"`

Expected: PASS.

- [ ] **Step 7: 커밋**

```bash
git add backend/src/main backend/src/test
git commit -m "feat: 매칭 후보와 중복 없는 알림 저장"
```

## Task 7: 초기 데이터와 5분 자동 탐색

**Files:**
- Create: `backend/src/main/java/com/valueswap/config/SeedDataInitializer.java`
- Create: `backend/src/main/java/com/valueswap/matching/MatchingScheduler.java`
- Test: `backend/src/test/java/com/valueswap/config/SeedMatchingTest.java`

- [ ] **Step 1: seed 매칭 실패 테스트 작성**

local seed 생성 로직을 테스트 profile에서 명시적으로 호출한 후 수동 매칭을 실행한다. `THREE_PARTY` 1건 이상과 `ONE_TO_ONE` 1건 이상, 햄버거 제공 이름 `불고기 햄버거 세트`가 선택된 edge를 검증한다.

- [ ] **Step 2: 실패 확인**

Run: `cd backend; .\gradlew.bat test --tests "*SeedMatchingTest"`

Expected: initializer 부재로 FAIL.

- [ ] **Step 3: 멱등 seed 구현**

`@Profile("local")` initializer는 `admin@valueswap.local`, 식당·농가·디자이너, 햄버거 가게·일반 사용자 계정을 이메일로 확인한 뒤 없을 때만 생성한다. 첨부 요구사항의 category, 이름, 태그와 금액을 그대로 저장한다.

- [ ] **Step 4: scheduler 구현**

```java
@Scheduled(fixedDelayString = "${valueswap.matching.interval-ms:300000}")
public void discoverMatches() { matchingService.runMatching(); }
```

`valueswap.matching.scheduler-enabled` 속성으로 조건부 활성화하고 test profile에서는 false로 둔다.

- [ ] **Step 5: seed 테스트 통과 확인**

Run: `cd backend; .\gradlew.bat test --tests "*SeedMatchingTest"`

Expected: PASS.

- [ ] **Step 6: 커밋**

```bash
git add backend/src/main backend/src/test
git commit -m "feat: 교환 시나리오 초기 데이터와 자동 매칭"
```

## Task 8: React 골격, 인증과 게시글 화면

**Files:**
- Create: `frontend/package.json`, `frontend/vite.config.js`, `frontend/index.html`
- Create: `frontend/src/{main.jsx,App.jsx,styles.css}`
- Create: `frontend/src/api/{client.js,auth.js,posts.js}.js`
- Create: `frontend/src/auth/{AuthProvider.jsx,ProtectedRoute.jsx}`
- Create: `frontend/src/components/{AppShell.jsx,ItemCard.jsx,PostCard.jsx}`
- Create: `frontend/src/pages/{LoginPage,SignupPage,HomePage,PostFormPage,PostDetailPage}.jsx`
- Create: `frontend/src/test/{setup.js,renderApp.jsx}`
- Test: `frontend/src/pages/LoginPage.test.jsx`
- Test: `frontend/src/pages/PostFormPage.test.jsx`

- [ ] **Step 1: Vite React 프로젝트와 테스트 도구 생성**

Run: `npm.cmd create vite@latest frontend -- --template react`

Run: `cd frontend; npm.cmd install; npm.cmd install axios react-router-dom; npm.cmd install -D vitest jsdom @testing-library/react @testing-library/jest-dom @testing-library/user-event`

Expected: 설치 성공 및 lockfile 생성.

`package.json`의 scripts에 `"test": "vitest"`를 추가하고 `vite.config.js`의 test 환경은 `jsdom`, setup file은 `./src/test/setup.js`로 지정한다. `renderApp(path)`는 `MemoryRouter`, `AuthProvider`를 포함해 지정 경로에서 화면을 렌더링한다.

- [ ] **Step 2: 로그인과 동적 항목 폼 실패 테스트 작성**

로그인 성공 시 token 저장과 `/` 이동, 401 오류 메시지, 제공·희망 항목 추가·삭제, 태그 쉼표 입력 변환, minValue > maxValue 검증을 Testing Library로 작성한다.

```jsx
it('제공 항목을 추가하고 구조화된 요청을 전송한다', async () => {
  renderApp('/posts/new');
  await user.click(screen.getByRole('button', {name: '제공 항목 추가'}));
  await user.type(screen.getByLabelText('제공 이름'), '돈까스 식사권');
  await user.click(screen.getByRole('button', {name: '교환 글 등록'}));
  expect(postsApi.create).toHaveBeenCalledWith(expect.objectContaining({
    provideItems: [expect.objectContaining({name: '돈까스 식사권'})]
  }));
});
```

- [ ] **Step 3: 실패 확인**

Run: `cd frontend; npm.cmd test -- --run`

Expected: 화면 모듈 부재로 FAIL.

- [ ] **Step 4: API와 인증 상태 구현**

Axios base URL은 `VITE_API_URL` 또는 `/api`를 사용한다. request interceptor가 localStorage token을 Bearer로 붙이고 response 401이면 token과 사용자 상태를 지운다. `AuthProvider`는 login/signup/me/logout을 제공한다.

- [ ] **Step 5: 게시글 화면 구현**

Home은 최근 글 카드, PostForm은 제공·희망 항목 배열, PostDetail은 작성자 신뢰도·상태·항목을 표시한다. 모든 입력에 label을 연결하고 submit 중 버튼 비활성화, API 오류와 빈 상태를 명시한다.

- [ ] **Step 6: 프론트 테스트와 build 통과 확인**

Run: `cd frontend; npm.cmd test -- --run; npm.cmd run build`

Expected: 모든 테스트 PASS, `dist` 생성.

- [ ] **Step 7: 커밋**

```bash
git add frontend
git commit -m "feat: 인증과 교환 글 React 화면 구현"
```

## Task 9: 매칭 흐름과 알림 화면

**Files:**
- Create: `frontend/src/api/{matches,notifications}.js`
- Create: `frontend/src/components/CycleFlow.jsx`
- Create: `frontend/src/pages/{MatchesPage,MatchDetailPage,NotificationsPage}.jsx`
- Test: `frontend/src/components/CycleFlow.test.jsx`
- Test: `frontend/src/pages/MatchesPage.test.jsx`

- [ ] **Step 1: 가중치 정렬과 방향 흐름 실패 테스트 작성**

API 응답 순서가 잘못되어도 MatchesPage가 score 내림차순, 동점 createdAt 내림차순으로 표시하는지 검증한다. CycleFlow는 각 edge의 `fromNickname → toNickname`, 제공 항목 이름과 점수를 표시해야 한다. 읽지 않은 알림은 강조되고 읽기 버튼이 PATCH API를 호출해야 한다.

- [ ] **Step 2: 실패 확인**

Run: `cd frontend; npm.cmd test -- --run src/components/CycleFlow.test.jsx src/pages/MatchesPage.test.jsx`

Expected: 컴포넌트 부재로 FAIL.

- [ ] **Step 3: 매칭 UI 구현**

```javascript
const ordered = [...matches].sort((a, b) =>
  b.score - a.score || new Date(b.createdAt) - new Date(a.createdAt));
```

2자는 왕복, 3·4자는 순환형 카드와 CSS 화살표로 렌더링한다. 모바일에서는 세로 흐름으로 바꾸고 각 edge 설명을 텍스트로도 제공해 접근성을 보장한다.

- [ ] **Step 4: 알림 UI 구현**

알림 목록은 unread 우선, 그 안에서 최신순으로 표시한다. 읽기 성공 후 해당 항목만 로컬 상태를 갱신하고 오류 시 원래 상태를 유지한다.

- [ ] **Step 5: 테스트와 build 통과 확인**

Run: `cd frontend; npm.cmd test -- --run; npm.cmd run build`

Expected: PASS.

- [ ] **Step 6: 커밋**

```bash
git add frontend/src
git commit -m "feat: 가중치 순 매칭 흐름과 알림 화면"
```

## Task 10: 문서와 전체 검증

**Files:**
- Create: `README.md`
- Create: `docs/api.md`
- Modify: `.env.example`
- Test: 전체 백엔드·프론트 테스트

- [ ] **Step 1: README 작성**

Java 17, Node 22, Docker 요구사항, `.env.example` 복사 방법, `docker compose up -d mysql`, `.\gradlew.bat bootRun --args="--spring.profiles.active=local"`, `npm.cmd run dev`, seed 계정, 수동 매칭 실행 순서를 기록한다. 실제 비밀번호나 운영 secret은 기록하지 않는다.

- [ ] **Step 2: API 문서 작성**

`docs/api.md`에 Auth, Posts, Matches, Notifications endpoint별 인증 요구사항, 요청·응답 예시, 400/401/403/404/409 오류 형식을 적는다.

- [ ] **Step 3: 백엔드 전체 테스트**

Run: `cd backend; .\gradlew.bat clean test`

Expected: `BUILD SUCCESS`, failures 0, errors 0.

- [ ] **Step 4: 프론트 전체 테스트와 production build**

Run: `cd frontend; npm.cmd test -- --run; npm.cmd run build`

Expected: 모든 Vitest PASS와 Vite build 성공.

- [ ] **Step 5: 비밀정보 최종 검사**

Run: `git grep -n -I -E '(JWT_SECRET=.{20,}|MYSQL_PASSWORD=[^$][^ {]|BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY|ghp_[A-Za-z0-9]+)' -- ':!docs/superpowers/**' ':!.env.example'`

Expected: 출력 없음. 출력이 있으면 커밋·푸시를 중단하고 값 제거 및 키 폐기 여부를 확인한다.

- [ ] **Step 6: 작업 트리와 Compose 검증**

Run: `docker compose --env-file .env.example config; git status --short`

Expected: Compose 유효, 문서 파일만 미커밋 상태.

- [ ] **Step 7: 문서 커밋**

```bash
git add README.md docs/api.md .env.example
git commit -m "docs: 핵심 교환 슬라이스 실행과 API 안내"
```

- [ ] **Step 8: 완료 기준 확인**

local profile에서 관리자 로그인 → 수동 매칭 실행 → 3자 및 1:1 후보 조회 → score 내림차순 화면 → 참여자 알림 조회까지 수행한다. 이 단계가 통과한 뒤에만 거래방·수락·QR·후기·관리자 기능의 두 번째 구현 계획을 작성한다.
