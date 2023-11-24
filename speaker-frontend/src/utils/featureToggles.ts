export const erLocal = () => location.hostname === 'localhost';
export const erDev = () => location.hostname === 'speaker.intern.dev.nav.no';
export const erProd = () => location.hostname === 'speaker.intern.nav.no';

export const erUtvikling = () => erLocal() || erDev();