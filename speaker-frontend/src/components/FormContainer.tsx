import React, { ReactNode, useState } from 'react';
import classNames from 'classnames';
import styles from './FormContainer.module.css';
import { Button, Heading } from '@navikt/ds-react';
import { AddCircle, Close } from '@navikt/ds-icons';

export interface FormContainerProps {
    buttonTitle: string;
    formTitle: string;
    children: ReactNode;
}

export const FormContainer = ({ buttonTitle, formTitle, children }: FormContainerProps) => {
    const [toggled, setToggled] = useState(false);

    return (
        <div className={classNames(styles.NyttVarsel, 'pb-5')}>
            {!toggled ? (
                <div className="pt-4">
                    <Button variant="secondary" icon={<AddCircle />} onClick={() => setToggled(true)}>
                        {buttonTitle}
                    </Button>
                </div>
            ) : (
                <div className={classNames(styles.NyttVarselContainer, 'flex flex-col gap-4 bg-gray-100 pt-1 pr-1')}>
                    <div className="flex flex-row justify-between items-center">
                        <Heading level="4" size="small">
                            {formTitle}
                        </Heading>
                        <Button
                            variant={'tertiary-neutral'}
                            title={'Lukk'}
                            icon={<Close aria-hidden />}
                            onClick={() => setToggled(false)}
                        />
                    </div>
                    {children}
                </div>
            )}
        </div>
    );
};
