# Frontend

Frontenddelen för Avanza Portföljhälsa är en single-page application byggd med
React 19, TypeScript och Vite. Den innehåller vyer för portföljöversikt,
innehav, notiser, analyser, dokument och inställningar samt flöden för
inloggning och lösenordsåterställning.

## Teknikstack

- React 19 och TypeScript
- Vite för lokal utveckling och produktionsbyggen
- React Router för klientbaserad navigering
- Tailwind CSS 4 för styling
- Recharts för diagram
- Font Awesome för ikoner
- ESLint för kodlintning

## Struktur

Källkoden finns under `src/`:

| Mapp/fil | Ansvar |
| --- | --- |
| `Components/` | Återanvändbara UI-komponenter och layoutdelar. |
| `Pages/` | Sidvyer, till exempel portföljöversikt, innehav och notiser. |
| `context/` | Autentiseringstillstånd via React Context och `useAuth`. |
| `utils/` | Delade hjälpfunktioner, till exempel formatering. |
| `assets/` | Bilder och andra statiska resurser. |
| `App.tsx` | Routning och övergripande sidlayout. |
| `main.tsx` | Monterar React-appen och dess providers. |
| `index.css`, `App.css` | Globala stilar och appens kompletterande CSS. |

Skyddade sidor använder `ProtectedRoute`. Autentiseringstillståndet hanteras
med `AuthProvider` och backendens JWT-cookie.

## Förutsättningar

- Node.js med npm
- Backend och databas om du vill testa funktioner som hämtar data från API:t

Installera beroenden från frontendmappen:

```bash
cd frontend
npm ci
```

## Starta i utvecklingsläge

Starta Vite från `frontend/`:

```bash
npm run dev
```

Vite skriver ut den lokala adressen i terminalen (vanligtvis
`http://localhost:5173`). Utvecklingsservern proxar `/api` till
`http://localhost:8082`, enligt `vite.config.ts`. Starta backendmiljön separat
om du vill använda API-anropen.

## API och autentisering

API-anrop använder `VITE_API_URL` som valfri basadress. Om variabeln inte är
satt används en relativ URL, vilket fungerar med Vites `/api`-proxy i
utvecklingsläge. Exempel på en lokal `.env`-inställning:

```env
VITE_API_URL=
```

Vite-exponerade `VITE_`-variabler ingår i klientbygget och ska därför inte
innehålla hemligheter.

Inloggning, sessionkontroll och utloggning använder backendens
`/api/auth/login`, `/api/auth/me` och `/api/auth/logout`. Anropen inkluderar
credentials så att webbläsaren kan ta emot och skicka JWT-cookien. Token ska
inte lagras i `localStorage` eller läsas av JavaScript.

Portföljöversikten och notissidan hämtar data från backendens API. Andra vyer
kan fortfarande använda exempeldata eller sakna full backendkoppling; kontrollera
respektive sida innan du förutsätter att en funktion är färdigintegrerad.

## Bygg och kontrollera koden

Kör kommandona från `frontend/`:

```bash
npm run build
npm run lint
npm run typecheck
```

`npm run build` kör TypeScript-projektbygget och skapar sedan produktionsfiler i
`dist/`. `npm run lint` kör ESLint. `npm run typecheck` kör TypeScript utan att
skapa utdatafiler.

Det finns ännu ingen frontendtestsvit konfigurerad. `npm test` är för
närvarande en platshållare och kör inga tester.

## Bidra till frontend

1. Skapa eller välj ett GitHub-issue.
2. Skapa en branch från den senaste versionen av `develop`.
3. Håll ändringen avgränsad och följ befintliga komponent- och routningsmönster.
4. Kör `npm run build` och `npm run lint` innan du öppnar en pull request.
5. Dokumentera synliga ändringar och eventuella API-kontraktsberoenden i
   pull request-beskrivningen.