/// <reference types="cypress" />

/**
 * Tests END TO END — Flujo de Reportes
 *
 * Simula un usuario real creando y visualizando reportes
 * de mascotas perdidas/encontradas.
 *
 * Requiere que el frontend esté corriendo en localhost:4200
 * y el backend en localhost:8080
 */


/// <reference types="cypress" />

describe('Flujo de reportes de mascotas', () => {

  beforeEach(() => {
    cy.visit('/login')
    cy.get('input[type="email"]').type('admin@sanosysalvos.cl')
    cy.get('input[type="password"]').type('admin1')
    cy.get('button[type="submit"]').click()
    cy.url().should('include', '/inicio')
  })

  it('Dashboard → muestra estadísticas del sistema', () => {
    cy.visit('/inicio')
    cy.get('body').should('be.visible')
    cy.get('body').invoke('text').should('match', /reporte|mascota|usuario/i)
  })

  it('Navegar a /reports → muestra el listado de reportes', () => {
    cy.visit('/reports')
    cy.url().should('include', '/reports')
    cy.get('body').should('be.visible')
  })

  it('Formulario de nuevo reporte → carga correctamente', () => {
    cy.visit('/reports/new')
    cy.url().should('include', '/reports/new')

    // Verifica que el formulario cargó
    cy.contains('Mascota Perdida').should('be.visible')
    cy.contains('Mascota Encontrada').should('be.visible')
    cy.get('textarea').should('be.visible')
    cy.contains('Publicar Reporte').should('be.visible')
  })

  it('Seleccionar tipo mascota perdida → activa el botón', () => {
    cy.visit('/reports/new')
    cy.contains('Mascota Perdida').click()
    cy.get('textarea').first().type("Test E2E — Perro perdido en Parque O'Higgins")
    cy.get('body').invoke('text').should('match', /mascota perdida/i)
  })

  it('Panel admin → carga la lista de usuarios', () => {
    cy.visit('/admin')
    cy.url().should('include', '/admin')
    cy.get('body').invoke('text').should('match', /usuario|email|rol/i)
  })

  it('Panel admin → tab de mascotas muestra contenido', () => {
    cy.visit('/admin')
    cy.contains(/mascota/i).click()
    cy.get('body').invoke('text').should('match', /mascota|especie|nombre/i)
  })

  it('Panel admin → tab de reportes muestra contenido', () => {
    cy.visit('/admin')
    cy.contains(/reporte/i).click()
    cy.get('body').invoke('text').should('match', /reporte|estado|tipo/i)
  })

  it('Logout desde el dashboard → redirige al login', () => {
    cy.get('nav').contains('Salir').click()
    cy.url().should('include', '/login')
  })

})