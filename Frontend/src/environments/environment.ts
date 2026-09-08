export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  azure: {
    clientId: 'fe5cc80e-6122-48fd-938c-2e444f068cf3',
    authority: 'https://login.microsoftonline.com/adae00b7-b38a-40cd-ae32-ec83026863e5',
    redirectUri: 'http://localhost:4200/auth-redirect',
    scopes: ['api://evaluaciondcn1.onmicrosoft.com/sanosysalvos/Pets.Access'],
  },
};