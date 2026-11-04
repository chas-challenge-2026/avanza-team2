# Teststatus

[![Backend CI](https://github.com/chas-challenge-2026/avanza-team2/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/chas-challenge-2026/avanza-team2/actions/workflows/backend-ci.yml)
[![Frontend CI](https://github.com/chas-challenge-2026/avanza-team2/actions/workflows/frontend-ci.yml/badge.svg)](https://github.com/chas-challenge-2026/avanza-team2/actions/workflows/frontend-ci.yml)
[![Native CI](https://github.com/chas-challenge-2026/avanza-team2/actions/workflows/native-build-test.yml/badge.svg)](https://github.com/chas-challenge-2026/avanza-team2/actions/workflows/native-build-test.yml)

**Senast uppdaterad:** 2026-11-04  
**Övergripande status:** Testinfrastruktur och tester finns för backend, frontend och native-kod. Aktuellt resultat från testkörningar och senaste CI-status har inte verifierats i samband med denna sammanställning.

> Att testfiler och CI-arbetsflöden finns innebär inte i sig att testerna passerar. Statusen nedan beskriver den konfiguration och testtäckning som hittats i repot, inte ett verifierat grönt resultat.

## Översikt

| Område   | Befintliga tester och kontroller                                                                                        | Status                                     |
| -------- | ----------------------------------------------------------------------------------------------------------------------- | ------------------------------------------ |
| Backend  | 25 Java-testfiler med Spring/JUnit-tester för bland annat tjänster, controllers, säkerhet, klienter och native-bryggan. | Uppsatt; senaste körresultat ej verifierat |
| Frontend | Jest och Testing Library. En testfil för inloggningssidan innehåller fyra testfall.                                     | Uppsatt; senaste körresultat ej verifierat |
| Native   | CI bygger native-moduler och kör testmål för risk, FX och backtest.                                                     | Uppsatt; senaste körresultat ej verifierat |

## Backend

Backendens tester finns i `backend/AvanzaPortal/src/test/java`. De täcker bland annat tjänster, controllers, autentisering och JWT, externa marknadsklienter, undantagshantering och mappning till native-backtest.

Kör hela backendens testsvit från repots rot:

```bash
mvn -f backend/AvanzaPortal/pom.xml clean test
```

GitHub Actions-arbetsflödet för backend kör `mvn --batch-mode --update-snapshots verify` vid relevanta pushar och pull requests. Det finns enligt [backendens utvecklardokumentation](../backend/AvanzaPortal/DEVELOPMENT.md) fortfarande behov av fler integrations-, säkerhets- och lasttester innan systemet är produktionsredo.

## Frontend

Frontend använder Jest med jsdom och Testing Library. Den befintliga testfilen för inloggningssidan har fyra testfall: validering av tomma fält, lyckad inloggning, felaktiga inloggningsuppgifter och visning av lösenord.

Kör testerna från `frontend/`:

```bash
npm test
```

Frontendens CI kör lint, TypeScript-typkontroll, tester och produktionsbygge. [Frontendens utvecklardokumentation](../frontend/DEVELOPMENT.md) säger att testsvit saknas och att `npm test` är en platshållare. Det stämmer inte med den nuvarande konfigurationen: Jest är konfigurerat och en testfil finns. Dokumentationen bör uppdateras.

## Native

Native-arbetsflödet bygger modulerna och kör testmål för risk, FX och backtest. FX:s livehämtning från nätverket är markerad `continue-on-error`, så ett fel i det steget stoppar inte arbetsflödet.

Enligt [riskmodulens dokumentation](../native/risk/README.md) är dess testkörning enklare kontroller/benchmarkfall snarare än en bred uppsättning testfall med explicita förväntningar. FX har ett offline-test som används av CI samt ett separat live-test. Backtestmodulen har ett eget testmål.

## Källor

- [Backendens utvecklardokumentation](../backend/AvanzaPortal/DEVELOPMENT.md)
- [Frontendens utvecklardokumentation](../frontend/DEVELOPMENT.md) och testkonfiguration i `frontend/package.json`
- CI: `.github/workflows/backend-ci.yml`, `.github/workflows/frontend-ci.yml`, `.github/workflows/native-build-test.yml`
- Native-testning: `native/risk/README.md`, `native/fx/README.md`, `native/backtest/README.md`
