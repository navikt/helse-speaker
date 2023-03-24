import { Button, Select, TextField } from '@navikt/ds-react';
import React, { useState } from 'react';
import { Subdomene } from '../../types';
import { useRecoilState } from 'recoil';
import { brukerState, subdomenerOgKonteksterState } from '../../state/state';
import { useForm, useWatch } from 'react-hook-form';
import { fetchSubdomenerOgKontekster, postNyKontekst } from '../../endepunkter';
import { FormContainer } from '../FormContainer';

interface NyKontekstForm {
    subdomene: string;
    navn: string;
    forkortelse: string;
}

export const NyKontekst = () => {
    const [subdomener, setSubdomener] = useRecoilState(subdomenerOgKonteksterState);
    const [bruker] = useRecoilState(brukerState);
    const [isLoading, setIsLoading] = useState(false);

    const {
        register,
        handleSubmit,
        control,
        reset,
        formState: { errors, isValid, isDirty },
    } = useForm<NyKontekstForm>({ mode: 'onBlur' });

    const selectedSubdomeneForkortelse = useWatch({ control, name: 'subdomene' });
    const defaultSubdomene: Subdomene = { navn: 'default', forkortelse: 'DF', kontekster: [] };
    const erDefaultSubdomene = () => ['default', undefined].includes(selectedSubdomeneForkortelse);

    const finnSelectedSubdomene = (): Subdomene =>
        subdomener?.find((it) => it.forkortelse === selectedSubdomeneForkortelse) ?? defaultSubdomene;

    const onSubmit = ({ navn, forkortelse }: NyKontekstForm) => {
        if (!bruker) return;
        setIsLoading(true);
        postNyKontekst({
            navn: navn,
            forkortelse: forkortelse,
            subdomene: selectedSubdomeneForkortelse,
        }).then((r) => {
            if (r.status === 200) {
                fetchSubdomenerOgKontekster()
                    .then((subdomener) => {
                        setSubdomener(subdomener);
                        reset();
                    })
                    .finally(() => setIsLoading(false));
            }
        });
    };

    if (!subdomener) return <></>;

    return (
        <FormContainer buttonTitle={'Legg til ny kontekst'} formTitle={'Ny kontekst'}>
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
                    <TextField
                        label="Navn"
                        size="medium"
                        disabled={erDefaultSubdomene()}
                        className={'w-[16rem]'}
                        error={(errors.navn?.message as string) ?? ''}
                        {...register('navn', {
                            required: 'Navn er påkrevd',
                            validate: {
                                navnFinnesFraFør: (value) =>
                                    !finnSelectedSubdomene()
                                        .kontekster?.map((it) => it.navn.toLowerCase())
                                        .includes(value.toLowerCase()) || 'Navnet finnes fra før',
                            },
                        })}
                    />
                    <TextField
                        label="Forkortelse"
                        size="medium"
                        htmlSize={4}
                        disabled={erDefaultSubdomene()}
                        error={(errors.forkortelse?.message as string) ?? ''}
                        {...register('forkortelse', {
                            required: 'Forkortelse er påkrevd',
                            pattern: {
                                value: /^[A-ZÆØÅ]{2}$/,
                                message: 'Maksimalt to tegn og kun store bokstaver',
                            },
                            validate: {
                                forkortelseFinnesFraFør: (value) =>
                                    !finnSelectedSubdomene()
                                        .kontekster?.map((it) => it.forkortelse)
                                        .includes(value) || 'Forkortelsen finnes fra før',
                            },
                        })}
                    />
                </div>
                <div className={'flex flex-row gap-4 pb-5'}>
                    <Button type={'submit'} variant={'primary'} disabled={!isValid || !isDirty} loading={isLoading}>
                        Lagre
                    </Button>
                    <Button
                        variant={'secondary'}
                        disabled={!isDirty}
                        onClick={() => {
                            reset();
                        }}
                    >
                        Avbryt
                    </Button>
                </div>
            </form>
        </FormContainer>
    );
};
