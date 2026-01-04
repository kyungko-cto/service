# ============================================
# 멀티 스테이지 빌드 - 대규모 트래픽 대응
# ============================================

# Stage 1: 빌드 스테이지
FROM eclipse-temurin:21-jdk-alpine AS build

# 빌드 최적화: 레이어 캐싱 활용
WORKDIR /app

# Gradle 래퍼 및 설정 파일 복사 (의존성 변경 시에만 재빌드)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 의존성 다운로드 (소스 코드 변경과 분리하여 캐싱)
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon || true

# 소스 코드 복사
COPY . .

# 애플리케이션 빌드
RUN ./gradlew clean bootJar --no-daemon -x test

# Stage 2: 런타임 스테이지 - 최소 이미지 크기
FROM eclipse-temurin:21-jre-alpine

# 보안: 비 root 사용자로 실행
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/api/build/libs/*.jar app.jar

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM 최적화 옵션 - 대규모 트래픽 대응
# 리팩토링: CPU 사용률 개선 및 메모리 누수 방지
ENV JAVA_OPTS="-XX:+UseG1GC \
  -XX:MaxRAMPercentage=75.0 \
  -XX:MaxGCPauseMillis=100 \
  -XX:+UseStringDeduplication \
  -XX:+ParallelRefProcEnabled \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/tmp/heapdump.hprof \
  -XX:+ExitOnOutOfMemoryError \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.profiles.active=prod"

# 가상 스레드 활성화 (Java 21)
ENV JAVA_TOOL_OPTIONS="--enable-preview"

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
