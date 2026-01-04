# 배달의민족 스타일 프로젝트

대규모 트래픽 대응 (10만명 동시 이용 가능) 배달 서비스 프로젝트

## 🚀 주요 기능

- 주문 관리
- 장바구니 관리 (Redis 캐시)
- 결제 처리
- 배달 관리
- 관리자 기능

## 🏗️ 아키텍처

### 모듈 구조
- `api`: REST API 모듈
- `admin-api`: 관리자 API 모듈
- `application`: 비즈니스 로직 서비스
- `domain`: 도메인 모델
- `db`: 데이터베이스 엔티티
- `auth`: 인증/인가
- `common`: 공통 유틸리티

### 기술 스택
- **Backend**: Spring Boot 3.5.7, Java 21
- **Database**: MySQL 8.0
- **Cache**: Redis 7
- **Load Balancer**: Nginx
- **Monitoring**: Prometheus, Grafana
- **CI/CD**: Jenkins
- **Container**: Docker, Docker Compose

## 📦 설치 및 실행

### 사전 요구사항
- Docker & Docker Compose
- Java 21 (로컬 개발 시)
- Gradle 8.x

### Docker Compose로 실행

```bash
# 전체 서비스 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f api

# 서비스 중지
docker-compose down
```

### 로컬 개발 환경

```bash
# 빌드
./gradlew clean build

# 실행
./gradlew :api:bootRun
```

## 🔧 성능 최적화

### 대규모 트래픽 대응
- **로드 밸런싱**: Nginx를 통한 5개 API 인스턴스 분산
- **커넥션 풀 최적화**: HikariCP 설정 최적화
- **캐싱 전략**: Redis를 활용한 캐싱
- **비동기 처리**: 비블로킹 작업 처리

### CPU 사용률 개선
- **가상 스레드**: Java 21 가상 스레드 활용
- **비동기 처리**: @Async를 통한 비블로킹 작업
- **스레드 풀 최적화**: CPU 코어 수에 맞춘 스레드 풀 설정

### 메모리 누수 방지
- **G1GC**: G1 가비지 컬렉터 사용
- **캐시 TTL**: Redis 캐시 자동 만료
- **커넥션 풀 관리**: HikariCP 커넥션 누수 감지

### 병목 제거
- **데이터베이스 최적화**: 인덱스, 쿼리 최적화
- **캐싱**: 자주 조회되는 데이터 캐싱
- **비동기 처리**: 블로킹 작업 최소화

## 📊 모니터링

### Prometheus
- URL: http://localhost:9090
- 메트릭 수집: Spring Boot Actuator, MySQL, Redis, Nginx

### Grafana
- URL: http://localhost:3000
- 기본 계정: admin / admin
- 대시보드: CPU, 메모리, 요청 수, 응답 시간 등

### 헬스체크
```bash
# API 헬스체크
curl http://localhost:8080/actuator/health

# 메트릭 조회
curl http://localhost:8080/actuator/metrics
```

## 🐳 Docker 설정

### Dockerfile
- 멀티 스테이지 빌드로 이미지 크기 최소화
- JVM 최적화 옵션 적용
- 헬스체크 설정

### docker-compose.yml
- 5개 API 인스턴스로 트래픽 분산
- 리소스 제한 설정 (CPU, 메모리)
- 헬스체크 및 자동 재시작 설정

## 🔄 CI/CD

### Jenkins Pipeline
1. 코드 체크아웃
2. 코드 품질 검사
3. 빌드
4. 보안 스캔
5. Docker 이미지 빌드
6. 통합 테스트
7. 배포 (Rolling Update)
8. 스모크 테스트

## 📝 환경 변수

### application.yml
- `SPRING_PROFILES_ACTIVE`: 환경 설정 (dev, prod)
- `DB_HOST`, `DB_PORT`, `DB_NAME`: 데이터베이스 설정
- `REDIS_HOST`, `REDIS_PORT`: Redis 설정

## 🔒 보안

- JWT 기반 인증
- Spring Security 설정
- 관리자 권한 검증 (@PreAuthorize)

## 📈 성능 벤치마크

### 목표 성능
- **동시 사용자**: 10만명
- **응답 시간**: 평균 200ms 이하
- **처리량**: 초당 10,000 요청
- **가용성**: 99.9%

## 🛠️ 개발 가이드

### 코드 스타일
- Java 21 문법 사용
- 도메인 주도 설계 (DDD) 원칙 준수
- 단일 책임 원칙 준수

### 테스트
```bash
# 단위 테스트
./gradlew test

# 통합 테스트
./gradlew integrationTest
```

## 📚 참고 자료

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Docker 공식 문서](https://docs.docker.com/)
- [Jenkins 공식 문서](https://www.jenkins.io/doc/)

## 📄 라이선스

이 프로젝트는 교육 목적으로 제작되었습니다.

