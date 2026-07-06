# ValueSwap

ValueSwap은 서로의 제공 항목과 희망 항목을 비교해 1:1부터 최대 4자까지 교환 경로를 찾는 MVP 웹 서비스입니다. 후보는 규칙 기반 가중치 점수 내림차순으로 표시되고, 새로운 경로가 발견되면 참여자별 알림이 한 번만 생성됩니다.

## 기술 구성

- 백엔드: Java 17, Spring Boot 3.5, Spring Security, JPA, Flyway, Gradle
- 프론트엔드: React 19, Vite 7, React Router, Axios, Vitest
- 데이터베이스: MySQL 8.4 (테스트는 H2)

## 로컬 실행

필수 도구는 Java 17, Node.js 22, Docker Desktop입니다.

1. 환경변수 예시를 복사합니다.

```powershell
Copy-Item .env.example .env
```

`.env`의 `MYSQL_PASSWORD`, `MYSQL_ROOT_PASSWORD`, `JWT_SECRET`을 로컬 전용 값으로 바꾸세요. `.env`와 `application-local.yml`은 Git에서 제외됩니다. 실제 운영 비밀값은 저장소에 커밋하지 않습니다.

2. MySQL을 실행합니다.

```powershell
docker compose --env-file .env up -d mysql
```

3. 백엔드를 실행합니다. 애플리케이션은 프로젝트 루트의 `.env`를 자동으로 읽습니다.

```powershell
Set-Location backend
.\gradlew.bat bootRun
```

IntelliJ에서도 별도 환경변수 입력 없이 `ValueSwapApplication`을 실행할 수 있습니다. 실행 작업 디렉터리는 프로젝트 루트 또는 `backend`여야 합니다.

4. 새 PowerShell에서 프론트엔드를 실행합니다.

```powershell
Set-Location frontend
npm.cmd install
npm.cmd run dev
```

브라우저에서 `http://localhost:5173`을 엽니다. 개발 서버는 `/api` 요청을 `http://localhost:8080`으로 전달합니다. 별도 API 주소가 필요하면 로컬 환경에 `VITE_API_URL`을 설정하세요. 이 값은 공개 클라이언트 설정이며 비밀정보를 넣으면 안 됩니다.

## 로컬 시드 계정

`local` 프로필에서 아래 계정과 교환 시나리오가 멱등으로 생성됩니다. 공통 비밀번호 `Password1!`은 로컬 데모 전용이며 운영에서 사용하지 않습니다.

| 이메일 | 역할 | 시나리오 |
|---|---|---|
| `admin@valueswap.local` | ADMIN | 수동 매칭 실행 |
| `restaurant@valueswap.local` | BUSINESS | 식사권 제공 |
| `farmer@valueswap.local` | USER | 농산물 제공 |
| `designer@valueswap.local` | USER | 디자인 제공 |
| `burger@valueswap.local` | USER | 1:1 교환 |
| `general@valueswap.local` | USER | 1:1 교환 |

자동 매칭은 기본 5분 간격으로 실행됩니다. 즉시 실행하려면 관리자 로그인 후 받은 토큰으로 다음 요청을 보냅니다.

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/matches/run `
  -Headers @{ Authorization = "Bearer <관리자-토큰>" }
```

## 검증

```powershell
Set-Location backend
.\gradlew.bat clean test

Set-Location ..\frontend
npm.cmd test -- --run
npm.cmd run build
```

API 계약과 오류 형식은 [docs/api.md](docs/api.md)를 참고하세요.

## MVP 범위

현재 구현은 회원가입·로그인, 구조화된 교환 글 CRUD, 선택형 가치 입력, 100점 환산 적합도, 2~4자 순환 탐색, 실시간 알림, 참여 수락·거절, 참여자 전용 실시간 텍스트 거래방과 전원 거래 완료까지 포함합니다. 이미지·파일 첨부, 실제 인도·결제, 거래 취소·분쟁, 후기·신뢰도 재계산, 신고·관리자 운영 화면은 다음 단계 범위입니다.
