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

# create a non-root user
RUN useradd -ms /bin/bash appuser

# copy the jar produced by Spring Boot (pom sets <finalName>app</finalName>)
COPY --from=build /app/target/app.jar /app/app.jar

# --- Lightweight entrypoint that verifies env (without leaking secrets) then starts the app ---
# Set CHECK_ENV_ON_START=0 in Cloud Run to disable after you've verified things.
RUN set -eu; \
  echo '#!/usr/bin/env sh'                                              >  /app/entrypoint.sh && \
  echo 'set -eu'                                                        >> /app/entrypoint.sh && \
  echo ''                                                               >> /app/entrypoint.sh && \
  echo 'log() { printf "%s\n" "$*"; }'                                  >> /app/entrypoint.sh && \
  echo 'check_var() {'                                                  >> /app/entrypoint.sh && \
  echo '  VAR="$1"; VAL="$(printenv "$VAR" || true)";'                  >> /app/entrypoint.sh && \
  echo '  if [ -z "$VAL" ]; then'                                       >> /app/entrypoint.sh && \
  echo '    log "  - $VAR: MISSING";'                                   >> /app/entrypoint.sh && \
  echo '  else'                                                         >> /app/entrypoint.sh && \
  echo '    case "$VAR" in *PASSWORD*|*KEY*) log "  - $VAR: **set**";; * ) log "  - $VAR: $VAL";; esac' >> /app/entrypoint.sh && \
  echo '  fi'                                                           >> /app/entrypoint.sh && \
  echo '}'                                                              >> /app/entrypoint.sh && \
  echo ''                                                               >> /app/entrypoint.sh && \
  echo 'if [ "${CHECK_ENV_ON_START:-1}" = "1" ]; then'                  >> /app/entrypoint.sh && \
  echo '  log "[startup] Checking expected environment variables…"'     >> /app/entrypoint.sh && \
  echo '  for v in \\'                                                  >> /app/entrypoint.sh && \
  echo '    SUPABASE_DB_HOST \\'                                        >> /app/entrypoint.sh && \
  echo '    SUPABASE_DB_PORT \\'                                        >> /app/entrypoint.sh && \
  echo '    SUPABASE_DB_NAME \\'                                        >> /app/entrypoint.sh && \
  echo '    SUPABASE_DB_USER \\'                                        >> /app/entrypoint.sh && \
  echo '    SUPABASE_DB_PASSWORD \\'                                    >> /app/entrypoint.sh && \
  echo '    SUPABASE_SSLMODE \\'                                        >> /app/entrypoint.sh && \
  echo '    COINGECKO_BASE_URL \\'                                      >> /app/entrypoint.sh && \
  echo '    COINGECKO_API_KEY \\'                                       >> /app/entrypoint.sh && \
  echo '    SPRING_PROFILES_ACTIVE \\'                                  >> /app/entrypoint.sh && \
  echo '    PORT; do'                                                   >> /app/entrypoint.sh && \
  echo '    check_var "$v";'                                            >> /app/entrypoint.sh && \
  echo '  done'                                                         >> /app/entrypoint.sh && \
  echo 'fi'                                                             >> /app/entrypoint.sh && \
  echo ''                                                               >> /app/entrypoint.sh && \
  echo 'exec java ${JAVA_OPTS:-} -Dserver.port="${PORT:-8080}" -jar /app/app.jar' >> /app/entrypoint.sh && \
  chmod 0755 /app/entrypoint.sh

# drop privs after files are in place
USER appuser

EXPOSE 8080
ENTRYPOINT ["/app/entrypoint.sh"]
