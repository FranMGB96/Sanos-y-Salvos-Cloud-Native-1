import { defineConfig } from 'cypress'

export default defineConfig({
  e2e: {
    baseUrl: 'http://localhost:4200',
    setupNodeEvents(on, config) {},
    // Tiempo de espera generoso para que carguen los componentes Angular
    defaultCommandTimeout: 8000,
    pageLoadTimeout: 30000,
    // Ignorar errores de CORS y de consola que no afectan los tests
    chromeWebSecurity: false,
  },
})
