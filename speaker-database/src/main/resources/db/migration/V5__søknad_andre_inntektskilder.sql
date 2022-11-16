UPDATE varselkode set avviklet = true, endret = now() WHERE kode = 'RV_SØ_9';

INSERT INTO varsel_tittel(varselkode_ref, tittel) VALUES
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_10'), 'Den sykmeldte har fått et nytt arbeidsforhold.');
INSERT INTO varsel_forklaring(varselkode_ref, forklaring) VALUES
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_10'), 'Den sykmeldte har oppgitt i søknaden at han/hun har fått et nytt arbeidsforhold i sykmeldingsperioden.');
INSERT INTO varsel_handling(varselkode_ref, handling) VALUES
                                                      ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_10'), 'Du må sjekke om sykepengene skal graderes ut ifra inntekt i det nye arbeidsforholdet. Du må kontakte bruker for å få informasjon om inntekten og når den er opptjent. Ta stilling til om det bør sendes en oppgave til NAV-kontoret for vurdering av vilkårene i § 8-4.');
