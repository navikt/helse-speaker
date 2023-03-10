UPDATE varsel_definisjon
SET tittel='Utbetaling opphører tidligere utbetaling. Kontroller simuleringen',
    forklaring=null,
    handling=null
where varselkode_ref = (SELECT id FROM varselkode WHERE kode = 'RV_UT_21');

INSERT INTO varsel_definisjon (varselkode_ref, tittel, forklaring, handling)
VALUES (
        (SELECT id FROM varselkode WHERE kode = 'RV_UT_21'),
        'Utbetalingen opphører tidligere utbetaling.',
        'På grunn av endring i for eksempel arbeidsgiverperiode eller andre årsaker beregner vi én sammenhengende utbetalingsak hvor det tidligere har vært flere.',
        'Kontakt noen i Klynge Bømlo, eksempelvis produktleder eller teknisk leder. Sakene må behandles i samråd med noen i utviklingsteamet.'
        );