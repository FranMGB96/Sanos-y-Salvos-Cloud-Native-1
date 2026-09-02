import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router'; // Importante para la navegación



@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule, RouterLink], // Añadido RouterLink aquí
  template: `
    <footer class="footer">
      <div class="footer-content">
        <div class="social-links">
          <a href="https://www.tiktok.com" target="_blank" class="social-icon" title="TikTok">
            <i class="fab fa-tiktok"></i>
          </a>
          <a href="https://www.instagram.com" target="_blank" class="social-icon" title="Instagram">
            <i class="fab fa-instagram"></i>
          </a>
          <a href="https://wa.me/56912345678" target="_blank" class="social-icon" title="WhatsApp">
            <i class="fab fa-whatsapp"></i>
          </a>
          <a href="https://www.facebook.com" target="_blank" class="social-icon" title="Facebook">
            <i class="fab fa-facebook-f"></i>
          </a>
        </div>

        <div class="footer-nav">
          <a routerLink="/nosotros" class="nav-link">Nosotros</a>
        </div>

        <div class="contact-email">
          <a href="mailto:contacto@sanosysalvo.cl">contacto&#64;sanosysalvo.cl</a>
        </div>

        <div class="footer-bottom">
          <p><span class="brand">🐾 Sanos y Salvos</span> &copy; {{ currentYear }}</p>
        </div>
      </div>
    </footer>`,
  styles: [`
    .footer {
      background: #1a237e;
      color: white;
      padding: 2.5rem 0 1.5rem 0;
      margin-top: 4rem;
      width: 100%;
      box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
    }
    .footer-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 1.2rem;
      max-width: 1200px;
      margin: 0 auto;
    }
    .social-links {
      display: flex;
      gap: 1.5rem;
    }
    .social-icon {
      width: 45px;
      height: 45px;
      background: white;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      text-decoration: none;
      transition: all 0.3s ease;
    }
    .social-icon i {
      color: #1a237e !important; 
      font-size: 1.3rem;
      display: block;
    }
    .social-icon:hover {
      transform: translateY(-5px);
      background: #ffca28;
    }
    .footer-nav {
      margin: 0.5rem 0;
    }
    .nav-link {
      color: white;
      text-decoration: none;
      font-weight: 500;
      font-size: 1.1rem;
      transition: color 0.2s ease;
    }
    .nav-link:hover {
      color: #ffca28;
    }
    .contact-email a {
      color: white;
      text-decoration: none;
      font-weight: 500;
      font-size: 1.1rem;
    }
    .contact-email a:hover {
      color: #ffca28;
      text-decoration: underline;
    }
    .footer-bottom {
      width: 90%;
      max-width: 600px;
      border-top: 1px solid rgba(255, 255, 255, 0.1);
      padding-top: 1rem;
      font-size: 0.85rem;
      color: rgba(255, 255, 255, 0.6);
      text-align: center;
    }
    .brand { font-weight: 700; color: white; margin-right: 5px; }
  `]
})
export class FooterComponent {
  currentYear = new Date().getFullYear();
}
