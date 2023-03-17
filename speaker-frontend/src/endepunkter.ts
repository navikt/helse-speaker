import { Bruker, Kontekst, Subdomene, Varsel } from './types';

type VarselPayload = Omit<Varsel, 'opprettet'>;
type SubdomenePayload = Omit<Subdomene, 'kontekster'>;
type KontekstPayload = Kontekst & {
    subdomene: string;
};

export const postOppdaterVarsel = (varsel: VarselPayload) =>
    fetch('/api/varsler/oppdater', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(varsel),
    });

export const fetchVarsler = () =>
    fetch('/api/varsler')
        .then((response) => response.json())
        .then((data) => data as Varsel[]);
export const fetchSubdomenerOgKontekster = () =>
    fetch('/api/varsler/subdomener-og-kontekster')
        .then((response) => response.json())
        .then((data) => data as Subdomene[]);

export const fetchNesteVarselkode = (subdomene: string, kontekst: string) =>
    fetch(`/api/varsler/generer-kode?subdomene=${subdomene}&kontekst=${kontekst}`)
        .then((response) => response.text())
        .then((data) => data as string);

export const postLagreVarsel = (varsel: VarselPayload) =>
    fetch('/api/varsler/opprett', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(varsel),
    });

export const postNyttSubdomene = (subdomene: SubdomenePayload) =>
    fetch('/api/varsler/nytt-subdomene', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(subdomene),
    });

export const postNyKontekst = (kontekst: KontekstPayload) =>
    fetch('/api/varsler/ny-kontekst', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(kontekst),
    });

export const fetchUser = () =>
    fetch('/api/bruker', {})
        .then((response) => response.json())
        .then((data) => data as Bruker);
