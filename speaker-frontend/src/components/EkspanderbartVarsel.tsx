import classNames from 'classnames';
import React, { ReactNode, useState } from 'react';
import {Accordion, Alert} from '@navikt/ds-react';

import styles from './EkspanderbartVarsel.module.css';

interface EkspanderbartVarselProps extends React.HTMLAttributes<HTMLDivElement>{
    label: ReactNode;
    children: ReactNode;
}

export const EkspanderbartVarsel: React.FC<EkspanderbartVarselProps> = ({ label, children, className, ...divProps}) => {
    const [open, setOpen] = useState(false);

    return (
        <Accordion.Item defaultOpen={open} className={classNames(styles.EkspanderbartVarsel, styles.warning, 'p-3', className)} {...divProps}>
            <Accordion.Header onClick={() => setOpen(!open)}>
                <Alert className={styles.Alert} variant={'warning'}>
                    {label}
                </Alert>
            </Accordion.Header>
            <Accordion.Content className={classNames(styles.Content, styles.warning)}> {children}
            </Accordion.Content>
        </Accordion.Item>
    );
};
