import './App.css'
import "@navikt/ds-css";
import "@navikt/ds-css-internal";
import {Header} from "@navikt/ds-react-internal";
import {Button, Loader, Search, Textarea} from '@navikt/ds-react';
import {EkspanderbartVarsel} from "./components/EkspanderbartVarsel";
import {useEffect, useState} from "react";

export declare type Varsel = {
    varselkode: string;
    tittel: string;
    forklaring?: string | null;
    handling?: string | null;
    avviklet: boolean;
}

const App = () => {
    const [varsler, setVarsler] = useState<Varsel[]>([])
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        setLoading(true);
        fetch('/api/varsler')
            .then((response) => response.json())
            .then((data) => {
                setVarsler(data);
                setLoading(false);
            })
    }, [])

    return (
        <>
            <Header className={'flex w-full'}>
                <Header.Title as={'h1'}>Speaker</Header.Title>
                <form
                    className="self-center px-5 mr-auto"
                    onSubmit={(e) => {
                        e.preventDefault()
                        console.log("Search!")
                    }}
                >
                    <Search
                        label="header søk"
                        size="small"
                        variant="simple"
                        placeholder="Søk"
                    />
                </form>
                <Header.User name={"Hen Norhen"} description={"En ident"}/>
            </Header>
            <div className={'p-4'}>
                { loading && <Loader size={'3xlarge'} title={'Laster varsler'}/> }
                { varsler.length > 0 &&
                    varsler.map((varsel) => <EkspanderbartVarsel key={varsel.varselkode} label={varsel.tittel}>
                        <Textarea
                            label="Tittel"
                            size="medium"
                            minRows={1}
                            maxRows={5}
                            value={varsel.tittel}
                            className={'py-5'}
                        />
                        <Textarea
                            label="Forklaring"
                            size="medium"
                            minRows={1}
                            maxRows={5}
                            value={varsel.forklaring ?? ''}
                            className={'pb-5'}
                        />
                        <Textarea
                            label="Hva gjør man?"
                            size="medium"
                            minRows={1}
                            maxRows={5}
                            value={varsel.handling ?? ''}
                            className={'pb-5'}
                        />
                        <div className={'flex flex-row gap-4 pb-5'}>
                            <Button variant={"primary"}>Lagre</Button>
                            <Button variant={"secondary"}>Avbryt</Button>
                        </div>
                    </EkspanderbartVarsel>)
                }
            </div>
        </>
    )
}

export default App
