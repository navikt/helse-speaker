import React, { useEffect } from 'react';
import { Header as DSHeader } from '@navikt/ds-react-internal';
import { useRecoilState } from 'recoil';
import { brukerState } from '../state/state';
import { fetchBruker } from '../endepunkter';
import { Søk } from './Søk';

export const Header = () => {
    const [bruker, setBruker] = useRecoilState(brukerState);

    useEffect(() => {
        fetchBruker().then((bruker) => setBruker(bruker));
    }, []);

    return (
        <DSHeader className={'flex w-full'}>
            <DSHeader.Title as={'h1'}>Speaker</DSHeader.Title>
            <Søk />
            <DSHeader.User name={bruker?.navn ?? 'Ikke innlogget'} description={bruker?.ident} />
        </DSHeader>
    );
};
