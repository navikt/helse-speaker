import React, { useState } from 'react';
import { Varsel } from '../App';
import { Button, Textarea } from '@navikt/ds-react';
import { EkspanderbartVarsel } from './EkspanderbartVarsel';

export interface VarselProps {
    varsel: Varsel;
}

const isSame = (varselA: Varsel, varselB: Varsel) => {
    return JSON.stringify(varselA) === JSON.stringify(varselB);
};

export const VarselComponent = ({ varsel }: VarselProps) => {
    const [temp, setTemp] = useState(varsel);
    const [errors, setErrors] = useState<string[]>([]);

    const update = (key: string, value: string) => {
        setTemp({
            ...temp,
            [key]: value === '' ? null : value,
        });
    };

    const reset = () => setTemp(varsel);

    const validateTittel = (tittel?: string | undefined) => {
        const feilmelding = 'Du må oppgi tittel for varselet';

        if (tittel && tittel.trim().length !== 0) {
            if (errors.includes(feilmelding)) setErrors([...errors.filter((it) => it !== feilmelding)]);
            return '';
        }
        if (!errors.includes(feilmelding)) setErrors([...errors, feilmelding]);
        return feilmelding;
    };

    return (
        <EkspanderbartVarsel key={varsel.varselkode} label={varsel.tittel}>
            <Textarea
                label="Tittel"
                size="medium"
                className={'py-5'}
                minRows={1}
                maxRows={5}
                value={temp.tittel ?? ''}
                error={validateTittel(temp.tittel)}
                onChange={(event) => update('tittel', event.target.value)}
            />
            <Textarea
                label="Forklaring"
                size="medium"
                minRows={1}
                maxRows={5}
                value={temp.forklaring ?? ''}
                className={'pb-5'}
                onChange={(event) => update('forklaring', event.target.value)}
            />
            <Textarea
                label="Hva gjør man?"
                size="medium"
                minRows={1}
                maxRows={5}
                value={temp.handling ?? ''}
                className={'pb-5'}
                onChange={(event) => update('handling', event.target.value)}
            />
            <div className={'flex flex-row gap-4 pb-5'}>
                <Button variant={'primary'} disabled={errors.length > 0 || isSame(varsel, temp)}>
                    Lagre
                </Button>
                <Button variant={'secondary'} disabled={isSame(varsel, temp)} onClick={() => reset()}>
                    Avbryt
                </Button>
            </div>
        </EkspanderbartVarsel>
    );
};
