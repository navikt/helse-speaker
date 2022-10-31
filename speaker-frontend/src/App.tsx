import './App.css';
import '@navikt/ds-css';
import '@navikt/ds-css-internal';
import { Header } from '@navikt/ds-react-internal';
import { Search } from '@navikt/ds-react';
import React from 'react';
import { Separator } from './components/Separator';
import { Clock, Dialog, Folder } from '@navikt/ds-icons';
import { RecoilRoot } from 'recoil';
import { Varsler } from './components/Varsler';

export declare type Varsel = {
    varselkode: string;
    tittel: string;
    forklaring?: string | null;
    handling?: string | null;
    avviklet: boolean;
};

const App = () => {
    return (
        <RecoilRoot>
            <Header className={'flex w-full'}>
                <Header.Title as={'h1'}>Speaker</Header.Title>
                <form
                    className="self-center px-5 mr-auto"
                    onSubmit={(e) => {
                        e.preventDefault();
                        console.log('Search!');
                    }}
                >
                    <Search label="header søk" size="small" variant="simple" placeholder="Søk" />
                </form>
                <Header.User name={'Hen Norhen'} description={'En ident'} />
            </Header>
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
