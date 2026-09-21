# Native PoC-bibliotek för #30 och #33. Byggs på jammy så glibc-ABI matchar
# runtime-imagen nedan. make -C jna_poc kör modulens egen Makefile direkt, så
# libpoc.so byggs med -fPIC (rot-Makefilen exporterar CFLAGS och tar bort den).
FROM ubuntu:22.04 AS native
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc g++ make libc6-dev libcurl4-openssl-dev \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /native
COPY native/ .
RUN make -C jna_poc
RUN make -C fx solib
RUN make -C risk solib

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY backend/AvanzaPortal/pom.xml .
RUN mvn dependency:resolve -q
COPY backend/AvanzaPortal/src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:21-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends \
    libcurl4 \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/avanza-portal-1.0-SNAPSHOT.jar app.jar
COPY --from=native /native/jna_poc/libpoc.so /app/libpoc.so
COPY --from=native /native/fx/build/libfx.so /app/libfx.so
COPY --from=native /native/risk/build/librisk.so /app/librisk.so
EXPOSE 8082
ENTRYPOINT ["java", "-Djna.library.path=/app", "-jar", "app.jar"]
