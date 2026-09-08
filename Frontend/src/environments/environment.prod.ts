export const environment = {
  production: true,
  apiUrl: 'http://localhost:8080/api',
  azure: {
    clientId: 'fe5cc80e-6122-48fd-938c-2e444f068cf3',
    authority: 'https://login.microsoftonline.com/adae00b7-b38a-40cd-ae32-ec83026863e5',
    // TODO: cuando se despliegue a EC2, cambiar por la URL pública real
    // y agregar esa misma URL como Redirect URI en la app SPA de Azure AD.
    redirectUri: 'http://localhost:4200/auth-redirect',
    scopes: ['api://evaluaciondcn1.onmicrosoft.com/sanosysalvos/Pets.Access'],
  },
};