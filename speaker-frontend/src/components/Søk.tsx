import { Search } from '@navikt/ds-react';
import React from 'react';
import { søkState } from '../state/state';
import { useRecoilState } from 'recoil';

export const Søk = () => {
    const [, setSøk] = useRecoilState(søkState);
    return (
        <form className="self-center px-5 mr-auto" onSubmit={(e) => e.preventDefault()}>
            <Search
                label="header søk"
                size="small"
                variant="simple"
                placeholder="Søk"
                onChange={(value) => setSøk(value)}
            />
        </form>
    );
};
