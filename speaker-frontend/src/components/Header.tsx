import { Search } from '@navikt/ds-react';
import React, { useEffect, useState } from 'react';
import { Header as DSHeader } from '@navikt/ds-react-internal';
import { useRecoilState } from 'recoil';
import { brukerState } from '../state/state';
import { fetchUser } from '../endepunkter';

export const Header = () => {
    const [bruker, setBruker] = useRecoilState(brukerState);
    const [loading, setLoading] = useState(false);

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
            <form
                className="self-center px-5 mr-auto"
                onSubmit={(e) => {
                    e.preventDefault();
                    console.log('Search!');
                }}
            >
                <Search label="header søk" size="small" variant="simple" placeholder="Søk" />
            </form>
            <DSHeader.User name={bruker?.navn ?? 'Ikke innlogget'} description={bruker?.ident} />
        </DSHeader>
    );
};
