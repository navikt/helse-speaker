import React, { useEffect, useState } from 'react';
import { Header as DSHeader } from '@navikt/ds-react-internal';
import { useRecoilState } from 'recoil';
import { brukerState } from '../state/state';
import { fetchUser } from '../endepunkter';
import { Søk } from './Søk';

export const Header = () => {
    const [bruker, setBruker] = useRecoilState(brukerState);
    const [, setLoading] = useState(false);

    useEffect(() => {
        setLoading(true);
        fetchUser().then((bruker) => {
            setBruker(bruker);
            setLoading(false);
        });
    }, []);

    return (
        <DSHeader className={'flex w-full'}>
            <DSHeader.Title as={'h1'}>Speaker</DSHeader.Title>
            <Søk />
            <DSHeader.User name={bruker?.navn ?? 'Ikke innlogget'} description={bruker?.ident} />
        </DSHeader>
    );
};
