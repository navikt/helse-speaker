import React, { useEffect, useState } from 'react';
import { Button, Heading, Select, Textarea } from '@navikt/ds-react';
import { Add } from '@navikt/ds-icons';
import { useForm, useWatch } from 'react-hook-form';
import { fetchNesteVarselkode, fetchSubdomenerOgKontekster, fetchVarsler, postLagreVarsel } from '../endepunkter';
import classNames from 'classnames';
import styles from './NyttVarsel.module.css';
import { useRecoilState } from 'recoil';
import { varslerState } from '../state/state';

interface NyttVarselForm {
    tittel: string;
    subdomene: string;
    kontekst: string;
    forklaring: string | null;
    handling: string | null;
}

export const NyttVarsel = () => {
    const [subdomenerOgKontekster, setSubdomenerOgKontekster] = useState<{ [subdomene: string]: string[] }>();
    const [nesteVarselkode, setNesteVarselkode] = useState<string | undefined>(undefined);
    const [skalOppretteNyttVarsel, setSkalOppretteNyttVarsel] = useState(false);
    const [, setVarsler] = useRecoilState(varslerState);

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
        postLagreVarsel({
            varselkode: nesteVarselkode ?? '',
            tittel: tittel,
            forklaring: forklaring,
            handling: handling,
            avviklet: false,
        }).then((r) => {
            if (r.status === 200) {
                fetchVarsler().then((varsler) => {
                    setVarsler(varsler);
                    setSkalOppretteNyttVarsel(false);
                    reset();
                });
            }
        });
    };

    useEffect(() => {
        fetchSubdomenerOgKontekster().then((subdomenerOgKontekster) => {
            setSubdomenerOgKontekster(subdomenerOgKontekster);
        });
    }, []);

    useEffect(() => {
        if (!erDefaultSubdomene() && !erDefaultKontekst())
            fetchNesteVarselkode(selectedSubdomene, selectedKontekst).then((varselkode) =>
                setNesteVarselkode(varselkode),
            );
        else setNesteVarselkode(undefined);
    }, [selectedSubdomene, selectedKontekst]);

    if (!subdomenerOgKontekster) return <></>;

    return (
        <div className={classNames(styles.NyttVarsel, 'p-4 pb-1')}>
            {!skalOppretteNyttVarsel ? (
                <Button icon={<Add aria-hidden></Add>} onClick={() => setSkalOppretteNyttVarsel(true)}>
                    Nytt varsel
                </Button>
            ) : (
                <div className={'flex flex-col gap-4'}>
                    <Heading level="4" size={'small'}>
                        Nytt varsel
                    </Heading>
                    <form className={'flex flex-col gap-4'} onSubmit={handleSubmit(onSubmit)}>
                        <div className={'flex flex-row gap-4 items-start'}>
                            {
                                <Select
                                    className={'w-[12rem]'}
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
                                    {Object.entries(subdomenerOgKontekster).map(([key]) => (
                                        <option key={key}>{key}</option>
                                    ))}
                                </Select>
                            }

                            <Select
                                className={'w-[10rem]'}
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
                                    subdomenerOgKontekster[selectedSubdomene].map((key) => {
                                        return <option key={key}>{key}</option>;
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
                            <Button type={'submit'} variant={'primary'} disabled={!isValid || !isDirty}>
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
            )}
        </div>
    );
};
