# Java-backend

Backenden för Avanza Portföljhälsa är ett REST API byggt med Java 21 och
Spring Boot. Den hanterar autentisering, användarnas konton och innehav,
portföljberäkningar samt driftvarningar.

Projektet moderniseras från en äldre version där SQL och affärslogik låg direkt
i controllers. Den nya strukturen delar upp ansvaret mellan controllers,
services, repositories, DTO:er och entities.

## Teknikstack

- Java 21
- Spring Boot 3.2
- Spring Web och Spring Security med JWT
- Spring Data JPA, Hibernate och Flyway
- PostgreSQL 12
- Maven
- JUnit 5 och Mockito
- JNA för kommunikation med native C/C++-kod

## Struktur

Källkoden finns under `src/main/java/se/comerit/avanza`:

| Paket | Ansvar |
| --- | --- |
| `controller` | Tar emot HTTP-anrop och returnerar HTTP-svar. |
| `service` | Innehåller affärslogik och samordnar repositories. |
| `repository` | Läser och sparar data med Spring Data JPA. |
| `entity` | Representerar databastabeller och relationer. |
| `dto` | Definierar typade request- och responseobjekt för API:t. |
| `security` | Hanterar JWT, autentisering och åtkomstregler. |
| `exception` | Gemensam hantering av fel och HTTP-statuskoder. |
| `nativebridge` | Kopplar Java-backenden till native kod genom JNA. |

Det huvudsakliga flödet är:

```text
HTTP request -> Controller -> Service -> Repository -> PostgreSQL
                      |            |
                      +--> DTO <---+
```

Controllers ska vara tunna. De läser requestdata och den autentiserade
användaren, anropar rätt service och skapar ett HTTP-svar. Affärslogik och
behörighetskontroller ska ligga i servicelagret.

## Förutsättningar

- JDK 21
- Maven 3.9 eller senare
- Docker Desktop med Docker Compose

Kontrollera aktuell Java-version:

```bash
java -version
mvn -version
```

På macOS kan Java 21 väljas för den aktuella terminalen med:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

## Starta systemet med Docker

Kör från projektets rot:

```bash
cd infra
docker compose up --build
```

Docker startar PostgreSQL och Spring Boot-applikationen. Backenden lyssnar på
port `8082`. Lokal portmappning styrs av
`infra/docker-compose.override.yml`.

Stoppa miljön med:

```bash
docker compose down
```

## Starta backenden med Maven

Databasen måste vara tillgänglig och datasource-inställningarna måste peka på
rätt värd. Gå därefter till Maven-modulen:

```bash
cd backend/AvanzaPortal
mvn spring-boot:run
```

Standardkonfigurationen finns i `src/main/resources/application.properties`.
Docker Compose skriver över databasinställningarna genom miljövariabler.

## API-endpoints

| Metod | Endpoint | Beskrivning |
| --- | --- | --- |
| `POST` | `/api/auth/login` | Verifierar användaren och returnerar en JWT. |
| `DELETE` | `/api/auth/logout` | Avslutar klientens inloggningsflöde. |
| `GET` | `/api/portfolio` | Hämtar portföljöversikten. |
| `GET` | `/api/holdings` | Hämtar användarens innehav. |
| `POST` | `/api/holdings/add` | Lägger till ett innehav. |
| `POST` | `/api/holdings/delete` | Tar bort ett innehav. |
| `GET` | `/api/alerts` | Hämtar driftvarningar. |
| `PUT` | `/api/alerts/{id}/dismiss` | Markerar en varning som hanterad. |

Alla endpoints förutom `/api/auth/**` kräver autentisering. JWT skickas i
HTTP-headern:

```http
Authorization: Bearer <token>
```

## Autentisering och säkerhet

Spring Security är konfigurerat för ett stateless API. Servern ska därför inte
förlita sig på `HttpSession` för att identifiera användaren. JWT-filtret läser
token, verifierar den och lägger användarens identitet i Spring Securitys
`SecurityContext`.

Lösenord verifieras med BCrypt. Äldre MD5-lösenord migreras vid en lyckad
inloggning där det fortfarande behövs.

Ägarskapskontroller ska förhindra IDOR-sårbarheter: en autentiserad användare får
endast läsa eller ändra resurser som tillhör det egna kontot.

JWT-hemligheten ska sättas genom miljövariabeln `JWT_SECRET` utanför lokal
utveckling. Standardvärdet i `application.properties` är endast för utveckling.

## Databas och migreringar

PostgreSQL används som databas. Databasstrukturen versioneras med Flyway och
migreringarna finns i:

```text
src/main/resources/db/migration
```

Nya schemaändringar ska läggas till som en ny Flyway-migrering. En redan körd
migrering ska inte ändras eftersom Flyway kontrollerar dess checksumma.

Spring Data JPA används genom repository-interface. Delar av den äldre koden
kan fortfarande använda `JdbcTemplate`; kvarvarande SQL ska migreras stegvis
utan att API-beteendet eller behörighetskontrollerna försvinner.

## DTO:er

API:t använder DTO:er för att skilja externa request- och responsemodeller från
JPA-entiteterna. Det gör API-kontraktet tydligare och undviker att interna
databasrelationer exponeras direkt. DTO:erna är indelade efter område:

```text
dto/auth
dto/alerts
dto/holdings
dto/portfolio
```

När en DTO refaktoreras ska befintliga JSON-fältnamn behållas, om förändringen
inte uttryckligen är en avtalad breaking change.

## Tester

Testerna finns under `src/test/java` och använder JUnit 5 samt Mockito. Kör hela
testsviten från backendmodulen:

```bash
cd backend/AvanzaPortal
mvn clean test
```

Kör en enskild testklass med:

```bash
mvn -Dtest=PortfolioServiceTest test
```

Servicetester verifierar affärslogik med mockade repositories. Controller- och
säkerhetstester bör även kontrollera HTTP-status, autentisering och API:ts
responsekontrakt. JPA-frågor och relationer behöver dessutom integrationstester
mot en riktig eller containerbaserad PostgreSQL-databas.

## Genomförd modernisering

- Projektet använder Java 21 och Spring Boot 3.2.
- Ett servicelager har införts för att flytta logik från controllers.
- Spring Data JPA och Flyway har lagts till.
- Spring Security, JWT och BCrypt har införts.
- Typade DTO:er används i flera API-flöden.
- Tester finns för services, controllers och JWT-komponenter.
- JNA-stöd finns för kommande native riskberäkningar.

## Kända begränsningar och fortsatt arbete

- Vissa controllers och services är fortfarande under migrering till den nya
  stateless-strukturen.
- `JdbcTemplate` finns kvar i delar av backenden och ska ersättas stegvis.
- Aktiekurser och USD/SEK-kurs är fortfarande hårdkodade på vissa ställen.
- Drifttröskeln är inte konsekvent i alla flöden.
- Pagination används ännu inte genomgående.
- Fler integrations- och säkerhetstester behövs.
- Native riskmått ska kopplas in i det körande API-flödet.

En detaljerad lista över tekniska risker och kvarvarande problem finns i
[`docs/known-bugs.md`](../../docs/known-bugs.md).

## Bidra till backenden

1. Skapa eller välj ett GitHub-issue.
2. Skapa en branch från den senaste versionen av `develop`.
3. Gör en avgränsad förändring och lägg till relevanta tester.
4. Kör `mvn clean test` med Java 21.
5. Pusha branchen och öppna en pull request mot `develop`.
6. Be minst en gruppmedlem granska ändringen före merge.

Undvik att blanda orelaterade refaktoreringar i samma pull request. Dokumentera
medvetna ändringar av API-kontrakt, databasstruktur eller säkerhetsbeteende i
PR-beskrivningen.
