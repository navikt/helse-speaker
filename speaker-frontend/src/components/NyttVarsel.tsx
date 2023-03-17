import React, { useEffect, useState } from 'react';
import { Button, Heading, Select, Textarea } from '@navikt/ds-react';
import { useForm, useWatch } from 'react-hook-form';
import { fetchNesteVarselkode, fetchSubdomenerOgKontekster, fetchVarsler, postLagreVarsel } from '../endepunkter';
import classNames from 'classnames';
import styles from './NyttVarsel.module.css';
import { useRecoilState } from 'recoil';
import { brukerState, varslerState } from '../state/state';
import { Subdomene } from '../types';

interface NyttVarselForm {
    tittel: string;
    subdomene: string;
    kontekst: string;
    forklaring: string | null;
    handling: string | null;
}

export const NyttVarsel = () => {
    const [subdomener, setSubdomener] = useState<Subdomene[]>();
    const [nesteVarselkode, setNesteVarselkode] = useState<string | undefined>(undefined);
    const [skalOppretteNyttVarsel, setSkalOppretteNyttVarsel] = useState(false);
    const [, setVarsler] = useRecoilState(varslerState);
    const [bruker] = useRecoilState(brukerState);
    const [isLoading, setIsLoading] = useState(false);

    const {
        register,
        handleSubmit,
        reset,
        control,
        formState: { errors, isValid, isDirty },
    } = useForm<NyttVarselForm>({ mode: 'onChange' });

    const selectedSubdomene = useWatch({ control, name: 'subdomene' });
    const selectedKontekst = useWatch({ control, name: 'kontekst' });
    const erDefaultSubdomene = () => ['default', undefined].includes(selectedSubdomene);
    const erDefaultKontekst = () => ['default', undefined].includes(selectedKontekst);

    const onSubmit = ({ tittel, forklaring, handling }: NyttVarselForm) => {
        if (!bruker) return;
        setIsLoading(true);
        postLagreVarsel({
            varselkode: nesteVarselkode ?? '',
            tittel: tittel,
            forklaring: forklaring,
            handling: handling,
            avviklet: false,
            forfattere: [bruker],
        }).then((r) => {
            if (r.status === 200) {
                fetchVarsler()
                    .then((varsler) => {
                        setVarsler(varsler);
                        setSkalOppretteNyttVarsel(false);
                        reset();
                    })
                    .finally(() => setIsLoading(false));
            }
        });
    };

    useEffect(() => {
        fetchSubdomenerOgKontekster().then((subdomenerOgKontekster) => {
            setSubdomener(subdomenerOgKontekster);
        });
    }, []);

    useEffect(() => {
        if (!erDefaultSubdomene() && !erDefaultKontekst())
            fetchNesteVarselkode(selectedSubdomene, selectedKontekst).then((varselkode) =>
                setNesteVarselkode(varselkode),
            );
        else setNesteVarselkode(undefined);
    }, [selectedSubdomene, selectedKontekst]);

    if (!subdomener) return <></>;

    return (
        <div className={classNames(styles.NyttVarsel, 'p-4 pb-1')}>
            <div className={'flex flex-col gap-4'}>
                <Heading level="4" size={'small'}>
                    Nytt varsel
                </Heading>
                <form className={'flex flex-col gap-4'} onSubmit={handleSubmit(onSubmit)}>
                    <div className={'flex flex-row gap-4 items-start'}>
                        <Select
                            className={'min-w-[12rem]'}
                            label="Subdomene"
                            error={errors.subdomene ? 'Subdomene påkrevd' : ''}
                            {...register('subdomene', {
                                validate: (value) => {
                                    return !['default', undefined].includes(value);
                                },
                            })}
                        >
                            <option key={0} value={'default'}>
                                Velg subdomene
                            </option>
                            {subdomener.map((it) => (
                                <option key={it.forkortelse} value={it.forkortelse}>
                                    {it.navn} ({it.forkortelse})
                                </option>
                            ))}
                        </Select>
                        <Select
                            className={'min-w-[10rem]'}
                            label="Kontekst"
                            disabled={erDefaultSubdomene()}
                            error={errors.kontekst ? 'Kontekst påkrevd' : ''}
                            {...register('kontekst', {
                                validate: (value) => {
                                    return !['default', undefined].includes(value);
                                },
                            })}
                        >
                            <option key={0} value={'default'}>
                                Velg kontekst
                            </option>
                            {!erDefaultSubdomene() &&
                                subdomener
                                    .find((it) => it.forkortelse === selectedSubdomene)
                                    ?.kontekster.map((kontekst) => {
                                        return (
                                            <option key={kontekst.forkortelse} value={kontekst.forkortelse}>
                                                {kontekst.navn} ({kontekst.forkortelse})
                                            </option>
                                        );
                                    })}
                        </Select>
                        <span className={'self-start mt-11'}>Varselkode: {nesteVarselkode ?? ''}</span>
                    </div>
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
                        minRows={3}
                        maxRows={5}
                        {...register('forklaring')}
                    />
                    <Textarea
                        label="Hva gjør man?"
                        size="medium"
                        className={'pb-5'}
                        minRows={3}
                        maxRows={5}
                        {...register('handling')}
                    />
                    <div className={'flex flex-row gap-4 pb-5'}>
                        <Button type={'submit'} variant={'primary'} disabled={!isValid || !isDirty} loading={isLoading}>
                            Lagre
                        </Button>
                        <Button
                            variant={'secondary'}
                            disabled={!isDirty && !skalOppretteNyttVarsel}
                            onClick={() => {
                                reset();
                                setSkalOppretteNyttVarsel(false);
                            }}
                        >
                            Avbryt
                        </Button>
                    </div>
                </form>
            </div>
        </div>
    );
};
