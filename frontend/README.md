Here's the Swedish translation — text only, no files touched.

---

# Frontend – Investerings- & Portföljöversikt

En React + TypeScript-applikation (SPA) för att se och hantera en simulerad investeringsportfölj: kontoinnehav, drift-/rebalanseringsvarningar, portföljanalys, dokument och inställningar.

## Teknikstack

- React 19 + TypeScript
- Vite (dev-server & bygge)
- React Router v7 (routing)
- Tailwind CSS v4 (styling)
- Recharts (diagram)

## 🚀 Installation, Körning & Testning

För att köra frontend-applikationen lokalt behöver du Node.js installerat.

Navigera först in i `frontend`-mappen:

```bash
cd frontend
```

**1. Installera alla beroenden:**

```bash
npm install
```

**2. Starta utvecklingsservern:**

```bash
npm run dev
```

Appen körs på `http://localhost:5173` som standard och förväntar sig att backend-API:et körs separat (se root-README för backend-/Docker-uppsättning).

### Testning & Kodvalidering

Stå kvar i `frontend`-mappen för att köra följande:

- Kör testsviten (Jest): `npm run test`
- Kör TypeScript-typkontroll: `npm run typecheck`

## Sidor & Funktionalitet

Appen har sex skyddade sidor bakom inloggning, samt inloggnings-/lösenordsåterställningsflöden.

| Route | Sida | Syfte |
|---|---|---|
| `/` | **Översikt** | Startvy efter inloggning. Visar totalt portföljvärde, investerat belopp, ett portföljsammandrag, drift-varningsbanner, tabell per konto, samt en fördelningsöversikt (Aktier/Fonder-split, exponering mot land/bransch) och ett flöde med senaste aktivitet. |
| `/innehav` | **Mina Innehav** | Listar alla innehav över konton med sök och filter (konto, valuta). Visar kort per konto med målallokering och driftstatus, samt öppnar en "snabb rebalansering"-modal för att justera ett kontos målallokering mot riktig backend-data. |
| `/notiser` | **Notiser** | Drift- och varningsnotiser (t.ex. att ett konto avviker från sin målallokering), med filtrering. |
| `/analyser` | **Portföljanalys** | Djupare portföljanalys: riskmått (volatilitet, Sharpe-kvot, max drawdown — hämtade från det nativa risk-biblioteket) samt en vy för rebalanseringssammanfattning. |
| `/documents` | **Dokument** | Dokument-/historikarkiv med filtrering (kontoutdrag, rapporter m.m.). |
| `/installningar` | **Inställningar** | Kontoinställningar, inklusive en redigerare för målallokering per konto och en växel för drift-notiser. |

Inloggning, "glömt lösenord" och "återställ lösenord" är publika routes utanför det inloggade skalet (ingen navbar/topbar).

## Navigation (Navbar & TopBar)

Appens skal har två samverkande navigationsdelar:

- **TopBar** — en fast header som visar den aktuella sidans titel (hämtad från den aktiva routen), en hamburgermeny-knapp (mobil), samt den inloggade användarens avatar/initialer.
- **Navbar** — vänstermenyn med sex länkar (Översikt, Innehav, Notiser, Analyser, Dokument, Inställningar), var och en med ikon och markering av aktiv sida. På mobil blir den en utdragbar meny (glider in/ut, stängs vid klick på bakgrunden eller vid navigering); på desktop (`md:`-brytpunkt och uppåt) är den en statisk, alltid synlig sidomeny.

Routing och sidtitlar styrs från en enda `routes`-array i `App.tsx`, så att lägga till en ny sida blir en enradsändring (path, titel, element) istället för att behöva uppdatera navbaren och titelraden separat.

## Kända brister och avgränsningar

- **Målallokering i inställningar**: ett gränssnitt för att ändra målallokering för varje konto har lagts till under Inställningar, men hela spar-/valideringsflödet är i dagsläget inte helt färdigställt.
- **Användning av mockad data**: vissa vyer (t.ex. Aktier/Fonder-fördelningen på Översikt och Innehav, samt exponering mot land/bransch) använder i dagsläget mockdata i frontend. Detta är medvetet — det gör att gränssnittet kan visa den tänkta funktionaliteten och layouten även där motsvarande datamodell i backend (t.ex. tillgångstyp per innehav) ännu inte är implementerad. Dessa ställen är markerade med `// TODO: mock data`-kommentarer i koden så att de är lätta att hitta när backend kommer ikapp.

