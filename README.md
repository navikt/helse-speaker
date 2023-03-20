# Speaker
![Bygg og deploy app](https://github.com/navikt/helse-speaker/workflows/Speaker/badge.svg)

## Beskrivelse

Fire moduler, to runtimes

* To ansvarsområder:
  * Validere varselkoder som produseres av Spleis
  * Tilby oppretting av/endring av varsler/varseldefinisjoner gjennom brukergrensesnitt
    * dev: https://speaker.intern.dev.nav.no
    * prod: https://speaker.intern.nav.no

* kafka - egen runtime, følger med på `aktivitetslogg_ny_aktivitet` på rapid'en og validerer varselkoden mot
databasen
* database - holder på alle migreringsscript. Disse deles av kafka og backend
* frontend - inneholder all frontendkode
* backend - egen runtime, utfører operasjoner i databasen og server frontend 

## Utvikling lokalt

Slik gjør du:

1. Start `LocalApp` som ligger i testscope i `speaker-backend`
   * `LocalApp` bruker den ekte appen under panseret, men gjør i tillegg en del andre greier, som: 
     * starter opp sin egen postgres-database vha. testcontainers
     * starter opp `MockOAuth2Server` slik at autentisering ikke må spesialhåndteres lokalt
     * lager en json-fil ved navn `testtoken.json` på rot som inneholder et gyldig access token til `MockOauth2Server`

2. Start frontend - `npm run dev`
   * `vite.config.js` er konfigurert til å proxye alle `/api`-kall til `LocalApp`
   * `vite.config.js` leser inn `testtoken.json` fra rot og legger på `Authorization` header som `Bearer <token>`, slik at 
   man har tilgang til å gjøre operasjoner mot backend.
3. Happy coding! 🎉

## Oppgradering av gradle wrapper
Finn nyeste versjon av gradle her: https://gradle.org/releases/

```./gradlew wrapper --gradle-version $gradleVersjon```

Husk å oppdater gradle versjon i build.gradle.kts filen
```gradleVersion = "$gradleVersjon"```

## Protip for å kjøre tester raskere
Finn filen .testcontainers.properties, ligger ofte på hjemmeområdet ditt eks:

```~/.testcontainers.properties```

legg til denne verdien

```testcontainers.reuse.enable=true```

## Henvendelser
Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

### For NAV-ansatte
Interne henvendelser kan sendes via Slack i kanalen ![#team-bømlo-værsågod](https://nav-it.slack.com/archives/C019637N90X).
