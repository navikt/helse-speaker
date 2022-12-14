INSERT INTO varselkode(kode, avviklet) VALUES
                                           ('RV_SØ_1', false),
                                           ('RV_SØ_2', false),
                                           ('RV_SØ_3', false),
                                           ('RV_SØ_4', false),
                                           ('RV_SØ_5', false),
                                           ('RV_SØ_6', true),
                                           ('RV_SØ_7', false),
                                           ('RV_SØ_8', false),
                                           ('RV_SØ_9', false),
                                           ('RV_SØ_10', false),
                                           ('RV_OO_1', false),
                                           ('RV_OO_2', false),
                                           ('RV_IM_1', false),
                                           ('RV_IM_2', false),
                                           ('RV_IM_3', false),
                                           ('RV_IM_4', false),
                                           ('RV_IM_5', false),
                                           ('RV_IM_6', false),
                                           ('RV_IM_7', false),
                                           ('RV_RE_1', false),
                                           ('RV_IT_1', false),
                                           ('RV_IT_2', true),
                                           ('RV_IT_3', false),
                                           ('RV_IT_4', false),
                                           ('RV_IT_5', true),
                                           ('RV_VV_1', false),
                                           ('RV_VV_2', false),
                                           ('RV_VV_3', true),
                                           ('RV_VV_4', false),
                                           ('RV_VV_5', false),
                                           ('RV_VV_8', false),
                                           ('RV_VV_9', false),
                                           ('RV_OV_1', false),
                                           ('RV_OV_2', true),
                                           ('RV_MV_1', false),
                                           ('RV_MV_2', false),
                                           ('RV_IV_1', false),
                                           ('RV_IV_2', false),
                                           ('RV_IV_3', false),
                                           ('RV_SV_1', false),
                                           ('RV_SV_2', false),
                                           ('RV_AY_3', false),
                                           ('RV_AY_4', false),
                                           ('RV_AY_5', false),
                                           ('RV_AY_6', false),
                                           ('RV_AY_7', false),
                                           ('RV_AY_8', false),
                                           ('RV_AY_9', false),
                                           ('RV_SI_1', false),
                                           ('RV_SI_2', false),
                                           ('RV_UT_1', false),
                                           ('RV_UT_2', false),
                                           ('RV_UT_3', false),
                                           ('RV_UT_4', false),
                                           ('RV_OS_1', false),
                                           ('RV_OS_2', false),
                                           ('RV_OS_3', false),
                                           ('RV_RV_1', false)
ON CONFLICT(kode) DO NOTHING;

INSERT INTO varsel_tittel(varselkode_ref, tittel) VALUES
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_1'), 'Søknaden inneholder permittering. Vurder om permittering har konsekvens for rett til sykepenger'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_2'), 'Minst én dag er avslått på grunn av foreldelse. Vurder å sende vedtaksbrev fra Infotrygd'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_3'), 'Sykmeldingen er tilbakedatert, vurder fra og med dato for utbetaling.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_4'), 'Utdanning oppgitt i perioden i søknaden.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_5'), 'Søknaden inneholder Permisjonsdager utenfor sykdomsvindu'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_6'), 'Søknaden inneholder egenmeldingsdager etter sykmeldingsperioden'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_7'), 'Søknaden inneholder Arbeidsdager utenfor sykdomsvindu'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_8'), 'Utenlandsopphold oppgitt i perioden i søknaden.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_9'), 'Det er oppgitt annen inntektskilde i søknaden. Vurder inntekt.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_10'), 'Den sykmeldte har oppgitt å ha andre arbeidsforhold med sykmelding i søknaden.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_OO_1'), 'Det er behandlet en søknad i Speil for en senere periode enn denne.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_OO_2'), 'Saken må revurderes fordi det har blitt behandlet en tidligere periode som kan ha betydning.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_IM_1'), 'Vi har mottatt en inntektsmelding i en løpende sykmeldingsperiode med oppgitt første/bestemmende fraværsdag som er ulik tidligere fastsatt skjæringstidspunkt.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_IM_2'), 'Første fraværsdag i inntektsmeldingen er ulik skjæringstidspunktet. Kontrollér at inntektsmeldingen er knyttet til riktig periode.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_IM_3'), 'Inntektsmeldingen og vedtaksløsningen er uenige om beregningen av arbeidsgiverperioden. Undersøk hva som er riktig arbeidsgiverperiode.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_IM_4'), 'Mottatt flere inntektsmeldinger - den første inntektsmeldingen som ble mottatt er lagt til grunn. Utbetal kun hvis det blir korrekt.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_IM_5'), 'Sykmeldte har oppgitt ferie første dag i arbeidsgiverperioden.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_IM_6'), 'Inntektsmelding inneholder ikke beregnet inntekt'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_IM_7'), 'Brukeren har opphold i naturalytelser'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_RE_1'), 'Fant ikke refusjonsgrad for perioden. Undersøk oppgitt refusjon før du utbetaler.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_IT_1'), 'Det er utbetalt en periode i Infotrygd etter perioden du skal behandle nå. Undersøk at antall forbrukte dager og grunnlag i Infotrygd er riktig'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_IT_2'), 'Perioden er lagt inn i Infotrygd, men ikke utbetalt. Fjern fra Infotrygd hvis det utbetales via speil.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_IT_3'), 'Utbetaling i Infotrygd overlapper med vedtaksperioden'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_IT_4'), 'Det er registrert utbetaling på nødnummer'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_IT_5'), 'Mangler inntekt for første utbetalingsdag i en av infotrygdperiodene'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_VV_1'), 'Arbeidsgiver er ikke registrert i Aa-registeret.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_VV_2'), 'Flere arbeidsgivere, ulikt starttidspunkt for sykefraværet eller ikke fravær fra alle arbeidsforhold'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_VV_3'), 'Første utbetalingsdag er i Infotrygd og mellom 1. og 16. mai. Kontroller at riktig grunnbeløp er brukt.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_VV_4'), 'Minst én dag uten utbetaling på grunn av sykdomsgrad under 20 %. Vurder å sende vedtaksbrev fra Infotrygd'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_VV_5'), 'Bruker mangler nødvendig inntekt ved validering av Vilkårsgrunnlag'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_VV_8'), 'Den sykmeldte har skiftet arbeidsgiver, og det er beregnet at den nye arbeidsgiveren mottar refusjon lik forrige. Kontroller at dagsatsen blir riktig.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_VV_9'), 'Bruker er fortsatt syk 26 uker etter maksdato'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_OV_1'), 'Perioden er avslått på grunn av manglende opptjening'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_OV_2'), 'Opptjeningsvurdering må gjøres manuelt fordi opplysningene fra AA-registeret er ufullstendige'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_MV_1'), 'Vurder lovvalg og medlemskap'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_MV_2'), 'Perioden er avslått på grunn av at den sykmeldte ikke er medlem av Folketrygden'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_IV_1'), 'Bruker har flere inntektskilder de siste tre månedene enn arbeidsforhold som er oppdaget i Aa-registeret.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_IV_2'), 'Har mer enn 25 % avvik. Dette støttes foreløpig ikke i Speil. Du må derfor annullere periodene.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_IV_3'), 'Fant frilanserinntekt på en arbeidsgiver de siste 3 månedene'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_SV_1'), 'Perioden er avslått på grunn av at inntekt er under krav til minste sykepengegrunnlag'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_SV_2'), 'Minst en arbeidsgiver inngår ikke i sykepengegrunnlaget'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_AY_3'), 'Bruker har mottatt AAP innenfor 6 måneder før skjæringstidspunktet. Kontroller at brukeren har rett til sykepenger'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_AY_4'), 'Bruker har mottatt dagpenger innenfor 4 uker før skjæringstidspunktet. Kontroller om bruker er dagpengemottaker. Kombinerte ytelser støttes foreløpig ikke av systemet'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_AY_5'), 'Det er utbetalt foreldrepenger i samme periode.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_AY_6'), 'Det er utbetalt pleiepenger i samme periode.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_AY_7'), 'Det er utbetalt omsorgspenger i samme periode.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_AY_8'), 'Det er utbetalt opplæringspenger i samme periode.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_AY_9'), 'Det er institusjonsopphold i perioden. Vurder retten til sykepenger.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_SI_1'), 'Feil under simulering'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_SI_2'), 'Simulering av revurdert utbetaling feilet. Utbetalingen må annulleres'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_UT_1'), 'Utbetaling av revurdert periode ble avvist av saksbehandler. Utbetalingen må annulleres'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_UT_2'), 'Utbetalingen ble gjennomført, men med advarsel'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_UT_3'), 'Feil ved utbetalingstidslinjebygging'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_UT_4'), 'Finner ingen utbetaling å annullere'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_OS_1'), 'Utbetalingen forlenger et tidligere oppdrag som opphørte alle utbetalte dager. Sjekk simuleringen.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_OS_2'), 'Utbetalingens fra og med-dato er endret. Kontroller simuleringen'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_OS_3'), 'Endrer tidligere oppdrag. Kontroller simuleringen.'),
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_RV_1'), 'Denne perioden var tidligere regnet som innenfor arbeidsgiverperioden')
;

INSERT INTO varsel_forklaring(varselkode_ref, forklaring) VALUES
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_1'), 'Bruker har oppgitt permittering på søknad om sykepenger'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_2'), 'Søknaden har kommet inn mer enn 3 måneder før dagen/dagene.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_3'), 'Sykmeldingen er tilbakedatert.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_4'), null),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_5'), null),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_6'), 'Det foreligger egenmeldingsdager etter sykmeldingsperioden'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_7'), null),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_8'), 'Det er oppgitt utenlandsopphold utenfor EØS i søknaden'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_9'), 'Den sykmeldte har oppgitt «Annet» som annen inntektskilde i søknaden.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_10'), 'Ved mottak av søknaden kjente vi bare til sykdom hos én arbeidsgiver.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_OO_1'), 'Søknadene har ikke kommet i vanlig rekkefølge. Dette kan skje dersom den sykmeldte avbryter en søknad/sykmelding, eller en lege skriver ut en tilbakedatert sykmelding. Perioder som allerede er avsluttet vil revurderes når denne perioden godkjennes.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_OO_2'), 'Det har blitt behandlet en søknadsperiode som gjelder et tidligere tidsrom. Dette kan medføre at antall forbrukte dager er endret eller at opptjening av ny rett til sykepenger påvirkes.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_IM_1'), 'Det kan bety at den sykmeldte har gjenopptatt arbeidet og så blitt sykmeldt igjen. Det kan derfor være et nytt skjæringstidspunkt.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_IM_2'), 'Første fraværsdag i inntektsmeldingen er ulik første fraværsdag i sykdomsperioden:'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_IM_3'), 'Arbeidsgiver og vedtaksløsningen har beregnet arbeidsgiverperioden ulikt.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_IM_4'), null),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_IM_5'), null),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_IM_6'), null),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_IM_7'), null),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_RE_1'), 'Fant ikke informasjon om det er refusjon i perioden. Refusjonen er satt til 100 % siden det samsvarer med siste utbetalte periode i Infotrygd.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_IT_1'), null),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_IT_2'), 'Det ligger registrert en søknad om sykepenger på SP UB.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_IT_3'), 'Denne sakstypen tas normalt ikke inn i Speil. Det kan ha skjedd en endring fra saken ble behandlet første gang til den nå skal revurderes.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_IT_4'), 'Denne sakstypen tas normalt ikke inn i Speil. Det kan ha skjedd en endring fra saken ble behandlet første gang til den nå skal revurderes.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_IT_5'), 'Denne sakstypen tas normalt ikke inn i Speil. Det kan ha skjedd en endring fra saken ble behandlet første gang til den nå skal revurderes.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_VV_1'), 'Arbeidsgiver på sykmelding eller inntektsmelding er ikke registrert i Aa-registeret.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_VV_2'), 'Det tidligste sykefraværet bestemmer skjæringstidspunktet. Dette gjelder: Inntekt på inntektsmeldingen som kommer fra arbeidsgivere med senere starttidspunkt kan gjelde feil periode og/eller inntekten som er foreslått for minst et av arbeidsforholdene er innhentet fra a-ordningen og er gjennomsnittet av rapportert inntekt de tre siste månedene før tidspunktet for arbeidsuførhet (§8-28 3. ledd bokstav a).'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_VV_3'), 'Vi klarer ikke å lese riktig skjæringstidspunkt når saken har startet i Infotrygd. Vi må kontrollere at riktig grunnbeløp er brukt i saker med sykepengegrunnlag over 6G.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_VV_4'), 'Minst én dag uten utbetaling på grunn av sykdomsgrad under 20'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_VV_5'), null),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_VV_8'), 'Den sykmeldte har skiftet arbeidsgiver i løpet av sykefraværet.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_VV_9'), null),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_OV_1'), 'Alle dager er avvist på grunn av at personen ikke har jobbet mer enn fire uker før sykmeldingstidspunktet'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_OV_2'), null),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_MV_1'), 'Medlemskapssjekken har ikke klart å entydig konkludere med at bruker er medlem'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_MV_2'), 'Alle dager er avvist på grunn av at personen ikke er medlem av Folketrygden'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_IV_1'), 'Bruker kan være frilanser.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_IV_2'), 'Det er lagt inn en ny inntekt som gir 25 % avvik. Skjønnsmessig fastsettelse av sykepengegrunnlaget støttes ikke i Speil.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_IV_3'), null),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_SV_1'), 'Alle dager er avvist på grunn av at sykepengegrunnlaget er under minstekrav'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_SV_2'), null),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_AY_3'), 'Bruker har mottatt AAP innenfor 6 måneder før skjæringstidspunktet'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_AY_4'), 'Bruker har mottatt dagpenger innenfor 4 uker før skjæringstidspunktet'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_AY_5'), 'Det er utbetalt foreldrepenger i Infotrygd etter at perioden ble godkjent i Speil.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_AY_6'), 'Det er utbetalt pleiepenger i Infotrygd etter at perioden ble godkjent i Speil.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_AY_7'), 'Det er utbetalt omsorgspenger i Infotrygd etter at perioden ble godkjent i Speil.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_AY_8'), 'Det er utbetalt opplæringspenger i Infotrygd etter at perioden ble godkjent i Speil.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_AY_9'), 'Vi henter inn opplysninger om institusjonsopphold. Det har kommet informasjon om at den sykmeldte oppholdt seg i institusjon (sykehus eller fengsel) i perioden.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_SI_1'), null),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_SI_2'), null),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_UT_1'), null),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_UT_2'), null),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_UT_3'), null),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_UT_4'), null),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_OS_1'), null),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_OS_2'), null),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_OS_3'), 'Det er opphørt en tidligere linje i Oppdrag. Dette skjer dersom en FOM-dato på en linje endrer seg.'),
                                                              ((SELECT id FROM varselkode WHERE kode = 'RV_RV_1'), null)
;

INSERT INTO varsel_handling(varselkode_ref, handling) VALUES
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_1'), 'Kontrollér at permitteringen ikke påvirker sykepengerettighetene'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_2'), 'Hvis det ikke finnes noe unntak fra foreldelsesfristen skal du godkjenne i Speil og sende vedtaksbrev fra Infotrygd. Hvis forslaget er feil må du avvise saken i Speil og behandle i Infotrygd. Dersom dagen/dagene som er avslått er innenfor arbeidsgiverperioden trenger du ikke å sende avslag fra Infotrygd.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_3'), 'Hvis tilbakedateringen er godkjent, skal du finne notat i Gosys eller stå i fritekstfelt på SP-UB. Da kan du godkjenne saken i Speil. Hvis tilbakedateringen er under vurdering, skal det være en åpen Gosys-oppgave. I slike tilfeller skal du legge saken på vent til vurderingen er ferdig. Hvis tilbakedateringen er avslått, skal du avvise saken i Speil og sende vedtak. Registerer avslaget i SP-SA. Husk også å lukke oppgaven på søknaden/inntektsmeldingen i Gosys.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_4'), null),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_5'), null),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_6'), 'Egenmeldinger kan ikke benyttes rett etter en sykmelding, sjekk arbeidsgiverperioden og utbetalingsoversikten.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_7'), null),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_8'), 'Undersøk om det er vedtak om beholde sykepenger under utenlandsopphold. Hvis det er søkt uten at det er behandlet - legg saken på vent til det foreligger vedtak i saken. Korriger dager dersom det er gitt avslag - og godkjenn saken i ny løsning. Hvis brukeren har fått vedtak fra NAV-kontoret, trenger du ikke å sende nytt vedtak.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_9'), 'Sjekk hva den andre inntektskilden består av og vurder om saken kan behandles i Speil. Hvis saken ikke kan behandles i Speil, må du behandle saken i Infotrygd.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_10'), 'Hvis sykepengegrunnlaget er fastsatt feil, eller det er sykmelding for flere arbeidsforhold som ikke er lagt til grunn, må saken behandles i Infotrygd.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_OO_1'), 'Undersøk at behandlingen av perioden blir riktig.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_OO_2'), 'Sjekk at telling av forbrukte dager og/eller opptjening av ny rett er riktig beregnet. Dersom revurderingen gjør at det er for mye utbetalt til den sykmeldte, se egne rutiner for håndtering av feilutbetalinger.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_IM_1'), 'Undersøk om arbeidet er gjenopptatt slik at sykmeldingen avbrytes. Det må avklares om det blir et nytt skjæringstidspunkt. Hvis det blir nytt skjæringstidspunkt, må perioden med det nye skjæringstidspunktet avvises i vedtaksløsningen og saken må behandles i Infotrygd.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_IM_2'), 'Kontroller at skjæringstidspunktet er riktig.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_IM_3'), 'Vurder hva som er riktig arbeidsgiverperiode. Hvis forslaget til vedtaksløsningen er feil, må saken behandles i Infotrygd.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_IM_4'), null),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_IM_5'), null),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_IM_6'), null),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_IM_7'), null),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_RE_1'), 'Undersøk hva som er oppgitt i siste inntektsmelding og eventuell informasjon i Infotrygd. Dersom refusjonen er satt feil må perioden behandles i Infotrygd.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_IT_1'), null),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_IT_2'), 'Dette varselet betyr ikke at perioden må behandles i Infotrygd. Fjern perioden fra SP UB når du utbetaler via speil. Hvis inntekten er registert så kan den slettes på SP-VT. Hvis det er en forutgående periode i Infotrygd som enda ikke er behandlet, trenger du ikke å avvise perioden hvis det er flere sykepengedager igjen er enn det som ligger til behandling i Infotrygd.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_IT_3'), 'Undersøk om saken kan revurderes i Speil og at utbetalingen blir riktig. Hvis saken ikke kan revurderes i Speil, må du annullere og behandle saken i Infotrygd.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_IT_4'), 'Undersøk om saken kan revurderes i Speil og at utbetalingen blir riktig. Hvis saken ikke kan revurderes i Speil, må du annullere og behandle saken i Infotrygd.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_IT_5'), 'Undersøk om saken kan revurderes i Speil og at utbetalingen blir riktig. Hvis saken ikke kan revurderes i Speil, må du annullere og behandle saken i Infotrygd.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_VV_1'), 'Sjekk at bruker er ansatt hos arbeidsgiveren.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_VV_2'), 'Du må fastsette et sykepengegrunnlag basert på inntekt fra alle arbeidsforholdene. Du må vurdere om de foreslåtte arbeidsforholdene er reelle og skal tas med i beregningen.  Hvis det er kommet inntektsmelding fra arbeidsgiver med senere starttidspunkt, må det sjekkes om inntekten for dette arbeidsforholdet er riktig beregnet (§8-28). Hvis det ikke er kommet inntektsmelding fra denne arbeidsgiveren, må den foreslåtte inntekten vurderes. Du må vurdere om den innhentede inntekten er riktig. Sjekk om det er et nytt arbeidsforhold eller varig lønnsendring i beregningsperioden. Sjekk om det variasjoner i inntekten som kan bety at det er lovlig fravær uten lønn i beregningsperioden. Vurderes etter §8-28.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_VV_3'), 'Kontroller at dagsatsen samsvarer med grunnbeløpet som skal brukes.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_VV_4'), 'Dersom du er enig i vurderingen, send avslagsbrev fra Infotrygd og godkjenn i ny løsning. OBS!!: Hør med den som er sykmeldt hvis det er mistanke om misforståelser ved utfylling av søknaden. Da kan du sende spørsmål i Modia og legge saken på vent til du får svar.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_VV_5'), null),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_VV_8'), 'Sjekk at riktig dagsats er lagt til grunn.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_VV_9'), null),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_OV_1'), 'Undersøk at opptjeningen er vurdert riktig. Hvis opptjening ikke er oppfylt kan saken godkjennes i speil og det må sendes avslagsbrev i Infotrygd.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_OV_2'), null),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_MV_1'), 'Sjekk om personen er medlem i folketrygden – se i medlemskapsfanen i Gosys. Sjekk bostedsadressen og hvor arbeidet utføres. Hvis bokommune er 0393 eller 2101 skal saken avvises og behandles av NAV Utland. Kilder: Gosys og Aa-registeret.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_MV_2'), 'Undersøk om det er riktig at personen ikke er medlem av Folketrygden. Hvis personen ikke er medlem kan saken godkjennes i speil og det må sendes vedtak i Infotrygd.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_IV_1'), 'Sjekk om bruker er kombinert arbeidstaker og frilanser. Hvis ja, send saken til Infotrygd.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_IV_2'), 'Du kan endre inntekten en gang til hvis du har gjort noe feil. Dersom endringen skal gjennomføres må periodene annulleres i Speil og behandles i Infotrygd.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_IV_3'), null),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_SV_1'), 'Undersøk at sykepengegrunnlaget er korrekt. Hvis det er fastsatt korrekt kan saken godkjennes i speil, da må avslagsbrev sendes i infotrygd.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_SV_2'), null),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_AY_3'), 'Kontrollér ny opptjeningstid etter maksdato'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_AY_4'), 'Sjekk om bruker er dagpengemottaker på sykmeldingstidspunktet. Hvis ja kan ikke saken behandles i dette systemet ennå.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_AY_5'), 'Undersøk hvilken ytelse som er riktig. Hvis utbetalingene i Speil må avslås på grunn av andre ytelser må du annullere og behandle i Infotrygd.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_AY_6'), 'Undersøk hvilken ytelse som er riktig. Hvis utbetalingene i Speil må avslås på grunn av andre ytelser må du annullere og behandle i Infotrygd.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_AY_7'), 'Undersøk hvilken ytelse som er riktig. Hvis utbetalingene i Speil må avslås på grunn av andre ytelser må du annullere og behandle i Infotrygd.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_AY_8'), 'Undersøk hvilken ytelse som er riktig. Hvis utbetalingene i Speil må avslås på grunn av andre ytelser må du annullere og behandle i Infotrygd.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_AY_9'), 'Undersøk om institusjonsoppholdet er forenlig med å motta sykepenger. Dersom en periode skal avslås på grunn av institusjonsopphold må saken annulleres og behandles i Infotrygd.'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_SI_1'), null),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_SI_2'), null),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_UT_1'), null),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_UT_2'), null),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_UT_3'), null),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_UT_4'), null),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_OS_1'), null),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_OS_2'), null),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_OS_3'), 'Undersøk om perioden blir riktig simulert, og ta en sjekk på oppdraget i ettertid'),
                                                          ((SELECT id FROM varselkode WHERE kode = 'RV_RV_1'), null)
;