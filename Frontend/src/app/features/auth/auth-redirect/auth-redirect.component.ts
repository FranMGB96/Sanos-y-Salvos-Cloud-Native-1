import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MsalBroadcastService } from '@azure/msal-angular';
import { InteractionStatus } from '@azure/msal-browser';
import { filter, take } from 'rxjs/operators';

// Página intencionalmente simple. Es el redirectUri que usa MSAL para el
// flujo de login (Redirect). No debe tener guards: si el Router de Angular
// redirige desde acá antes de tiempo, se puede "limpiar" el hash con el
// código de autenticación antes de que MSAL alcance a leerlo.
//
// Una vez que MSAL termina de procesar la respuesta (inProgress$ vuelve a
// None), navegamos a /inicio. Si el login falló, el authGuard de esa ruta
// se encarga de mandar de vuelta a /login.
@Component({
  selector: 'app-auth-redirect',
  standalone: true,
  template: `<p style="text-align:center; margin-top: 3rem; color:#666;">Procesando inicio de sesión...</p>`,
})
export class AuthRedirectComponent implements OnInit {

  constructor(
    private router: Router,
    private broadcastService: MsalBroadcastService,
  ) {}

  ngOnInit() {
    this.broadcastService.inProgress$.pipe(
      filter((status) => status === InteractionStatus.None),
      take(1),
    ).subscribe(() => {
      this.router.navigate(['/inicio']);
    });
  }
}
