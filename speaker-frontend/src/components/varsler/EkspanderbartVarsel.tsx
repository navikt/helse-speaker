import classNames from 'classnames';
import React, { ReactNode, useState } from 'react';
import { Accordion, Alert, BodyShort } from '@navikt/ds-react';

import styles from './EkspanderbartVarsel.module.css';
import { Bruker } from '../../types';
import dayjs from 'dayjs';

interface EkspanderbartVarselProps extends React.HTMLAttributes<HTMLDivElement> {
    forfattere: Bruker[];
    tidspunkt: string;
    label: ReactNode;
    children: ReactNode;
    avviklet: boolean;
}

const NORSK_DATOFORMAT_MED_KLOKKESLETT = 'DD.MM.YYYY kl HH.mm';

export const EkspanderbartVarsel: React.FC<EkspanderbartVarselProps> = ({
    label,
    children,
    className,
    tidspunkt,
    forfattere,
    avviklet,
    ...divProps
}) => {
    const [open, setOpen] = useState(false);

    const authorsText = forfattere.map((it) => it.navn).join(', ');

    const siDet = () => {
        if (label != null) {
            const synth = window.speechSynthesis;
            const utterance = new SpeechSynthesisUtterance(label.toString());
            utterance.lang = "nb"
            synth.speak(utterance);
        }
    };

    return (
        <Accordion.Item
            defaultOpen={open}
            className={classNames(styles.EkspanderbartVarsel, avviklet ? styles.avviklet : styles.warning, className)}
            {...divProps}
        >
            <Accordion.Header onClick={() => setOpen(!open)}>
                <Alert className={styles.Alert} variant={'warning'}>
                    {label} <button onClick={siDet}>🔊</button>
                    <div className={'flex flex-row gap-1 italic text-gray-700'}>
                        <BodyShort size={'small'}>
                            Sist endret {dayjs(tidspunkt).format(NORSK_DATOFORMAT_MED_KLOKKESLETT)}
                        </BodyShort>
                        {forfattere && forfattere.length > 0 && <BodyShort size={'small'}>av {authorsText}</BodyShort>}
                    </div>
                </Alert>
            </Accordion.Header>
            {open && (
                <Accordion.Content className={classNames(styles.Content, avviklet ? styles.avviklet : styles.warning)}>{children}</Accordion.Content>
            )}
        </Accordion.Item>
    );
};
