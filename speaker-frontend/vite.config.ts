import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import tokenFile from '../testtoken.json';

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [react()],
    server: {
        headers: {},
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                configure: (proxy, options) => {
                    options.headers = {
                        ...options.headers,
                        Authorization: `Bearer ${tokenFile.token}`,
                    };
                },
            },
        },
    },
});
