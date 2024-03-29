import { atom, selector } from 'recoil';
import { Bruker, Subdomene, Varsel } from '../types';

export const varslerState = atom({
    key: 'varslerState',
    default: [] as Varsel[],
});

export const søkState = atom<string>({
    key: 'søkState',
    default: '',
});

export const brukerState = atom<Bruker | undefined>({
    key: 'brukerState',
    default: undefined,
});

export const teammedlemmerState = atom<Bruker[]>({
    key: 'teammedlemmerState',
    default: [],
});

export const subdomenerOgKonteksterState = atom<Subdomene[]>({
    key: 'subdomenerOgKonteksterState',
    default: [],
});

export const velgbareTeammeldemmerState = selector({
    key: 'velgbareTeammeldemmerState',
    get: ({ get }) => {
        const gjeldendeBruker = get(brukerState);
        const teammedlemmer = get(teammedlemmerState);

        if (!gjeldendeBruker || !teammedlemmer) return [];

        return teammedlemmer.filter((it) => it.oid != gjeldendeBruker.oid);
    },
});

export const søkbareVarslerState = selector({
    key: 'søkbareVarslerState',
    get: ({ get }) => {
        const filter = get(søkState).toLowerCase();
        const list = get(varslerState);

        if (filter === '') return list;

        return list.filter((it) => {
            return (
                it.varselkode.toLowerCase().includes(filter) ||
                it.tittel.toLowerCase().includes(filter) ||
                it.forklaring?.toLowerCase().includes(filter) === true ||
                it.handling?.toLowerCase().includes(filter) === true
            );
        });
    },
});
