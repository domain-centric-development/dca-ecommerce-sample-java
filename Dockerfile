# syntax=docker/dockerfile:1
# Two stages: build the Spring Boot jar with the Gradle wrapper, run it on a JRE.
#   docker build -t dca-shop-java .
#   docker run --rm -p 8080:8080 dca-shop-java
# The building blocks and rule catalog come from Maven Central, so the build needs no sibling checkout.

FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Wrapper + build scripts first so the dependency download is cached across source changes
COPY gradlew settings.gradle build.gradle gradle.properties ./
COPY gradle ./gradle
RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon dependencies > /dev/null || true

COPY src ./src
RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon bootJar \
 && mv build/libs/*.jar /workspace/app.jar

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --uid 1001 shop
USER shop
COPY --from=build /workspace/app.jar ./app.jar
EXPOSE 8080
# SPRING_PROFILES_ACTIVE=jdbc switches to the JDBC persistence profile (H2 in memory)
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
