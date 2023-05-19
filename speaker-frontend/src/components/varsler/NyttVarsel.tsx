import React, { useEffect, useState } from 'react';
import { BodyShort, Button, Chips, Label, Select, Textarea, TextField } from '@navikt/ds-react';
import { useForm, useWatch } from 'react-hook-form';
import { fetchNesteVarselkode, fetchVarsler, postLagreVarsel } from '../../endepunkter';
import { useRecoilValue, useSetRecoilState } from 'recoil';
import { brukerState, subdomenerOgKonteksterState, varslerState, velgbareTeammeldemmerState } from '../../state/state';
import { Bruker, Subdomene } from '../../types';
import { FormContainer } from '../FormContainer';

interface NyttVarselForm {
    tittel: string;
    subdomene: string;
    kontekst: string;
    forklaring: string | null;
    handling: string | null;
}

export const NyttVarsel = () => {
    const [nesteVarselkode, setNesteVarselkode] = useState<string | undefined>(undefined);
    const subdomener = useRecoilValue<Subdomene[]>(subdomenerOgKonteksterState);

    const teammedlemmer = useRecoilValue(velgbareTeammeldemmerState);
    const [selectedMedforfattere, setSelectedMedforfattere] = useState<Bruker[]>([]);

    const setVarsler = useSetRecoilState(varslerState);
    const bruker = useRecoilValue(brukerState);

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
            forfattere: [...selectedMedforfattere, bruker],
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
        if (!erDefaultSubdomene() && !erDefaultKontekst())
            fetchNesteVarselkode(selectedSubdomene, selectedKontekst).then((varselkode) =>
                setNesteVarselkode(varselkode),
            );
        else setNesteVarselkode(undefined);
    }, [selectedSubdomene, selectedKontekst]);

    if (!subdomener) return <></>;

    return (
        <FormContainer buttonTitle={'Legg til nytt varsel'} formTitle={'Nytt varsel'}>
            <form className="flex flex-col gap-4 max-w-[1154px]" onSubmit={handleSubmit(onSubmit)}>
                <div className="flex flex-row gap-4 items-start">
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
                    <BodyShort size="small" className={'self-end pb-4 pl-4'}>
                        Varselkode: {nesteVarselkode ?? ''}
                    </BodyShort>
                </div>
                <TextField
                    label="Tittel"
                    size="medium"
                    className={'py-5 w-1/2'}
                    error={(errors.tittel?.message as string) ?? ''}
                    {...register('tittel', { required: 'Tittel er påkrevd' })}
                />
                <Textarea
                    label="Hva betyr det?"
                    size="medium"
                    className={'pb-5'}
                    minRows={3}
                    maxRows={5}
                    {...register('forklaring')}
                />
                <Textarea
                    label="Hva gjør du?"
                    size="medium"
                    className={'pb-5'}
                    minRows={3}
                    maxRows={5}
                    {...register('handling')}
                />
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
                <div className={'flex flex-row gap-4 pb-5'}>
                    <Button type={'submit'} variant={'primary'} disabled={!isValid || !isDirty} loading={isLoading}>
                        Lagre
                    </Button>
                    <Button variant={'secondary'} disabled={!isDirty} onClick={() => reset()}>
                        Avbryt
                    </Button>
                </div>
            </form>
        </FormContainer>
    );
};
