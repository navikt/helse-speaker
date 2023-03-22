import { VarselComponent } from './Varsel';
import React from 'react';
import { useRecoilValue } from 'recoil';
import { søkbareVarslerState } from '../state/state';

export const Varsler = () => {
    const søkbareVarsler = useRecoilValue(søkbareVarslerState);

    return (
        <div className={'flex flex-col gap-4'}>
            {søkbareVarsler.length > 0 &&
                søkbareVarsler.map((varsel) => <VarselComponent key={varsel.varselkode} varsel={varsel} />)}
        </div>
    );
};
