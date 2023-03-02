INSERT INTO varselkode(kode, avviklet) VALUES ('RV_IM_22', false);

INSERT INTO varsel_definisjon(varselkode_ref, tittel, forklaring, handling) VALUES
    ((SELECT id FROM varselkode WHERE kode = 'RV_IM_22'),
     'Det er mottatt flere inntektsmeldinger på kort tid for samme arbeidsgiver',
     'Det har kommet inn flere inntektsmeldinger på kort tid',
     'Du må undersøke om det er oppgitt endringer i inntekt, refusjon eller arbeidsgiverperiode som skal legges til grunn i saken og/ eller om forslaget til vedtak er riktig. Hvis det må gjøres endringer må du undersøke om saken kan revurderes i Speil, eller om saken må tas i Infotrygd.'
     );