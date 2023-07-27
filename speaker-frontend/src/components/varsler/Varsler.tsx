import { VarselComponent } from './Varsel';
import React, { useState } from 'react';
import { useRecoilValue } from 'recoil';
import { søkbareVarslerState } from '../../state/state';

export const Varsler = () => {
    const søkbareVarsler = useRecoilValue(søkbareVarslerState);

    const [skalFiltrereAvvikledeVarsler, setSkalFiltrereAvvikledeVarsler] = useState(false)

    const Checkbox = () => {
        return (
            <div style={{ color: 'green', lineHeight : 3, padding: 5 }}>
                <input type="checkbox" id="checkbox" checked={skalFiltrereAvvikledeVarsler} onChange={() => setSkalFiltrereAvvikledeVarsler(!skalFiltrereAvvikledeVarsler)} />
                <label htmlFor="checkbox">Filtrer bort avviklede varsler</label>
            </div>
        )
    }

    const antallVarsler = skalFiltrereAvvikledeVarsler ? søkbareVarsler.filter(varsel => !varsel.avviklet).length : søkbareVarsler.length

    return (
        <div>
            <Checkbox/>
            <p style={{color: 'green', lineHeight: 3}}> Viser {antallVarsler} varsler </p>
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
