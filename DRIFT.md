# Drift och deploy - läs detta först

Denna guide riktar sig särskilt till teamets driftansvariga, men alla i teamet bör känna till innehållet.

## Köra lokalt

Krav: Docker Desktop (Windows/Mac) eller Docker Engine (Linux). På Windows behöver Docker Desktop WSL2, se vanliga fel nedan.

```bash
cd infra
docker compose up --build
```

Appen svarar sedan på http://localhost:PORT. Vilken port som gäller för ert case står i `infra/docker-compose.override.yml`. Inloggningsuppgifter till seed-datan står i README.

- Stoppa: Ctrl+C, eller `docker compose down`
- Börja om med tom databas: `docker compose down -v` och sedan `up --build` igen

## Vanliga fel lokalt

- **"WSL 2 installation is incomplete" (Windows):** kör `wsl --install` i PowerShell som administratör, starta om datorn, starta Docker Desktop igen.
- **"port is already allocated":** någon annan process använder porten. Stäng den, eller ändra porten i `infra/docker-compose.override.yml` (den filen är er att ändra).
- **Kodändringar syns inte:** ni har glömt `--build`.
- **Databasen i konstigt läge:** `docker compose down -v` och börja om. Volymen är bara lokal, inget försvinner i driftmiljön.

## Så funkar deploy

- Push till `develop` bygger om er stage-miljö, push till `main` bygger om prod. Adresserna står i README.
- Grön bock eller rött X på committen i GitHub visar hur deployen gick. Vid rött X: klicka på markeringen och läs byggloggen.
- **Ett misslyckat bygge sänker inte er miljö.** Senast fungerande version fortsätter köra tills ett nytt bygge går igenom.
- Bygget tar några minuter. Vid deadline pushar alla team samtidigt och kön blir längre: pusha i god tid.
- Arbetsflöde: testa alltid på `develop` innan ni mergar till `main`.

## Plattformskontraktet - fyra regler

Er deploy-miljö kräver att:

1. `infra/docker-compose.yml` ligger kvar på sin plats
2. webbtjänsten heter `app`
3. `expose` används i `infra/docker-compose.yml`, aldrig `ports` (lokala portar hör hemma i `docker-compose.override.yml`)
4. appens faktiska lyssningsport matchar expose-värdet

En automatisk kontroll körs på varje push och ger rött X med förklaring om regel 1-3 bryts. Regel 4 kan inte kontrolleras automatiskt: byter ni port appen lyssnar på, uppdatera expose-värdet samtidigt.

Allt annat är fritt fram: uppgradera språkversion, ramverk, basimages i Dockerfile, lägga till tjänster i composen och så vidare.

## Byta Postgres-version (v2-kravet)

Databasvolymen i er driftmiljö innehåller datafiler från nuvarande Postgres-version. Byter ni bara image-version startar databasen inte. Gör så här:

1. Testa lokalt först (`docker compose down -v` ger er en färsk lokal volym).
2. Skicka ett techsupport-ärende (kategori "Deploy & CI") INNAN ni pushar versionsbytet till `develop`/`main`, och skriv att ni behöver databas-reset för Postgres-versionsbyte.
3. Vi nollställer volymen i driftmiljön i samband med er deploy.

## Nollställa databasen i driftmiljön

Ni kan inte själva nollställa databasen i stage/prod. Skicka ett techsupport-ärende: https://chas-challenge.comerit.se/support/

## Hemlighethantering (secrets)

**Princip:** Aldrig hardkodade hemligheter i koden eller docker-compose.yml. Använd miljövariabler istället.

**Kör lokalt med secrets:**

```bash
cd infra
docker compose --env-file .env.local up --build
```

**Stage/Prod:**

- Secrets lagras i GitHub Actions secrets: https://github.com/settings/secrets/actions
- Behöriga: team lead och driftansvarig
- Deploy-workflowet injicerar dem vid bygge
- Se `.github/workflows/` för implementation

**Nuvarande secrets:**

- `DB_PASSWORD` (PostgreSQL)
- `JWT_SECRET` (säkerhet v1)
- `FX_API_KEY` (v2-ej implementerad än)

**Att lägga till nya secrets:**

1. Definiera secret där det behövs for ex. i `docker-compose.yml` som `${SECRET_NAME}`
2. Lägg till i `.env.local`
3. Lägg till i GitHub Actions secrets (admin)

**Rotation av stage/prod-secrets:**
Om du byter lösenord i GitHub Actions secrets, behöver du:

1. **Uppdatera GitHub Actions secret:** `DB_PASSWORD=nytt_lösenord`
2. **Nollställa databasen:** Skicka techsupport-ärende (se ovan). DB-reset krävs för att ny lösenordshash ska gälla.
3. **Verify:** Efter DB-reset, testa att login fungerar med gamla seed-data.

## Frontend i v2

Bygg frontenden i samma Dockerfile som backend (eget byggsteg som kopierar in byggresultatet i backend-imagen). En separat frontend-container får ingen egen publik adress.
