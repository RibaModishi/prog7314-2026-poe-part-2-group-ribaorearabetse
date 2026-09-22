# --- Build stage -------------------------------------------------------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy Gradle wrapper first so dependency downloads are cached across builds
COPY gradlew gradlew.bat ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
RUN chmod +x gradlew

COPY src ./src
RUN ./gradlew installDist --no-daemon

# --- Runtime stage -------------------------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/install/campus-companion-api ./

# In Cloud Run / Render, mount or bake the real service-account file and point
# FIREBASE_CREDENTIALS_PATH at it. Never bake real credentials into the image itself.
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["./bin/campus-companion-api"]
