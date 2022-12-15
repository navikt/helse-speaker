INSERT INTO varsel_tittel(varselkode_ref, tittel) VALUES
    ((SELECT id FROM varselkode WHERE kode = 'RV_IM_4'), 'Det er mottatt flere inntektsmeldinger på samme skjæringstidspunkt. Undersøk at arbeidsgiverperioden, sykepengegrunnlaget og refusjonsopplysningene er riktige.');
INSERT INTO varsel_forklaring(varselkode_ref, forklaring) VALUES
    ((SELECT id FROM varselkode WHERE kode = 'RV_IM_4'), 'Det har kommet inn flere inntektsmeldinger. Speil håndterer endringer i refusjon, men ikke tilfeller der inntekt eller arbeidsgiverperiode er endret. ');
INSERT INTO varsel_handling(varselkode_ref, handling) VALUES
    ((SELECT id FROM varselkode WHERE kode = 'RV_IM_4'), 'Du må undersøke om det er oppgitt endringer i inntekt, refusjon eller arbeidsgiverperiode som skal legges til grunn i saken og/ eller om forslaget til vedtak er riktig. Hvis det må gjøres endringer må du undersøke om saken kan revurderes i Speil, eller om saken må tas i Infotrygd.');
