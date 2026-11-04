# Beslutslogg – Frontend 
---

## Ursprunglig Designvision & Användarupplevelse 

* **Kontext & Mål:** 
  Skapa ett enkelt, guidande och visuellt tydligt gränssnitt. Användaren ska inte behöva vara en finansiell expert för att få en god överblick över sin ekonomi, sina konton och sin portfölj.
* **Visuellt koncept:**
  * **Klara färger & kortbaserad layout:** Information avgränsas i tydliga rutor och kort för att undvika ett rörigt intryck.
  * **Riktad navigering:** Användaren ska nå rätt information snabbt utan oändligt klickande. Ikoner lades till i menyn för att göra valen mer ledande.
  * **Avgränsade filter:** Filter och interaktiva alternativ begränsades medvetet till de viktigaste parametrarna för att hålla fokus på kärnfunktionaliteten och undvika att projektets storlek skenade i väg.

---

## Arkitektur- & Prioriteringsbeslut

### Borttagning av graf för portföljutveckling över tid
* **Kontext:** Ursprungligen planerades en graf för att användaren enkelt skulle kunna följa sin portföljs historiska utveckling över tid.
* **Beslut:** Efter diskussioner och samtal med backend-teamet beslutades att utelämna denna graf ur slutleveransen.
* **Motivering:** Backend saknade i detta skede en snygg och prestandamässig lösning för att leverera den historiska datan på ett sätt som matchade vår design och kvalitetskraven gentemot kunden. Att tvinga fram lösningen hade krävt för mycket resurser bortom projektets ramar.
* **Konsekvens:** Resurserna lades i stället på att förfina nyckeltalsvisning, kontofokus och tydlig presentation av tillgångsfördelning.

---

## 3. Bearbetning av Feed Forward & Iterativ Utveckling

Efter feedback från kunden och specialisterna genomfördes omstruktureringar i gränssnittet:

### Rensning av information & minskad scroll 
* **Problem:** Samma information återkom på flera ställen i applikationen och översiktssidan upplevdes ha för mycket scroll.
* **Åtgärd:** Information tog bort från dubblettställen och översiktssidan städades upp. Den mest kritiska informationen samlades kort och koncist högst upp på sidan för en snabb och enkel överblick.

### Skifte till Kontonivå
* **Feedback:** Det är portföljens *enskilda konton* som ska visas, utvärderas och ha specifika målallokeringar – inte bara portföljen som en helhet.
* **Åtgärd:**
  * **Innehavssidan:** Uppdaterades till att lista portföljens alla konton. Tydliga larm/alerts visas när ett enskilt konto avviker från sitt mål, tillsammans med en direktkopplad rebalanseringsknapp på det berörda kontot.
  * **Inställningssidan:** Byggdes om så att användaren kan se portföljens alla konton och sätta/ändra målallokering på specifika kontonivåer.

---

## Kända Brister & Användning av Mock-data

* **Kontext:** Eftersom frontend genomgick stora och snabba förändringar baserat på inkommen feedback, behövde även backend anpassa sin struktur och sina ändpunkter parallellt (t.ex. med den nya rebalanseringslogiken på kontonivå).
* **Beslut:** Vissa vyer och funktioner (såsom den fullständiga sammankopplingen mellan målallokering under Inställningar, Innehavssidan och dynamiska alerts) drivs i dagsläget helt eller delvis av **mockad data**.
* **Motivering:** Detta gör att vi kan demonstrera den tänkta UI/UX-upplevelsen, komponenternas visningsläge och det kompletta användarflödet i frontend, även om den bakomliggande API-integrationen inte är helt ihopkopplad hela vägen i mål.
