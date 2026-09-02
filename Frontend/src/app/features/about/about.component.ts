import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';



@Component({
  selector: 'app-about',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="about-container">
      <section class="hero-section">
        <h1>Sobre Nosotros</h1>
        <p>En <strong>Sanos y Salvos</strong>, creemos que cada mascota merece estar en casa.</p>
        
        <div class="image-wrapper animate-up">
          <img src="assets/SanosySalvos.png" alt="Sanos y Salvos Mascotas" class="mid-image">
        </div>
      </section>

      <div class="content-grid">
        <section class="card animate-up">
          <div class="icon">🎯</div>
          <h2>Misión</h2>
          <p>Facilitar el reencuentro de mascotas perdidas con sus familias a través de una plataforma tecnológica solidaria, rápida y eficiente, reduciendo el tiempo de angustia tanto para los animales como para sus dueños.</p>
        </section>

        <section class="card animate-up" style="animation-delay: 0.1s">
          <div class="icon">👁️</div>
          <h2>Visión</h2>
          <p>Convertirnos en la red de apoyo para mascotas más grande y confiable de Chile, logrando que ninguna mascota se quede sin hogar por falta de herramientas de comunicación y búsqueda.</p>
        </section>

        <section class="card full-width animate-up" style="animation-delay: 0.2s">
          <h2>¿Quiénes Somos?</h2>
          <p>Somos un equipo apasionado por los animales que entendió que la tecnología puede ser el puente más fuerte entre una mascota perdida y su hogar. Sanos y Salvos nació como una respuesta a la necesidad de centralizar los reportes de mascotas en una comunidad activa y comprometida.</p>
        </section>
      </div>
    </div>
  `,
  styles: [`
    .about-container {
      max-width: 1000px;
      margin: 0 auto;
      padding: 4rem 2rem;
      font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    }
    .hero-section {
      text-align: center;
      margin-bottom: 0; 
      position: relative;
      z-index: 10;
    }
    .hero-section h1 {
      color: #1a237e;
      font-size: 3rem;
      margin-bottom: 1rem;
    }
    .hero-section p {
      font-size: 1.2rem;
      color: #555;
    }

    /* Ajustes de la imagen */
    .image-wrapper {
      margin-top: 2.5rem;
      /* Margen negativo para que la imagen se meta entre las tarjetas */
      margin-bottom: -120px; /* Aumentado ligeramente para acomodar la nueva altura */
      display: flex;
      justify-content: center;
    }
    .mid-image {
      width: 100%;
      max-width: 700px; /* Agrandado un poco para dar más aire */
      height: auto; /* Permite que la altura se ajuste proporcionalmente */
      /* ELIMINADO: object-fit: cover; - Esto causaba el recorte */
      /* ELIMINADO: aspect-ratio: 16 / 7; - Forzaba una proporción incorrecta */
      border-radius: 20px;
      border: 8px solid white; /* Borde un poco más grueso para más aire */
      box-shadow: 0 10px 30px rgba(0,0,0,0.15);
    }

    .content-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 2rem;
      padding-top: 8rem; /* Espacio para que la imagen no tape el texto de las tarjetas */
    }
    .card {
      background: white;
      padding: 2.5rem;
      border-radius: 16px;
      box-shadow: 0 4px 20px rgba(0,0,0,0.08);
      border-top: 5px solid #1a237e;
    }
    .card h2 {
      color: #1a237e;
      margin-bottom: 1rem;
    }
    .card p {
      line-height: 1.6;
      color: #444;
    }
    .icon {
      font-size: 2.5rem;
      margin-bottom: 1rem;
    }
    .full-width {
      grid-column: span 2;
      border-top: 5px solid #ffca28;
    }

    @keyframes fadeInUp {
      from { opacity: 0; transform: translateY(20px); }
      to { opacity: 1; transform: translateY(0); }
    }
    .animate-up {
      animation: fadeInUp 0.6s ease forwards;
    }

    @media (max-width: 768px) {
      .content-grid { 
        grid-template-columns: 1fr; 
        padding-top: 10rem; /* Aumentado para móvil */
      }
      .full-width { grid-column: span 1; }
      .mid-image { 
        max-width: 95%; 
        margin-bottom: -60px; /* Ajustado para móvil */
      }
    }
  `]
})
export class AboutComponent {}
