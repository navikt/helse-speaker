import { Varsel } from './App';

export const postOppdaterVarsel = (varsel: Varsel) =>
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
