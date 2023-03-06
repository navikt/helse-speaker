import { atom, selector } from 'recoil';
import { Bruker, Varsel } from '../types';

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

export const søkbareVarslerState = selector({
    key: 'søkbareVarslerState',
    get: ({ get }) => {
        const filter = get(søkState);
        const list = get(varslerState);

        if (filter === '') return list;

        return list.filter((it) => {
            return (
                it.varselkode.includes(filter) ||
                it.tittel.includes(filter) ||
                it.forklaring?.includes(filter) === true ||
                it.handling?.includes(filter) === true
            );
        });
    },
});
