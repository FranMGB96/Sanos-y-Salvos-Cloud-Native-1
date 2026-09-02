import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { FooterComponent } from './shared/components/navbar/footer.component';
import { AuthService } from './core/services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  // 2. Agrega FooterComponent a los imports
  imports: [RouterOutlet, NavbarComponent, FooterComponent, CommonModule], 
  template: `
    <app-navbar *ngIf="auth.isLoggedIn()"></app-navbar>

    <main [class.with-nav]="auth.isLoggedIn()">
      <router-outlet></router-outlet>
    </main>

    <app-footer *ngIf="auth.isLoggedIn()"></app-footer>
  `,
  styles: [`
    main { 
      min-height: calc(100vh - 160px); /* Ajuste para que el footer no flote si hay poco contenido */
    } 
    main.with-nav { 
      padding-top: 64px; 
    }
  `]
})
export class AppComponent { 
  constructor(public auth: AuthService) {} 
}