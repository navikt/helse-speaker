import { Loader } from '@navikt/ds-react';
import { VarselComponent } from './Varsel';
import { fetchVarsler } from '../endepunkter';
import React, { useEffect, useState } from 'react';
import { useRecoilState } from 'recoil';
import { varslerState } from '../state/state';

export const Varsler = () => {
    const [varsler, setVarsler] = useRecoilState(varslerState);
    const [loading, setLoading] = useState(false);

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
            {varsler.length > 0 && varsler.map((varsel) => <VarselComponent key={varsel.varselkode} varsel={varsel} />)}
        </div>
    );
};
