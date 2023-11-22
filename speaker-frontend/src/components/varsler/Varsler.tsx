import { VarselComponent } from './Varsel';
import React, { useState } from 'react';
import { useRecoilValue } from 'recoil';
import { søkbareVarslerState } from '../../state/state';
import { Checkbox, CheckboxGroup } from '@navikt/ds-react';

export const Varsler = () => {
    const søkbareVarsler = useRecoilValue(søkbareVarslerState);
    const [skalFiltrereAvvikledeVarsler, setSkalFiltrereAvvikledeVarsler] = useState(false)

    const antallVarsler = skalFiltrereAvvikledeVarsler ? søkbareVarsler.filter(varsel => !varsel.avviklet).length : søkbareVarsler.length

    return (
        <div>
            <CheckboxGroup legend="Filtrer bort avviklede varsler" hideLegend onChange={(v) => setSkalFiltrereAvvikledeVarsler(!!v[0])}>
                <Checkbox value={true}>Filtrer bort avviklede varsler</Checkbox>
            </CheckboxGroup>
            <p className={'my-3'}> Viser {antallVarsler} varsler </p>
            <div className={'flex flex-col gap-4'}>
                {søkbareVarsler.length > 0 &&
                    søkbareVarsler.map((varsel) => {
                        if (varsel.avviklet && skalFiltrereAvvikledeVarsler) return null
                        return <VarselComponent key={varsel.varselkode} varsel={varsel} />
                    })}
            </div>
        </div>
    );
};
