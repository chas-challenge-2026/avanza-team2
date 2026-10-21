# Java-backend

Backenden för Avanza Portföljhälsa är ett REST API byggt med Java 21 och Spring Boot 3.2. Den hanterar bland annat autentisering, innehav, portföljberäkningar, målallokering, driftvarningar, marknadsdata och backtesting.

Projektet har moderniserats från en äldre version där SQL och affärslogik låg direkt i controllers. Den nuvarande strukturen använder controllers, services, repositories, DTO:er och JPA-entiteter för att separera olika ansvarsområden.

## Teknikstack

- Java 21
- Spring Boot 3.2.12
- Spring Web
- Spring Security med JWT
- Spring Data JPA och Hibernate
- Flyway
- PostgreSQL 15
- Maven
- JUnit 5 och Mockito
- Marketstack för aktiekurser och historisk marknadsdata
- JNA för kommunikation med native C/C++-kod

## Struktur

Källkoden finns i:

```text
backend/AvanzaPortal/src/main/java/se/comerit/avanza
```

| Paket          | Ansvar                                                                  |
| -------------- | ----------------------------------------------------------------------- |
| `client`       | Kommunikation med externa tjänster som Marketstack.                     |
| `controller`   | Tar emot HTTP-anrop och returnerar HTTP-svar.                           |
| `service`      | Innehåller affärslogik och samordnar repositories och externa tjänster. |
| `repository`   | Läser och sparar data med Spring Data JPA.                              |
| `entity`       | Representerar databastabeller och relationer.                           |
| `dto`          | Definierar request- och responseobjekt för API:t.                       |
| `security`     | Hanterar JWT, autentisering och åtkomstregler.                          |
| `exception`    | Hanterar applikationsfel och HTTP-statuskoder.                          |
| `nativebridge` | Kopplar Java-backenden till native C/C++-kod genom JNA.                 |

Det huvudsakliga flödet är:

```text
React frontend
      |
      v
REST Controller
      |
      v
Service
  |        \
  v         v
Repository  Marketstack/JNA
  |
  v
PostgreSQL
```

Controllers ska främst hantera requests och responses. Affärslogik och behörighetskontroller ska ligga i servicelagret.

## Förutsättningar

För att köra projektet lokalt behövs:

- Docker Desktop med Docker Compose
- JDK 21
- Maven 3.9 eller senare
- Node.js om frontenden ska köras separat
- En API-nyckel från Marketstack

Kontrollera att Maven använder Java 21:

```bash
java -version
mvn -version
```

På macOS kan Java 21 väljas för den aktuella terminalen med:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

## Miljövariabler

Skapa filen `infra/.env`. Filen är gitignorerad och ska aldrig committas.

```env
DB_USERNAME=avanza
DB_PASSWORD=your_local_password
JWT_SECRET=your_random_secret_with_at_least_32_characters
MARKETSTACK_API_KEY=your_marketstack_api_key
```

Hemligheter ska alltid hanteras genom miljövariabler och får inte hårdkodas i koden.

## Starta systemet med Docker

Docker är det rekommenderade sättet att köra hela systemet eftersom imagen bygger Java-backenden, React-frontenden och de native biblioteken.

Kör från projektets rot:

```bash
cd infra
docker compose --env-file .env up --build
```

När systemet har startat finns applikationen på:

```text
http://localhost:8082
```

PostgreSQL kan nås lokalt på port `5433`.

Stoppa systemet med:

```bash
docker compose --env-file .env down
```

För att även ta bort den lokala databasvolymen:

```bash
docker compose --env-file .env down -v
```

Det sista kommandot tar bort all lokal data och bör endast användas när databasen ska återställas.

## Köra frontenden separat

Frontenden kan köras separat i utvecklingsläge med hot reload:

```bash
cd frontend
npm install
npm run dev
```

Frontenden finns då på:

```text
http://localhost:5173
```

Backenden fortsätter att köras på port `8082`.

## Testanvändare

Den lokala databasen innehåller följande användare:

```text
anna@example.com / password123
erik@example.com / password123
```

## API-endpoints

### Autentisering

| Metod    | Endpoint           | Beskrivning                                    |
| -------- | ------------------ | ---------------------------------------------- |
| `POST`   | `/api/auth/login`  | Loggar in användaren och skapar en JWT-cookie. |
| `GET`    | `/api/auth/me`     | Kontrollerar om användaren är inloggad.        |
| `DELETE` | `/api/auth/logout` | Tar bort JWT-cookien och loggar ut användaren. |

### Portfölj och innehav

| Metod  | Endpoint                              | Beskrivning                                |
| ------ | ------------------------------------- | ------------------------------------------ |
| `GET`  | `/api/portfolio`                      | Hämtar användarens portföljöversikt.       |
| `GET`  | `/api/holdings?page=0&size=10`        | Hämtar användarens innehav med pagination. |
| `POST` | `/api/holdings/add`                   | Lägger till ett innehav.                   |
| `POST` | `/api/holdings/delete?holdingId={id}` | Tar bort ett innehav.                      |

### Alerts

| Metod | Endpoint                     | Beskrivning                                                                                       |
| ----- | ---------------------------- | ------------------------------------------------------------------------------------------------- |
| `GET` | `/api/alerts?page=0&size=10` | Hämtar lagrade aviseringar, dynamiskt beräknade live-driftaviseringar och aktuell driftgräns.     |
| `PUT` | `/api/alerts/{id}/dismiss`   | Avfärdar en lagrad avisering via dess ID. Live-driftaviseringar kan inte avfärdas via endpointen. |

### Målallokering

| Metod | Endpoint                  | Beskrivning                           |
| ----- | ------------------------- | ------------------------------------- |
| `GET` | `/api/target-allocations` | Hämtar användarens målallokering.     |
| `PUT` | `/api/target-allocations` | Uppdaterar användarens målallokering. |

### Marknadsdata

| Metod  | Endpoint                     | Beskrivning                                |
| ------ | ---------------------------- | ------------------------------------------ |
| `GET`  | `/api/market/price/{ticker}` | Hämtar senaste tillgängliga aktiekurs.     |
| `GET`  | `/api/market/fx/{from}/{to}` | Hämtar växlingskurs mellan två valutor.    |
| `POST` | `/api/market/backtest`       | Kör en backtest med lagrad historisk data. |
| `POST` | `/api/market/history/import` | Importerar historiska priser för backtest. |

Alla endpoints förutom login och logout kräver en autentiserad användare.

## Aviseringsinställning i frontend

Inställningen **Notis vid avvikelse** sparas i webbläsarens `localStorage` under nyckeln `avanza.settings.drift-notifications-enabled`. Den är aktiverad som standard.

När inställningen stängs av döljs aviseringar på sidan Notiser, senaste aviseringsaktivitet och varningsmeddelanden på Översikt samt avvikelsemeddelandet i Analyser. Fördelningsdiagram och avvikelsevärden visas fortfarande som analysdata. Inställningen påverkar endast visningen i den aktuella webbläsaren; backend fortsätter att beräkna och returnera driftaviseringar.

Övriga platshållarinställningar för generella notiser, utdelningar och kvartalsrapporter finns inte längre i gränssnittet eftersom de saknar kopplad funktionalitet.

## Autentisering och säkerhet

Backenden använder Spring Security och ett stateless autentiseringsflöde. Efter en lyckad inloggning sparas JWT-token i en HttpOnly-cookie som heter `jwt`.

Frontend behöver därför skicka cookies tillsammans med API-anrop:

```javascript
fetch("/api/portfolio", {
  credentials: "include",
});
```

JWT-filtret verifierar cookien och placerar användarens identitet i Spring Securitys `SecurityContext`.

Lösenord sparas och verifieras med BCrypt. Äldre MD5-lösenord kan migreras till BCrypt efter en lyckad inloggning.

Ägarskapskontroller används för att förhindra IDOR-sårbarheter. En användare ska endast kunna läsa, ändra eller ta bort data som tillhör det egna kontot.

## Databas och Flyway

PostgreSQL 15 används som databas. Docker Compose skapar databasen och laddar testdata från:

```text
infra/seed.sql
```

Databasförändringar versioneras med Flyway. Migreringarna finns i:

```text
backend/AvanzaPortal/src/main/resources/db/migration
```

När databasschemat ändras ska en ny Flyway-migrering skapas. En migrering som redan har körts ska inte ändras eftersom Flyway kontrollerar dess checksumma.

Backenden använder Spring Data JPA och repository-interface för databasåtkomst. Den äldre användningen av `JdbcTemplate` har ersatts i den nuvarande backendkoden.

## DTO:er

DTO:er används för att skilja API-modeller från databasens JPA-entiteter.

DTO:erna är indelade efter område:

```text
dto/alerts
dto/auth
dto/holdings
dto/market
dto/portfolio
dto/targetallocation
```

Request-DTO:er använder Jakarta Validation där indata behöver kontrolleras. När en DTO ändras ska befintliga JSON-fältnamn behållas om ändringen inte uttryckligen ska vara en breaking change.

## Marknadsdata

Backenden hämtar end-of-day-priser från Marketstack. Aktiekurserna är alltså de senast tillgängliga slutkurserna och ska inte betraktas som realtidskurser.

`MarketService` översätter applikationens tickers till Marketstacks symbolformat. Exempel:

```text
ERIC-B -> ERIC-B.ST
VOLV-B -> VOLV-B.ST
SWED-A -> SWED-A.ST
SAND   -> SAND.ST
AAPL   -> AAPL
```

Priser cachas i 24 timmar för att minska antalet externa API-anrop. Om en ticker inte stöds eller Marketstack inte returnerar ett giltigt pris returneras inget pris i stället för ett hårdkodat värde.

## Native C/C++ och JNA

Java-backenden kommunicerar med native C/C++-bibliotek genom JNA.

Följande native delar är kopplade till backenden:

- FX-beräkningar
- Riskberäkningar, bland annat Sharpe-kvot
- Backtestmotor

Docker-imagen bygger biblioteken och kopierar följande filer till runtime-miljön:

```text
libfx.so
librisk.so
libbacktest.so
```

Backtest-endpointen använder historiska priser från databasen och stöder strategierna:

```text
BUY_HOLD
REBALANCE_MONTHLY
```

Minst två gemensamma handelsdagar krävs för de valda aktierna.

## Tester

Backendtesterna finns i:

```text
backend/AvanzaPortal/src/test/java
```

Testerna använder JUnit 5, Mockito och Spring MockMvc.

Kör hela testsviten från projektets rot:

```bash
mvn -f backend/AvanzaPortal/pom.xml clean test
```

Eller från backendmodulen:

```bash
cd backend/AvanzaPortal
mvn clean test
```

Kör en enskild testklass med:

```bash
mvn -Dtest=MarketServiceTest test
```

Testerna täcker bland annat:

- autentisering och JWT
- holdings och ägarskapskontroller
- portföljberäkningar
- målallokering och validering
- alerts och driftberäkningar
- Marketstack-klienter och caching
- backtest-input och mappning av native-resultat
- controllers och HTTP-statuskoder

## Genomförd modernisering

- Projektet använder Java 21 och Spring Boot 3.2.
- Servicelager har införts för att flytta logik från controllers.
- Spring Data JPA används för databasåtkomst.
- Flyway används för databasmigreringar.
- Spring Security, JWT och BCrypt har införts.
- Autentiseringen använder HttpOnly-cookie i stället för `HttpSession`.
- DTO:er används för API-anrop och responses.
- Ägarskapskontroller skyddar användarnas data.
- Holdings och alerts har pagination.
- Marketstack används i stället för hårdkodade aktiekurser.
- En gemensam driftgräns används för portfölj och alerts.
- Native FX-, risk- och backtestkod är kopplad till Java genom JNA.
- Tester finns för services, controllers, säkerhet och externa klienter.

## Kända begränsningar

[`docs/known-bugs.md`](../../docs/known-bugs.md) listan har tagits bort eftersom den blandade äldre v1-problem med
pågående v2-arbete och inte längre gav en tillförlitlig bild av backendens
aktuella status. Nya begränsningar kan upptäckas när systemet vidareutvecklas;
dokumentera dem som GitHub-issues och uppdatera den här guiden när de påverkar
hur backend körs eller används.

- Marketstack ger end-of-day-priser och inte realtidskurser.
- Endast ett begränsat antal tickers stöds av den nuvarande marknadskonfigurationen.
- Backtest kräver historiska priser i databasen.
- Historisk import startas från sidan Analyser, högst en gång per 24 timmar och webbläsare. Importen körs inte schemalagt, och endpointen kräver autentisering.
- Live-driftaviseringar beräknas vid API-anrop och sparas inte i databasen. Endast lagrade aviseringar kan avfärdas via `PUT /api/alerts/{id}/dismiss`.
- Inställningen för driftaviseringar är webbläsarlokal och styr bara vad som visas i frontend; den ändrar inte backendens beräkning eller API-svar.
- Portföljöversikten är en aggregerad vy och hämtar därför samtliga innehav utan pagination.
- JWT-cookiens produktionsinställningar behöver konfigureras separat, exempelvis `Secure=true` över HTTPS.
- Fler integrations-, säkerhets- och lasttester behövs innan systemet är produktionsredo.

## Arbetsflöde

1. Välj eller skapa ett GitHub-issue.
2. Uppdatera den lokala `develop`-branchen.
3. Skapa en separat branch för uppgiften.
4. Gör en avgränsad förändring.
5. Lägg till eller uppdatera relevanta tester.
6. Kör testerna med Java 21.
7. Pusha branchen och öppna en pull request mot `develop`.
8. Be minst en gruppmedlem granska ändringen före merge.

Exempel:

```bash
git switch develop
git pull --ff-only
git switch -c feature/issue-number-short-description
```

Undvik att blanda orelaterade ändringar i samma pull request. API-förändringar, databasändringar och säkerhetsförändringar ska förklaras i PR-beskrivningen.
