# ValueSwap API

기본 경로는 `/api`이며 JSON을 사용합니다. 인증이 필요한 요청은 다음 헤더를 포함합니다.

```http
Authorization: Bearer <accessToken>
```

## 인증

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---:|---|
| POST | `/auth/signup` | 아니요 | 회원가입 |
| POST | `/auth/login` | 아니요 | JWT 발급 |
| GET | `/auth/me` | 예 | 내 정보 조회 |

회원가입 요청:

```json
{
  "email": "user@example.com",
  "password": "Password1!",
  "name": "홍길동",
  "nickname": "교환왕"
}
```

로그인 요청과 응답:

```json
{
  "email": "user@example.com",
  "password": "Password1!"
}
```

```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer"
}
```

## 교환 글

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---:|---|
| GET | `/posts` | 아니요 | 활성 글 목록 |
| GET | `/posts/{postId}` | 아니요 | 글 상세 |
| GET | `/posts/my` | 예 | 내 글 목록 |
| POST | `/posts` | 예 | 글 등록 |
| PUT | `/posts/{postId}` | 작성자 | 활성 글 수정 |
| DELETE | `/posts/{postId}` | 작성자 | 글 취소(soft delete) |

등록·수정 요청은 같은 구조를 사용합니다.

```json
{
  "title": "식사권과 농산물을 교환해요",
  "description": "마포구 직거래를 원합니다.",
  "region": "서울 마포구",
  "provideItems": [
    {
      "category": "COUPON",
      "subCategory": "식사권",
      "name": "돈까스 식사권",
      "description": "1인 식사권",
      "quantity": 1,
      "estimatedValue": 12000,
      "valuePolicy": "DIRECT",
      "tags": ["돈까스", "점심", "마포"]
    }
  ],
  "wantItems": [
    {
      "category": "FOOD_MATERIAL",
      "subCategory": "농산물",
      "name": "감자",
      "description": "상태 좋은 감자",
      "quantity": 1,
      "minValue": 8000,
      "maxValue": 15000,
      "valuePolicy": "DIRECT",
      "tags": ["감자", "농산물"]
    }
  ]
}
```

카테고리 값은 `FOOD`, `FOOD_MATERIAL`, `DAILY_GOODS`, `ELECTRONICS`, `COUPON`, `SERVICE`, `TALENT`, `DESIGN`, `BEAUTY`, `LESSON`, `ETC`입니다. `valuePolicy`는 `DIRECT`, `NEGOTIABLE`, `OFFER_REQUESTED`이며 직접 입력이 아니면 금액은 `null`일 수 있습니다. 세부 카테고리도 선택 사항입니다. 글 상태는 `ACTIVE`, `IN_EXCHANGE`, `COMPLETED`, `CANCELLED`입니다.

## 매칭

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---:|---|
| POST | `/matches/run` | ADMIN | 전체 활성 글을 다시 계산 |
| GET | `/matches/my` | 예 | 내가 포함된 후보 목록 |
| GET | `/matches/{matchId}` | 참여자 | 후보 상세 |
| POST | `/matches/{matchId}/accept` | 참여자 | 참여 수락 |
| POST | `/matches/{matchId}/reject` | 참여자 | 참여 거절 |

수동 실행 응답:

```json
{
  "scannedPosts": 5,
  "discovered": 2,
  "created": 2
}
```

후보 목록은 서버에서 `score` 내림차순, 동점이면 `createdAt` 내림차순입니다. 프론트엔드도 같은 정렬을 적용합니다. `edges`의 `fromNickname → toNickname`은 희망 글에서 그 희망을 충족하는 제공 글로 향하는 탐색 방향이고, `orderIndex`가 순환 순서입니다. 실제 물품 전달 방향은 그 반대입니다.

`score`는 카테고리·이름·태그·가치·지역·신뢰도 원점수를 평가 가능한 최대점으로 나누어 반올림한 0~100 정수입니다. 양쪽 세부 카테고리가 없거나 가치가 직접 입력되지 않은 경우 해당 항목은 분모에서 제외합니다. 화면 등급은 85~100 매우 높음, 70~84 높음, 55~69 보통, 40~54 낮음, 0~39 매우 낮음입니다.

```json
{
  "id": 12,
  "matchType": "THREE_PARTY",
  "status": "PROPOSED",
  "score": 88,
  "createdAt": "2026-06-30T12:00:00",
  "participants": [
    { "userId": 2, "nickname": "식당", "postId": 3, "postTitle": "식사권", "orderIndex": 0, "acceptStatus": "PENDING" }
  ],
  "edges": [
    { "fromNickname": "식당", "toNickname": "디자이너", "provideItemName": "식사권", "wantItemName": "식사권", "score": 88, "orderIndex": 0 }
  ]
}
```

`referenceId`가 있는 알림은 해당 매칭 후보 ID를 의미하며, 알림 카드 선택 시 읽음 처리 후 `/matches/{referenceId}`로 이동합니다. 과거에 생성되어 `referenceId`가 없는 알림은 상세 이동을 제공하지 않습니다.

상세 응답은 위 객체를 `{ "match": ... }`로 감쌉니다. 동일한 순환 경로는 `cycleKey`로 중복 저장되지 않으며 알림도 사용자·후보·유형 조합당 한 번만 생성됩니다. 전원 수락 시 후보 상태는 `ACCEPTED`, 관련 글은 `IN_EXCHANGE`가 됩니다.

## 알림

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---:|---|
| GET | `/notifications` | 예 | 내 알림 목록 |
| PATCH | `/notifications/{notificationId}/read` | 소유자 | 읽음 처리 |

```json
{
  "id": 21,
  "title": "새로운 교환 후보",
  "message": "3자 교환 경로가 발견되었습니다.",
  "type": "MATCH_FOUND",
  "read": false,
  "referenceId": 12,
  "createdAt": "2026-06-30T12:00:00"
}
```

## 오류

모든 오류는 같은 형식을 사용합니다. 입력 검증 오류일 때만 `fieldErrors`에 필드별 메시지가 들어갑니다.

```json
{
  "timestamp": "2026-06-30T03:00:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "요청 값이 올바르지 않습니다.",
  "path": "/api/posts",
  "fieldErrors": {
    "title": "비어 있을 수 없습니다"
  }
}
```

| 상태 | 대표 상황 |
|---:|---|
| 400 | 형식·필수값·가치 범위 오류 |
| 401 | 토큰 없음/만료, 로그인 실패 |
| 403 | 다른 사용자의 글·알림·후보 접근, 관리자 권한 없음 |
| 404 | 사용자·글·후보·알림을 찾지 못함 |
| 409 | 이메일 중복, 진행 중인 글 수정, 이미 취소된 글 |
