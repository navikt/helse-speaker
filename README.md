# Speaker
![Bygg og deploy app](https://github.com/navikt/helse-speaker/workflows/Speaker/badge.svg)

## Beskrivelse

App som lytter på SSE apiet til Sanity og publiserer varseldefinisjoner til Kafka.
Downstream leses og caches disse av Spesialist, som igjen server tekstene til Speil.

## Oppgradering av gradle wrapper
Finn nyeste versjon av gradle her: https://gradle.org/releases/

Kjør denne to ganger (ja, den må kjøres to ganger 🤷, bare se [dokumentasjonen til Gradle](https://docs.gradle.org/current/userguide/gradle_wrapper.html#sec:upgrading_wrapper)):
```./gradlew wrapper --gradle-version $gradleVersjon```

## Henvendelser
Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

### For NAV-ansatte
Interne henvendelser kan sendes via Slack i kanalen ![#team-bømlo-værsågod](https://nav-it.slack.com/archives/C019637N90X).
