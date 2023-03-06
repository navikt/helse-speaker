export declare type Varsel = {
    varselkode: string;
    tittel: string;
    forklaring?: string | null;
    handling?: string | null;
    avviklet: boolean;
};

export declare type Bruker = {
    epostadresse: string;
    navn: string;
    ident: string;
};
