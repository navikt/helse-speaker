CREATE TABLE bruker
(
    løpenummer   SERIAL  NOT NULL PRIMARY KEY,
    oid          uuid    NOT NULL UNIQUE,
    navn         VARCHAR NOT NULL,
    epostadresse VARCHAR NOT NULL,
    ident        VARCHAR NOT NULL
);

CREATE TABLE forfatter
(
    løpenummer     SERIAL NOT NULL PRIMARY KEY,
    definisjon_ref uuid   NOT NULL REFERENCES varsel_definisjon (unik_id),
    bruker_ref     uuid   NOT NULL REFERENCES bruker (oid)
);
