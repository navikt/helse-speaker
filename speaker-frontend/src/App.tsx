import './App.css';
import '@navikt/ds-css';
import '@navikt/ds-css-internal';
import { Header } from '@navikt/ds-react-internal';
import { Loader, Search } from '@navikt/ds-react';
import React, { useEffect, useState } from 'react';
import { VarselComponent } from './components/Varsel';

export declare type Varsel = {
    varselkode: string;
    tittel: string;
    forklaring?: string | null;
    handling?: string | null;
    avviklet: boolean;
};

const App = () => {
    const [varsler, setVarsler] = useState<Varsel[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        setLoading(true);
        fetch('/api/varsler')
            .then((response) => response.json())
            .then((data) => {
                setVarsler(data);
                setLoading(false);
            });
    }, []);

    return (
        <>
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
            <div className={'p-4'}>
                {loading && <Loader size={'3xlarge'} title={'Laster varsler'} />}
                {varsler.length > 0 && varsler.map((varsel) => <VarselComponent key={varsel.tittel} varsel={varsel} />)}
            </div>
        </>
    );
};

export default App;
