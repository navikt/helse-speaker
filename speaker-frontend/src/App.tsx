import './App.css'
import "@navikt/ds-css";
import "@navikt/ds-css-internal";
import {Header} from "@navikt/ds-react-internal";
import {Button, Search, TextField} from '@navikt/ds-react';
import {EkspanderbartVarsel} from "./components/EkspanderbartVarsel";

export declare type Varsel = {
    kode: string;
    tittel: string;
    forklaring?: string;
    handling?: string;
}

function App() {

    const varsler: Varsel[] = [
        {
            kode: "RV_SY_1",
            tittel: "En tittel",
            forklaring: "En forklaring",
            handling: "En handling"
        },
        {
            kode: "RV_SY_1",
            tittel: "En tittel",
            forklaring: "En forklaring",
            handling: "En handling"
        },
        {
            kode: "RV_SY_1",
            tittel: "En tittel",
            forklaring: "En forklaring",
            handling: "En handling"
        },
        {
            kode: "RV_SY_1",
            tittel: "En tittel",
            forklaring: "En forklaring",
            handling: "En handling"
        },
        {
            kode: "RV_SY_1",
            tittel: "En tittel",
            forklaring: "En forklaring",
            handling: "En handling"
        },
    ]

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
                {
                    varsler.map((varsel) => <EkspanderbartVarsel label={varsel.tittel}>
                        <TextField
                            label="Tittel"
                            size="medium"
                            value={varsel.tittel}
                            className={'py-5'}
                        />
                        <TextField
                            label="Forklaring"
                            size="medium"
                            value={varsel.forklaring}
                            className={'pb-5'}
                        />
                        <TextField
                            label="Hva gjør man?"
                            size="medium"
                            value={varsel.handling}
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
