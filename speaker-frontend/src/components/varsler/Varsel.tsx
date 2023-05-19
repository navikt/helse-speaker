import React, { useState } from 'react';
import { BodyShort, Button, Chips, Label, Textarea } from '@navikt/ds-react';
import { EkspanderbartVarsel } from './EkspanderbartVarsel';
import { useForm } from 'react-hook-form';
import { fetchVarsler, postOppdaterVarsel } from '../../endepunkter';
import { useRecoilState, useRecoilValue, useSetRecoilState } from 'recoil';
import { brukerState, varslerState, velgbareTeammeldemmerState } from '../../state/state';
import styles from './Varsel.module.css';
import { Bruker, Varsel } from '../../types';

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
    const [bruker] = useRecoilState(brukerState);

    const teammedlemmer = useRecoilValue(velgbareTeammeldemmerState);
    const [selectedMedforfattere, setSelectedMedforfattere] = useState<Bruker[]>([]);

    const [isLoading, setIsLoading] = useState(false);
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
        if (!bruker) return;
        setIsLoading(true);
        postOppdaterVarsel({
            varselkode: varsel.varselkode,
            tittel: tittel,
            forklaring: forklaring,
            handling: handling,
            avviklet: varsel.avviklet,
            forfattere: [...selectedMedforfattere, bruker],
        }).then((r) => {
            if (r.status === 200) {
                fetchVarsler()
                    .then((varsler) => {
                        setVarsler(varsler);
                        reset({ tittel: tittel, forklaring: forklaring ?? '', handling: handling ?? '' });
                    })
                    .finally(() => {
                        setIsLoading(false);
                    });
            }
        });
    };

    return (
        <EkspanderbartVarsel
            key={varsel.varselkode}
            label={varsel.tittel}
            tidspunkt={varsel?.opprettet ?? ''}
            forfattere={varsel.forfattere}
        >
            <form onSubmit={handleSubmit(onSubmit)} className={'flex flex-col gap-4'}>
                <BodyShort>
                    <span className={styles.varselkode}>Varselkode:</span> {varsel.varselkode}
                </BodyShort>
                <Textarea
                    label="Tittel"
                    size="medium"
                    minRows={1}
                    maxRows={5}
                    error={(errors.tittel?.message as string) ?? ''}
                    {...register('tittel', { required: 'Tittel er påkrevd' })}
                />
                <Textarea label="Hva betyr det?" size="medium" minRows={1} maxRows={5} {...register('forklaring')} />
                <Textarea label="Hva gjør du?" size="medium" minRows={1} maxRows={5} {...register('handling')} />
                <Label>Medforfattere</Label>
                <Chips>
                    {teammedlemmer.map((c) => (
                        <Chips.Toggle
                            selected={selectedMedforfattere.includes(c)}
                            key={c.oid}
                            onClick={(event) => {
                                event.preventDefault();
                                setSelectedMedforfattere(
                                    selectedMedforfattere.includes(c)
                                        ? selectedMedforfattere.filter((x) => x !== c)
                                        : [...selectedMedforfattere, c],
                                );
                            }}
                        >
                            {c.navn}
                        </Chips.Toggle>
                    ))}
                </Chips>
                <div className={'flex flex-row gap-4'}>
                    <Button type={'submit'} variant={'primary'} disabled={!isValid || !isDirty} loading={isLoading}>
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
