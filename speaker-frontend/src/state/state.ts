import { atom } from 'recoil';
import { Bruker, Varsel } from '../types';

export const varslerState = atom({
    key: 'varslerState',
    default: [] as Varsel[],
});

export const brukerState = atom<Bruker | undefined>({
    key: 'brukerState',
    default: undefined,
});
