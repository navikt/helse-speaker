import './App.css';
import '@navikt/ds-css';
import '@navikt/ds-css-internal';
import React, { useEffect, useState } from 'react';
import { Separator } from './components/Separator';
import { Add, Clock, Dialog, Folder } from '@navikt/ds-icons';
import { useSetRecoilState } from 'recoil';
import { Varsler } from './components/Varsler';
import { Header } from './components/Header';
import { NyttVarsel } from './components/NyttVarsel';
import { NyttSubdomene } from './components/NyttSubdomene';
import { Tabs } from '@navikt/ds-react';
import { NyKontekst } from './components/NyKontekst';
import { fetchTeammedlemmer } from './endepunkter';
import { teammedlemmerState } from './state/state';

const App = () => {
    const [state, setState] = useState('varsler');
    const setTeammedlemmer = useSetRecoilState(teammedlemmerState);

    useEffect(() => {
        fetchTeammedlemmer().then((teammedlemmer) => {
            setTeammedlemmer(teammedlemmer);
        });
    }, []);

    return (
        <>
            <Header />
            <div className={'flex flex-row py-4 pr-8 justify-end gap-16'}>
                <Clock className={'w-[24px] h-[24px]'}></Clock>
                <Dialog className={'w-[24px] h-[24px]'}></Dialog>
                <Folder className={'w-[24px] h-[24px]'}></Folder>
            </div>
            <Separator />
            <div className={'max-w-[1275px]'}>
                <Tabs value={state} onChange={setState}>
                    <Tabs.List>
                        <Tabs.Tab value={'varsler'} label={'Varsler'} />
                        <Tabs.Tab
                            value="nytt-varsel"
                            label="Nytt varsel"
                            icon={<Add aria-hidden title="Nytt varsel" />}
                        />
                        <Tabs.Tab
                            value="nytt-subdomene"
                            label="Nytt subdomene"
                            icon={<Add aria-hidden title="Nytt subdomene" />}
                        />
                        <Tabs.Tab
                            value="ny-kontekst"
                            label="Ny kontekst"
                            icon={<Add aria-hidden title="Ny kontekst" />}
                        />
                    </Tabs.List>
                    <Tabs.Panel value={'varsler'}>
                        <Varsler />
                    </Tabs.Panel>
                    <Tabs.Panel value="nytt-varsel">
                        <NyttVarsel />
                    </Tabs.Panel>
                    <Tabs.Panel value="nytt-subdomene">
                        <NyttSubdomene />
                    </Tabs.Panel>
                    <Tabs.Panel value="ny-kontekst">
                        <NyKontekst />
                    </Tabs.Panel>
                </Tabs>
            </div>
        </>
    );
};

export default App;
