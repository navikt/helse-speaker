UPDATE varsel_definisjon
SET forklaring = 'Det har kommet inn flere inntektsmeldinger. Speil håndterer endringer i refusjon og inntekt, men ikke tilfeller der arbeidsgiverperiode er endret.'
WHERE varselkode_ref = (SELECT id FROM varselkode WHERE kode = 'RV_IM_4');
