import { atom } from 'recoil';
import { Varsel } from '../App';

export const varslerState = atom({
    key: 'varslerState',
    default: [] as Varsel[],
});
