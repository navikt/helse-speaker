CREATE TABLE subdomene
(
    løpenummer  SERIAL PRIMARY KEY NOT NULL,
    id          uuid UNIQUE        NOT NULL default gen_random_uuid(),
    navn        VARCHAR UNIQUE     NOT NULL,
    forkortelse VARCHAR UNIQUE     NOT NULL
);

CREATE TABLE kontekst
(
    løpenummer    SERIAL PRIMARY KEY             NOT NULL,
    id            uuid UNIQUE                    NOT NULL DEFAULT gen_random_uuid(),
    navn          VARCHAR                        NOT NULL,
    forkortelse   VARCHAR                        NOT NULL,
    subdomene_ref uuid REFERENCES subdomene (id) NOT NULL,
    UNIQUE (forkortelse, subdomene_ref),
    UNIQUE (navn, subdomene_ref)
);

INSERT INTO subdomene(navn, forkortelse)
VALUES ('Saksbehandling', 'SB'),
       ('Regelverk', 'RV');

INSERT INTO kontekst(forkortelse, navn, subdomene_ref)
VALUES ('SY', 'Sykmelding', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'RV')),
       ('SØ', 'Søknad', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'RV')),
       ('VV', 'Vilkårsvurdering', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'RV')),
       ('OO', 'Out of order', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'RV')),
       ('IM', 'Inntektsmelding', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'RV')),
       ('ST', 'Sykdomstidslinje', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'RV')),
       ('RE', 'Refusjon', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'RV')),
       ('IT', 'Infotrygd', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'RV')),
       ('SI', 'Simulering', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'RV')),
       ('OV', 'Opptjeningsvurdering', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'RV')),
       ('MV', 'Medlemskapsvurdering', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'RV')),
       ('IV', 'Inntektsvurdering', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'RV')),
       ('SV', 'Sykepengegrunnlagsvurdering', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'RV')),
       ('AY', 'Andre ytelser', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'RV')),
       ('UT', 'Utbetaling', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'RV')),
       ('OS', 'Oppdragsystemet', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'RV')),
       ('RV', 'Revurdering', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'RV')),
       ('VT', 'Vedtaksperiodetilstand', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'RV')),
       ('AG', 'Arbeidsgiver', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'RV')),
       ('AN', 'Annet', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'RV'));

INSERT INTO kontekst(forkortelse, navn, subdomene_ref)
VALUES ('RV', 'Risikovurdering', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'SB')),
       ('BO', 'Beslutteroppgave', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'SB')),
       ('EX', 'Eksterne systemer', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'SB')),
       ('IK', 'Inngangskriterier', (SELECT id FROM subdomene WHERE subdomene.forkortelse = 'SB'));
