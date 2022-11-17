INSERT INTO varsel_handling(varselkode_ref, handling) VALUES
    ((SELECT id FROM varselkode WHERE kode = 'RV_SØ_10'), 'Du må sjekke om sykepengene skal graderes ut ifra inntekt i det nye inntektsforholdet. Du må finne informasjon om inntekten og når den er opptjent. Ta stilling til om det bør sendes en oppgave til NAV-kontoret for vurdering av vilkårene i § 8-4.');
