import React from 'react';
import { Table } from '@navikt/ds-react';
import { useRecoilValue } from 'recoil';
import { subdomenerOgKonteksterState } from '../../state/state';

interface Flattened {
    subdomenenavn: string;
    kontekstnavn: string;
    kontekstforkortelse: string;
}
export const Kontekster = () => {
    const subdomenerOgKontekster = useRecoilValue(subdomenerOgKonteksterState);

    const flattened: Flattened[] = subdomenerOgKontekster.flatMap((subdomene) => {
        return subdomene.kontekster.map((kontekst) => {
            return {
                subdomenenavn: `${subdomene.navn} (${subdomene.forkortelse})`,
                kontekstnavn: kontekst.navn,
                kontekstforkortelse: kontekst.forkortelse,
            };
        });
    });

    return (
        <Table zebraStripes size={'small'} className={'max-w-[1275px] whitespace-nowrap table-fixed border-hidden'}>
            <Table.Header>
                <Table.Row>
                    <Table.HeaderCell scope="col" className={'w-[250px]'}>
                        Subdomene
                    </Table.HeaderCell>
                    <Table.HeaderCell scope="col" className={'w-[250px]'}>
                        Navn
                    </Table.HeaderCell>
                    <Table.HeaderCell scope="col">Forkortelse</Table.HeaderCell>
                </Table.Row>
            </Table.Header>
            <Table.Body>
                {flattened.map(({ subdomenenavn, kontekstnavn, kontekstforkortelse }, i) => {
                    return (
                        <Table.Row key={i} shadeOnHover={false}>
                            <Table.DataCell className="border-hidden overflow-ellipsis">{subdomenenavn}</Table.DataCell>
                            <Table.DataCell className="border-hidden overflow-ellipsis">{kontekstnavn}</Table.DataCell>
                            <Table.DataCell className="border-hidden overflow-ellipsis">
                                {kontekstforkortelse}
                            </Table.DataCell>
                            <Table.DataCell className={'border-hidden overflow-ellipsis w-full'}></Table.DataCell>
                        </Table.Row>
                    );
                })}
            </Table.Body>
        </Table>
    );
};
