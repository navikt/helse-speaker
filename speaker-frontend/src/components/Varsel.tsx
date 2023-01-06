import React from 'react';
import { Varsel } from '../App';
import { BodyShort, Button, Textarea } from '@navikt/ds-react';
import { EkspanderbartVarsel } from './EkspanderbartVarsel';
import { useForm } from 'react-hook-form';
import { fetchVarsler, postOppdaterVarsel } from '../endepunkter';
import { useSetRecoilState } from 'recoil';
import { varslerState } from '../state/varselState';
import styles from './Varsel.module.css';

export interface VarselProps {
    varsel: Varsel;
}

interface VarselForm {
    tittel: string;
    forklaring: string | null;
    handling: string | null;
}

export const VarselComponent = ({ varsel }: VarselProps) => {
    const setVarsler = useSetRecoilState(varslerState);
    const defaultValues = {
        tittel: varsel.tittel,
        forklaring: varsel.forklaring ?? '',
        handling: varsel.handling ?? '',
    };

    const {
        register,
        handleSubmit,
        reset,
        formState: { errors, isValid, isDirty },
    } = useForm({
        mode: 'onChange',
        defaultValues,
    });

    const onSubmit = ({ tittel, forklaring, handling }: VarselForm) => {
        postOppdaterVarsel({
            varselkode: varsel.varselkode,
            tittel: tittel,
            forklaring: forklaring,
            handling: handling,
            avviklet: varsel.avviklet,
        }).then((r) => {
            if (r.status === 200) {
                fetchVarsler().then((varsler) => {
                    setVarsler(varsler);
                    reset({tittel: tittel, forklaring: forklaring ?? '', handling: handling ?? ''})
                });
            }
        });
    };

    return (
        <EkspanderbartVarsel key={varsel.varselkode} label={varsel.tittel}>
            <form onSubmit={handleSubmit(onSubmit)}>
                <BodyShort className={'pt-5'}>
                    <span className={styles.varselkode}>Varselkode:</span> {varsel.varselkode}
                </BodyShort>
                <Textarea
                    label="Tittel"
                    size="medium"
                    className={'py-5'}
                    minRows={1}
                    maxRows={5}
                    error={(errors.tittel?.message as string) ?? ''}
                    {...register('tittel', { required: 'Tittel er påkrevd' })}
                />
                <Textarea
                    label="Forklaring"
                    size="medium"
                    className={'pb-5'}
                    minRows={1}
                    maxRows={5}
                    {...register('forklaring')}
                />
                <Textarea
                    label="Hva gjør man?"
                    size="medium"
                    className={'pb-5'}
                    minRows={1}
                    maxRows={5}
                    {...register('handling')}
                />
                <div className={'flex flex-row gap-4 pb-5'}>
                    <Button type={'submit'} variant={'primary'} disabled={!isValid || !isDirty}>
                        Lagre
                    </Button>
                    <Button variant={'secondary'} disabled={!isDirty} onClick={() => reset()}>
                        Avbryt
                    </Button>
                </div>
            </form>
        </EkspanderbartVarsel>
    );
};
