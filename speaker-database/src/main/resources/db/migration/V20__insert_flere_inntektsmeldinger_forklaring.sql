INSERT INTO varsel_definisjon(varselkode_ref, tittel, forklaring, handling) VALUES
    ((SELECT id FROM varselkode WHERE kode = 'RV_IM_4'),
     'Det er mottatt flere inntektsmeldinger på samme skjæringstidspunkt. Undersøk at arbeidsgiverperioden, sykepengegrunnlaget og refusjonsopplysningene er riktige.',
     'Det har kommet inn flere inntektsmeldinger. Speil håndterer endringer i refusjon og inntekt, men ikke tilfeller der arbeidsgiverperiode er endret.',
     'Du må undersøke om det er oppgitt endringer i inntekt, refusjon eller arbeidsgiverperiode som skal legges til grunn i saken og/ eller om forslaget til vedtak er riktig. Hvis det må gjøres endringer må du undersøke om saken kan revurderes i Speil, eller om saken må tas i Infotrygd.');
