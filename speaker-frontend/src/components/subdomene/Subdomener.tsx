import React from 'react';
import { Table } from '@navikt/ds-react';
import { useRecoilValue } from 'recoil';
import { subdomenerOgKonteksterState } from '../../state/state';

export const Subdomener = () => {
    const subdomener = useRecoilValue(subdomenerOgKonteksterState);

    return (
        <Table zebraStripes size={'small'} className={'max-w-[1275px] whitespace-nowrap table-fixed border-hidden'}>
            <Table.Header>
                <Table.Row>
                    <Table.HeaderCell scope="col" className={'w-[250px]'}>
                        Navn
                    </Table.HeaderCell>
                    <Table.HeaderCell scope="col">Forkortelse</Table.HeaderCell>
                </Table.Row>
            </Table.Header>
            <Table.Body>
                {subdomener.map(({ navn, forkortelse }, i) => {
                    return (
                        <Table.Row key={i} shadeOnHover={false}>
                            <Table.DataCell className="border-hidden overflow-ellipsis">{navn}</Table.DataCell>
                            <Table.DataCell className="border-hidden overflow-ellipsis">{forkortelse}</Table.DataCell>
                            <Table.DataCell className={'border-hidden overflow-ellipsis w-full'}></Table.DataCell>
                        </Table.Row>
                    );
                })}
            </Table.Body>
        </Table>
    );
};
