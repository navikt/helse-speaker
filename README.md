# Speaker
![Bygg og deploy app](https://github.com/navikt/helse-speaker/workflows/Speaker/badge.svg)

## Beskrivelse

App som lytter på SSE-API-et til Sanity og publiserer varseldefinisjoner til Kafka.
Downstream leses og caches disse av Spesialist, som igjen server tekstene til Speil.

Lytteren kobler til på nytt når SSE-tilkoblingen avsluttes, med eller uten feil.
Feil logges før nytt forsøk. `/isalive` svarer med 503 hvis lytteren avsluttes uventet.

## Henvendelser
Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

### For NAV-ansatte
Interne henvendelser kan sendes via Slack i kanalen ![#team-sas-værsågod](https://nav-it.slack.com/archives/C019637N90X).
