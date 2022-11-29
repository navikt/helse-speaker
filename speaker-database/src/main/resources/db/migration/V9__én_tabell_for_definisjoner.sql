CREATE TABLE varsel_definisjon(
    id BIGSERIAL PRIMARY KEY,
    varselkode_ref BIGINT NOT NULL REFERENCES varselkode(id),
    unik_id uuid NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    tittel VARCHAR NOT NULL,
    forklaring VARCHAR,
    handling VARCHAR,
    opprettet TIMESTAMP DEFAULT now(),
    endret TIMESTAMP DEFAULT null
);