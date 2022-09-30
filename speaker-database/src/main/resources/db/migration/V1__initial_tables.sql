CREATE TABLE varselkode(
    id SERIAL NOT NULL PRIMARY KEY,
    kode VARCHAR NOT NULL UNIQUE,
    avviklet BOOLEAN NOT NULL default false,
    opprettet timestamptz NOT NULL default now(),
    endret timestamptz
);

CREATE TABLE varsel_tittel(
    id SERIAL NOT NULL PRIMARY KEY,
    varselkode_ref INT NOT NULL REFERENCES varselkode(id),
    tittel VARCHAR NOT NULL,
    opprettet timestamptz NOT NULL default now(),
    UNIQUE (varselkode_ref, tittel)
);

CREATE TABLE varsel_forklaring(
    id SERIAL NOT NULL PRIMARY KEY,
    varselkode_ref INT NOT NULL REFERENCES varselkode(id),
    forklaring VARCHAR NOT NULL,
    opprettet timestamptz NOT NULL default now(),
    UNIQUE (varselkode_ref, forklaring)
);

CREATE TABLE varsel_handling(
    id SERIAL NOT NULL PRIMARY KEY,
    varselkode_ref INT NOT NULL REFERENCES varselkode(id),
    handling VARCHAR NOT NULL,
    opprettet timestamptz NOT NULL default now(),
    UNIQUE (varselkode_ref, handling)
);