UPDATE varsel_definisjon
SET tittel='Utbetalingen opphører tidligere utbetaling.',
    forklaring='På grunn av endring i for eksempel arbeidsgiverperiode eller andre årsaker beregner vi én sammenhengende utbetalingsak hvor det tidligere har vært flere.',
    handling='Kontakt noen i Klynge Bømlo, eksempelvis produktleder eller teknisk leder. Sakene må behandles i samråd med noen i utviklingsteamet.'
where varselkode_ref = (SELECT id FROM varselkode WHERE kode = 'RV_UT_21');