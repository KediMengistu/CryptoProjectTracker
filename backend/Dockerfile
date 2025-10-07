# ---- Build stage (uses your pinned Maven via mvnw; Java 21) ----
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# copy only what's needed to resolve deps first (better caching)
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

# now copy sources and build
COPY . .
# re-chmod because COPY . . may overwrite mvnw without exec bit
RUN chmod +x mvnw && ./mvnw -q -DskipTests package

# ---- Run stage (Java 21 JRE) ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# run as non-root (optional but recommended)
RUN useradd -ms /bin/bash appuser
USER appuser

# copy the jar produced by Spring Boot
# If you set <finalName>app</finalName> in pom.xml:
COPY --from=build /app/target/app.jar ./app.jar
# Otherwise, fallback (pick the only fat jar Spring Boot builds):
# COPY --from=build /app/target/*.jar ./app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
