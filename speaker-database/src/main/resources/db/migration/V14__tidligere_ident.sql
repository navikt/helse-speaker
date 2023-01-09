INSERT INTO varselkode(kode, avviklet) VALUES ('RV_AN_5', false);

INSERT INTO varsel_definisjon(varselkode_ref, tittel, forklaring, handling) VALUES
    ((SELECT id FROM varselkode WHERE kode = 'RV_AN_5'),
     'Personen har blitt behandlet på en tidligere ident',
     null,
     null);