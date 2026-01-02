# Build Stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./gradlew clean bootJar --no-daemon

# Run Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# G1GC 및 가상 스레드 최적화 옵션
ENTRYPOINT ["java", \
            "-XX:+UseG1GC", \
            "-XX:MaxRAMPercentage=75.0", \
            "-XX:MaxGCPauseMillis=100", \
            "-XX:+UseStringDeduplication", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "app.jar"]