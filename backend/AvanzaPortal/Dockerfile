# Native PoC-bibliotek för #30 och #33. Byggs på jammy så glibc-ABI matchar
# runtime-imagen nedan. make -C jna_poc kör modulens egen Makefile direkt, så
# libpoc.so byggs med -fPIC (rot-Makefilen exporterar CFLAGS och tar bort den).
FROM ubuntu:22.04 AS native
RUN apt-get update && apt-get install -y --no-install-recommends \
        gcc make libc6-dev \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /native
COPY native/ .
RUN make -C jna_poc

FROM maven:3.8-openjdk-11 AS build
WORKDIR /app
COPY backend/AvanzaPortal/pom.xml .
RUN mvn dependency:resolve -q
COPY backend/AvanzaPortal/src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:11-jre-jammy
WORKDIR /app
COPY --from=build /app/target/avanza-portal-1.0-SNAPSHOT.jar app.jar
COPY --from=native /native/jna_poc/libpoc.so /app/libpoc.so
EXPOSE 8082
ENTRYPOINT ["java", "-Djna.library.path=/app", "-jar", "app.jar"]
