import { VarselComponent } from './Varsel';
import { fetchVarsler } from '../endepunkter';
import React, { useEffect } from 'react';
import { useRecoilState, useRecoilValue } from 'recoil';
import { søkbareVarslerState, varslerState } from '../state/state';

export const Varsler = () => {
    const [, setVarsler] = useRecoilState(varslerState);
    const søkbareVarsler = useRecoilValue(søkbareVarslerState);

    useEffect(() => {
        fetchVarsler().then((varsler) => {
            setVarsler(varsler);
        });
    }, []);

    return (
        <div className={'p-4 pt-0'}>
            {søkbareVarsler.length > 0 &&
                søkbareVarsler.map((varsel) => <VarselComponent key={varsel.varselkode} varsel={varsel} />)}
        </div>
    );
};
