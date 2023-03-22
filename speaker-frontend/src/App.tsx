import './App.css';
import '@navikt/ds-css';
import '@navikt/ds-css-internal';
import React, { useEffect, useState } from 'react';
import { useSetRecoilState } from 'recoil';
import { Varsler } from './components/Varsler';
import { Header } from './components/Header';
import { NyttVarsel } from './components/NyttVarsel';
import { NyttSubdomene } from './components/NyttSubdomene';
import { Tabs } from '@navikt/ds-react';
import { NyKontekst } from './components/NyKontekst';
import { fetchSubdomenerOgKontekster, fetchTeammedlemmer, fetchVarsler } from './endepunkter';
import { subdomenerOgKonteksterState, teammedlemmerState, varslerState } from './state/state';

const App = () => {
    const [state, setState] = useState('varsler');
    const setTeammedlemmer = useSetRecoilState(teammedlemmerState);
    const setSubdomener = useSetRecoilState(subdomenerOgKonteksterState);
    const setVarsler = useSetRecoilState(varslerState);

    useEffect(() => {
        fetchTeammedlemmer().then((teammedlemmer) => {
            setTeammedlemmer(teammedlemmer);
        });
        fetchVarsler().then((varsler) => {
            setVarsler(varsler);
        });
        fetchSubdomenerOgKontekster().then((subdomenerOgKontekster) => {
            setSubdomener(subdomenerOgKontekster);
        });
    }, []);

    return (
        <>
            <Header />
            <Tabs value={state} onChange={setState} className="mt-8">
                <Tabs.List className="pl-10">
                    <Tabs.Tab value={'varsler'} label={'Varsler'} />
                    <Tabs.Tab value="subdomener" label="Subdomener" />
                    <Tabs.Tab value="kontekster" label="Kontekster" />
                </Tabs.List>
                <Tabs.Panel value="varsler" className="pl-10">
                    <NyttVarsel />
                    <Varsler />
                </Tabs.Panel>
                <Tabs.Panel value="subdomener" className="pl-10">
                    <NyttSubdomene />
                </Tabs.Panel>
                <Tabs.Panel value="kontekster" className="pl-10">
                    <NyKontekst />
                </Tabs.Panel>
            </Tabs>
        </>
    );
};

export default App;
