import { Loader } from '@navikt/ds-react';
import { VarselComponent } from './Varsel';
import { fetchVarsler } from '../endepunkter';
import React, { useEffect, useState } from 'react';
import { useRecoilState, useRecoilValue } from 'recoil';
import { søkbareVarslerState, varslerState } from '../state/state';

export const Varsler = () => {
    const [, setVarsler] = useRecoilState(varslerState);
    const [loading, setLoading] = useState(false);
    const søkbareVarsler = useRecoilValue(søkbareVarslerState);

    useEffect(() => {
        setLoading(true);
        fetchVarsler().then((varsler) => {
            setVarsler(varsler);
            setLoading(false);
        });
    }, []);

    return (
        <div className={'p-4 pt-0.5'}>
            {loading && <Loader size={'3xlarge'} title={'Laster varsler'} />}
            {søkbareVarsler.length > 0 &&
                søkbareVarsler.map((varsel) => <VarselComponent key={varsel.varselkode} varsel={varsel} />)}
        </div>
    );
};
