[![Backend CI](https://github.com/chas-challenge-2026/avanza-team2/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/chas-challenge-2026/avanza-team2/actions/workflows/backend-ci.yml)
[![Frontend CI](https://github.com/chas-challenge-2026/avanza-team2/actions/workflows/frontend-ci.yml/badge.svg)](https://github.com/chas-challenge-2026/avanza-team2/actions/workflows/frontend-ci.yml)
[![Native CI](https://github.com/chas-challenge-2026/avanza-team2/actions/workflows/native-build-test.yml/badge.svg)](https://github.com/chas-challenge-2026/avanza-team2/actions/workflows/native-build-test.yml)

# Avanza Portföljhälsa

Pedagogisk kodbas för kursen "Java Backend-utveckling med Spring Boot" på Chas Academy.

Applikationen simulerar en portföljöversikt där användare kan logga in,
se sina innehav, portföljvärde och driftvarningar. **v1** är byggd med Spring Boot 2.7,
Thymeleaf och JdbcTemplate, och innehåller avsiktliga antipatterns (ingen servicelager,
SQL direkt i controllers, MD5-lösenord, inga tester m.m. — se [docs/known-bugs.md](docs/known-bugs.md)
och [docs/architecture.md](docs/architecture.md)).

Kursuppdraget är att refaktorera v1 till **v2**: Java 21, Spring Boot 3.2, Spring Data JPA,
Spring Security + JWT och ett React/TypeScript-frontend. Se [docs/v2-targets.md](docs/v2-targets.md)
för fullständig målarkitektur.

## Starta programmet

1. Öppna mapp `infra/`

2. Skapa din egen `.env` fil (den är redan gitignorad och ska aldrig committas)

3. Lägg till dessa parametrar i enviroment filen och ändra nyckalar till dina egna

```env
#Database configuration
DB_USERNAME=avanza
DB_PASSWORD=some_local_password

#JWT secret - 32 karaktär minst
JWT_SECRET=change_me_to_a_random_32_char_min_string
```

4. Öppna **Terminal 1** och följande kommando för att starta docker:

```bash
cd infra
docker compose --env-file .env up --build
```

5. Öppna **Terminal 2** och följande kommando för att starta frontend:

```bash
cd frontend
npm install
npm run dev
```

6.  Öppna din lokala websida på denna porten och använd följande test användare:

**Port**

- `http://localhost:5173`

**Testanvändare:**

- `anna@example.com` / `password123`
- `erik@example.com` / `password123`

## Teknikstack

| Lager    | Teknik                                      |
| -------- | ------------------------------------------- |
| Backend  | Spring Boot 3.2.12, Java 21, Maven          |
| Frontend | React 19, TypeScript, Vite, Tailwind CSS    |
| Native   | C, C++                                      |
| Databas  | PostgreSQL 12 (seedas via `infra/seed.sql`) |
| Deploy   | Docker Compose                              |

## Utveckling

Backend och databas körs via Docker Compose enligt ovan. Frontend kan även köras
fristående i devläge med hot reload:

```bash
cd frontend
npm install
npm run dev
```

## Kör tester

```bash
cd backend/AvanzaPortal
mvn test
```

## Miljövariabler & konfiguration

Databasuppkoppling (`SPRING_DATASOURCE_*`) sätts i [infra/docker-compose.yml](infra/docker-compose.yml).
Lokal portmappning ändras i [infra/docker-compose.override.yml](infra/docker-compose.override.yml) —
rör aldrig `docker-compose.yml`, se [DRIFT.md](DRIFT.md) för plattformskontraktet.

## Dokumentation

- [docs/architecture.md](docs/architecture.md) — systembeskrivning för v1
- [docs/known-bugs.md](docs/known-bugs.md) — kända fel/antipatterns (uppgiftslista)
- [docs/README-pain-points.md](docs/README-pain-points.md) — vad som spricker vid skala
- [docs/v2-targets.md](docs/v2-targets.md) — målarkitektur för v2
- [DRIFT.md](DRIFT.md) — drift och deploy
- [backend/AvanzaPortal/Backend-ReadMe.md](backend/AvanzaPortal/Backend-ReadMe.md) — startguide, arkitektur, API och testning för Java-backenden
- [native/docs/BUILD.md](native/docs/BUILD.md) — native-dokumentation
- [native/docs/CONTRIBUTE.md](native/docs/CONTRIBUTE.md) — information om kodstilen
