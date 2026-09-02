/// <reference types="cypress" />

/**
 * Tests END TO END — Flujo de Login
 *
 * Simula un usuario real en el navegador:
 * abre la página, llena el formulario y verifica el resultado.
 *
 * Requiere que el frontend esté corriendo en localhost:4200
 * y el backend en localhost:8080
 */

describe('Flujo de autenticación', () => {

  beforeEach(() => {
    cy.visit('/login')
  })

  it('Login exitoso con credenciales válidas → redirige al dashboard', () => {
    cy.get('input[type="email"]').type('admin@sanosysalvos.cl')
    cy.get('input[type="password"]').type('admin1')
    cy.get('button[type="submit"]').click()
    cy.url().should('include', '/inicio')
    cy.get('nav').should('contain.text', 'Administrador')
  })

  it('Login admin → muestra el link al panel de administración', () => {
    cy.get('input[type="email"]').type('admin@sanosysalvos.cl')
    cy.get('input[type="password"]').type('admin1')
    cy.get('button[type="submit"]').click()
    cy.url().should('include', '/inicio')
    cy.get('nav').should('contain.text', 'Panel Admin')
  })

  it('Login con contraseña incorrecta → permanece en login', () => {
    cy.get('input[type="email"]').type('admin@sanosysalvos.cl')
    cy.get('input[type="password"]').type('passwordMala')
    cy.get('button[type="submit"]').click()
    // Verifica que no redirigió al dashboard
    cy.url().should('include', '/login')
    cy.url().should('not.include', '/inicio')
  })

  it('Login con campos vacíos → no permite enviar', () => {
    cy.get('button[type="submit"]').click()
    cy.url().should('include', '/login')
  })

  it('Logout → redirige al login', () => {
    cy.get('input[type="email"]').type('admin@sanosysalvos.cl')
    cy.get('input[type="password"]').type('admin1')
    cy.get('button[type="submit"]').click()
    cy.url().should('include', '/inicio')
    // Botón de cerrar sesión se llama "Salir"
    cy.get('nav').contains('Salir').click()
    cy.url().should('include', '/login')
  })

  it('Acceder a /inicio sin login → redirige al login', () => {
    cy.visit('/inicio')
    cy.url().should('include', '/login')
  })

  it('Acceder a /admin sin login → redirige al login', () => {
    cy.visit('/admin')
    cy.url().should('include', '/login')
  })

})