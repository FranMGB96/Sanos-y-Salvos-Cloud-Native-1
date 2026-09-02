import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { PetService } from '../../../core/services/pet.service';
import { CategoryService } from '../../../core/services/category.service';
import { AuthService } from '../../../core/services/auth.service';
import { Pet } from '../../../core/models/pet.model';
import { Category } from '../../../core/models/category.model';
import { environment } from '../../../../environments/environment';

interface PetConDueno extends Pet {
  nombreDueno?: string;
}

@Component({
  selector: 'app-pet-list',
  standalone: true,
  imports: [CommonModule, RouterLink],

  template: `
    <div class="page">

      <div class="page-header">
        <div>
          <h1>Mascotas</h1>
          <p>{{ pets.length }} mascota(s) registradas</p>
        </div>
        <a routerLink="/pets/new" class="btn-primary">+ Nueva Mascota</a>
      </div>

      <div class="filter-bar">
        <button
          class="filter-chip"
          [class.active]="selectedCategoria === null"
          (click)="filterByCategoria(null)">
          Todas
        </button>
        <button
          *ngFor="let cat of categories"
          class="filter-chip"
          [class.active]="selectedCategoria === cat.nombre"
          (click)="filterByCategoria(cat.nombre)">
          {{ getEmoji(cat.nombre) }} {{ cat.nombre }}
        </button>
      </div>

      <div *ngIf="loading" class="loading">Cargando mascotas...</div>

      <div class="pets-grid" *ngIf="!loading">

        <div class="pet-card" *ngFor="let pet of pets">

          <div class="pet-avatar">
            <img
              *ngIf="pet.fotoUrl"
              [src]="pet.fotoUrl"
              [alt]="pet.nombre"
              (error)="pet.fotoUrl='https://placehold.co/400x300?text=Mascota'"
            >
            <span *ngIf="!pet.fotoUrl" class="pet-emoji">
              {{ getEmoji(pet.especie) }}
            </span>
          </div>

          <div class="pet-info">
            <h3>{{ pet.nombre }}</h3>
            <p class="pet-especie">{{ pet.especie }}</p>
            <div class="pet-tags">
              <span class="tag" *ngIf="pet.raza">{{ pet.raza }}</span>
              <span class="tag" *ngIf="pet.color">{{ pet.color }}</span>
              <span class="tag" *ngIf="pet.tamanio">{{ pet.tamanio }}</span>
            </div>
          </div>

          <!-- Botones para el dueño -->
          <div class="pet-actions" *ngIf="isOwner(pet)">
            <a [routerLink]="['/pets/edit', pet.id]" class="btn-edit">Editar</a>
            <button class="btn-delete" (click)="delete(pet)">Eliminar</button>
          </div>

          <!-- Nombre del dueño para mascotas de otros -->
          <div class="pet-actions pet-other" *ngIf="!isOwner(pet)">
            <span class="owner-badge">
              👤 {{ pet.nombreDueno || 'Cargando...' }}
            </span>
          </div>

        </div>

        <div class="empty-state" *ngIf="pets.length === 0">
          <span>🐾</span>
          <p>No hay mascotas registradas</p>
          <a routerLink="/pets/new" class="btn-primary">Registrar primera mascota</a>
        </div>

      </div>
    </div>
  `,

  styles: [`
    .page{ padding:2rem; max-width:1100px; margin:0 auto }

    .page-header{
      display:flex; justify-content:space-between;
      align-items:flex-start; margin-bottom:2rem
    }
    .page-header h1{ font-size:1.8rem; color:#1a237e; margin:0 0 .25rem }
    .page-header p{ color:#666; margin:0; font-size:.9rem }

    .btn-primary{
      background:#1a237e; color:white; padding:.7rem 1.4rem;
      border-radius:8px; text-decoration:none; font-weight:600; font-size:.9rem
    }

    .filter-bar{
      display:flex; flex-wrap:wrap; gap:.5rem;
      margin-bottom:1.5rem
    }
    .filter-chip{
      border:1px solid #c5cae9; background:white; color:#3949ab;
      padding:.4rem 1rem; border-radius:20px; font-size:.85rem;
      font-weight:600; cursor:pointer; transition:.15s
    }
    .filter-chip:hover{ background:#e8eaf6 }
    .filter-chip.active{ background:#1a237e; color:white; border-color:#1a237e }

    .loading{ text-align:center; padding:3rem; color:#666 }

    .pets-grid{
      display:grid;
      grid-template-columns:repeat(auto-fill,minmax(280px,1fr));
      gap:1.25rem
    }

    .pet-card{
      background:white; border-radius:12px; overflow:hidden;
      box-shadow:0 2px 10px rgba(0,0,0,.08); transition:transform .2s
    }
    .pet-card:hover{ transform:translateY(-2px) }

    .pet-avatar{
      height:220px; background:#e8eaf6; display:flex;
      align-items:center; justify-content:center; overflow:hidden
    }
    .pet-avatar img{ width:100%; height:100%; object-fit:contain; background:#e8eaf6 }
    .pet-emoji{ font-size:4rem }

    .pet-info{ padding:1rem 1rem .5rem }
    .pet-info h3{ margin:0 0 .2rem; font-size:1.1rem; color:#1a237e }
    .pet-especie{ margin:0 0 .5rem; color:#666; font-size:.85rem }

    .pet-tags{ display:flex; flex-wrap:wrap; gap:.3rem; margin-bottom:.5rem }
    .tag{
      background:#e8eaf6; color:#3949ab;
      padding:.15rem .5rem; border-radius:12px; font-size:.75rem
    }

    .pet-actions{
      display:flex; gap:.5rem;
      padding:.75rem 1rem; border-top:1px solid #f0f0f0
    }

    .pet-other{
      justify-content:center;
      background:#fafafa;
    }

    .owner-badge{
      font-size:.85rem; color:#555; font-weight:600;
    }

    .btn-edit{
      flex:1; text-align:center; padding:.4rem; border-radius:6px;
      background:#e8eaf6; color:#1a237e; text-decoration:none;
      font-size:.85rem; font-weight:600
    }

    .btn-delete{
      flex:1; padding:.4rem; border-radius:6px; border:none;
      background:#ffebee; color:#c62828; font-size:.85rem;
      font-weight:600; cursor:pointer
    }

    .empty-state{ grid-column:1/-1; text-align:center; padding:4rem 2rem }
    .empty-state span{ font-size:4rem; display:block; margin-bottom:1rem }
    .empty-state p{ color:#666; margin-bottom:1.5rem }
  `]
})
export class PetListComponent implements OnInit {

  pets: PetConDueno[] = [];
  allPets: PetConDueno[] = [];
  categories: Category[] = [];
  selectedCategoria: string | null = null;
  loading = true;
  currentUserId: number | null = null;

  constructor(
    private petService: PetService,
    private categoryService: CategoryService,
    private auth: AuthService,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.currentUserId = this.auth.getCurrentUser()?.userId ?? null;

    this.petService.getAll().subscribe({
      next: (mascotas) => {
        // ownerIds únicos que NO son el usuario actual
        const ownerIdsAjenos = [...new Set(
          mascotas
            .filter(p => p.ownerId !== this.currentUserId)
            .map(p => p.ownerId)
        )];

        if (ownerIdsAjenos.length === 0) {
          this.allPets = mascotas;
          this.pets = mascotas;
          this.loading = false;
          return;
        }

        // Consultar nombre de cada dueño en paralelo
        const peticiones = ownerIdsAjenos.map(id =>
          this.http.get<any>(`${environment.apiUrl}/users/${id}`).pipe(
            map(u => ({ id, nombre: u.nombre as string })),
            catchError(() => of({ id, nombre: 'Usuario desconocido' }))
          )
        );

        forkJoin(peticiones).subscribe(duenos => {
          const mapaDuenos = new Map(duenos.map(d => [d.id, d.nombre]));
          const mascotasConDueno = mascotas.map(p => ({
            ...p,
            nombreDueno: p.ownerId !== this.currentUserId
              ? mapaDuenos.get(p.ownerId) ?? 'Usuario desconocido'
              : undefined
          }));
          this.allPets = mascotasConDueno;
          this.pets = mascotasConDueno;
          this.loading = false;
        });
      },
      error: () => { this.loading = false; }
    });

    this.categoryService.getAll().subscribe({
      next: (cats) => this.categories = cats,
      error: () => this.categories = []
    });
  }

  filterByCategoria(nombre: string | null) {
    this.selectedCategoria = nombre;
    this.pets = nombre
      ? this.allPets.filter(p => p.especie?.toLowerCase() === nombre.toLowerCase())
      : this.allPets;
  }

  getEmoji(especie: string): string {
    const e = especie?.toLowerCase();
    if (e === 'perro')  return '🐶';
    if (e === 'gato')   return '🐱';
    if (e === 'ave')    return '🐦';
    if (e === 'conejo') return '🐰';
    if (e === 'pez')    return '🐟';
    return '🐾';
  }

  isOwner(pet: Pet): boolean {
    return pet.ownerId === this.currentUserId;
  }

  delete(pet: Pet) {
    if (!confirm(`¿Eliminar a ${pet.nombre}?`)) return;
    this.petService.delete(pet.id!).subscribe(() => {
      this.pets = this.pets.filter(p => p.id !== pet.id);
      this.allPets = this.allPets.filter(p => p.id !== pet.id);
    });
  }
}
