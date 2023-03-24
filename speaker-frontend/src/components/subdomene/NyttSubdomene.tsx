import { Button, TextField } from '@navikt/ds-react';
import React, { useEffect, useState } from 'react';
import { Subdomene } from '../../types';
import { useRecoilState } from 'recoil';
import { brukerState, varslerState } from '../../state/state';
import { useForm } from 'react-hook-form';
import { fetchSubdomenerOgKontekster, fetchVarsler, postNyttSubdomene } from '../../endepunkter';
import { FormContainer } from '../FormContainer';

interface NyttSubdomeneForm {
    navn: string;
    forkortelse: string;
}

export const NyttSubdomene = () => {
    const [subdomener, setSubdomener] = useState<Subdomene[]>();
    const [, setVarsler] = useRecoilState(varslerState);
    const [bruker] = useRecoilState(brukerState);
    const [isLoading, setIsLoading] = useState(false);

    const {
        register,
        handleSubmit,
        reset,
        formState: { errors, isValid, isDirty },
    } = useForm<NyttSubdomeneForm>({ mode: 'onBlur' });

    const onSubmit = ({ navn, forkortelse }: NyttSubdomeneForm) => {
        if (!bruker) return;
        setIsLoading(true);
        postNyttSubdomene({
            navn: navn,
            forkortelse: forkortelse,
        }).then((r) => {
            if (r.status === 200) {
                fetchVarsler()
                    .then((varsler) => {
                        setVarsler(varsler);
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

    if (!subdomener) return <></>;

    return (
        <FormContainer buttonTitle={'Legg til nytt subdomene'} formTitle={'Nytt subdomene'}>
            <form className={'flex flex-col gap-4'} onSubmit={handleSubmit(onSubmit)}>
                <div className={'flex flex-row gap-4 items-start'}>
                    <TextField
                        label="Navn"
                        size="medium"
                        description={'Menneskelig lesbart navn'}
                        className={'py-5 w-[16rem]'}
                        error={(errors.navn?.message as string) ?? ''}
                        {...register('navn', {
                            required: 'Navn er påkrevd',
                            validate: {
                                navnFinnesFraFør: (value) =>
                                    !subdomener?.map((it) => it.navn).includes(value) || 'Navnet finnes fra før',
                            },
                        })}
                    />
                    <TextField
                        label="Forkortelse"
                        htmlSize={4}
                        size="medium"
                        description={'Maksimalt to tegn og kun store bokstaver'}
                        className={'py-5'}
                        error={(errors.forkortelse?.message as string) ?? ''}
                        {...register('forkortelse', {
                            required: 'Forkortelse er påkrevd',
                            pattern: {
                                value: /^[A-ZÆØÅ]{2}$/,
                                message: 'Maksimalt to tegn og kun store bokstaver',
                            },
                            validate: {
                                forkortelseFinnesFraFør: (value) =>
                                    !subdomener?.map((it) => it.forkortelse).includes(value) ||
                                    'Forkortelsen finnes fra før',
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
