CREATE TABLE varseldefinisjon
(
    løpenummer SERIAL PRIMARY KEY NOT NULL,
    referanse  UUID UNIQUE        NOT NULL DEFAULT gen_random_uuid(),
    varselkode VARCHAR UNIQUE     NOT NULL,
    json       jsonb              NOT NULL
);