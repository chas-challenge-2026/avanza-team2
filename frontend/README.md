# Frontend – Investerings- & Portföljöversikt

## 🚀 Installation, Körning & Testning

För att köra frontend-applikationen lokalt behöver du Node.js installerat. 

1. Navigera först in i frontend-mappen:
   ```bash
   cd frontend

   1. Installera alla beroenden:
   npm install

   2. Starta utvecklingsservern:
   npm run dev

## Testning & Kodvalidering
Stå kvar i frontend-mappen för att köra tester och typkontroller:

  Kör testsviten (Jest): 
  npm run test

  Kör TypeScript-typkontroll: 
  npm run typecheck
  
##  Kända brister och avgränsningar

* **Målallokering i inställningar:** Gränssnitt för att ändra mål för varje konto har lagts till under inställningar, men funktionaliteten är i dagsläget inte helt färdigställd.

* **Användning av mockad data:** För vissa funktioner och vyer används i dagsläget mockdata i frontend. Detta är gjort för att kunna demonstrera den tänkta funktionaliteten och visningen i gränssnittet även där backend-integrationen inte är helt färdigställd.
