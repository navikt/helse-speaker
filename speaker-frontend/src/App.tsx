import './App.css';
import '@navikt/ds-css';
import '@navikt/ds-css-internal';
import React from 'react';
import { Separator } from './components/Separator';
import { Clock, Dialog, Folder } from '@navikt/ds-icons';
import { RecoilRoot } from 'recoil';
import { Varsler } from './components/Varsler';
import { Header } from './components/Header';

const App = () => {
    return (
        <RecoilRoot>
            <Header />
            <div className={'flex flex-row py-4 pr-8 justify-end gap-16'}>
                <Clock className={'w-[24px] h-[24px]'}></Clock>
                <Dialog className={'w-[24px] h-[24px]'}></Dialog>
                <Folder className={'w-[24px] h-[24px]'}></Folder>
            </div>
            <Separator />
            <Varsler />
        </RecoilRoot>
    );
};

export default App;
