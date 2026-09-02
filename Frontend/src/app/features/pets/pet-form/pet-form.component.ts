import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { PetService } from '../../../core/services/pet.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-pet-form',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],

  template: `
    <div class="page">

      <div class="page-header">
        <a routerLink="/pets" class="back-link">← Volver</a>
        <h1>{{isEdit ? 'Editar' : 'Nueva'}} Mascota</h1>
      </div>

      <div class="form-card">

        <form [formGroup]="form" (ngSubmit)="onSubmit()">

          <div class="form-row">

            <div class="field">
              <label>Nombre *</label>

              <input
                type="text"
                formControlName="nombre"
                placeholder="Nombre de la mascota">
            </div>

            <div class="field">

              <label>Especie *</label>

              <select formControlName="especie">

                <option value="">Seleccionar...</option>
                <option value="perro">Perro</option>
                <option value="gato">Gato</option>
                <option value="ave">Ave</option>
                <option value="otro">Otro</option>

              </select>
            </div>
          </div>

          <div class="form-row">

            <div class="field">
              <label>Raza</label>

              <input
                type="text"
                formControlName="raza"
                placeholder="Ej: Labrador">
            </div>

            <div class="field">
              <label>Color</label>

              <input
                type="text"
                formControlName="color"
                placeholder="Ej: Café con blanco">
            </div>
          </div>

          <div class="form-row">

            <div class="field">

              <label>Tamaño</label>

              <select formControlName="tamanio">

                <option value="">Seleccionar...</option>
                <option value="PEQUENIO">Pequeño</option>
                <option value="MEDIANO">Mediano</option>
                <option value="GRANDE">Grande</option>

              </select>
            </div>

            <!-- FOTO -->
            <div class="field">

              <label>Foto de la mascota</label>

              <div class="upload-zone"
                   [class.has-image]="previewUrl"
                   (click)="fileInput.click()"
                   (dragover)="$event.preventDefault()"
                   (drop)="onDrop($event)">

                <input #fileInput type="file" accept="image/*"
                       (change)="onFileSelected($event)" style="display:none">

                <ng-container *ngIf="!previewUrl">
                  <div class="upload-icon">📷</div>
                  <p class="upload-text">Haz clic o arrastra una foto aquí</p>
                  <p class="upload-hint">PNG, JPG — máx. 5 MB</p>
                </ng-container>

                <ng-container *ngIf="previewUrl">
                  <img [src]="previewUrl" class="preview-img-zone">
                  <button type="button" class="btn-remove-photo"
                          (click)="removePhoto($event)">✕ Cambiar foto</button>
                </ng-container>

              </div>

            </div>
          </div>

          <div class="field full">

            <label>Descripción</label>

            <textarea
              formControlName="descripcion"
              rows="3"
              placeholder="Características adicionales...">
            </textarea>

          </div>

          <div class="alert-success" *ngIf="successMsg">
            {{successMsg}}
          </div>

          <div class="alert-error" *ngIf="errorMsg">
            {{errorMsg}}
          </div>

          <div class="form-actions">

            <a routerLink="/pets" class="btn-cancel">
              Cancelar
            </a>

            <button type="submit" [disabled]="loading">

              {{loading ? 'Guardando...' : (isEdit ? 'Actualizar' : 'Registrar')}}

            </button>
          </div>

        </form>
      </div>
    </div>
  `,

  styles: [`
    .page{
      padding:2rem;
      max-width:700px;
      margin:0 auto
    }

    .back-link{
      color:#1a237e;
      text-decoration:none;
      font-size:.9rem;
      display:block;
      margin-bottom:.5rem
    }

    h1{
      font-size:1.6rem;
      color:#1a237e;
      margin:0
    }

    .form-card{
      background:white;
      border-radius:12px;
      padding:2rem;
      box-shadow:0 2px 10px rgba(0,0,0,.08)
    }

    .form-row{
      display:grid;
      grid-template-columns:1fr 1fr;
      gap:1rem
    }

    .field{
      margin-bottom:1.2rem
    }

    .field.full{
      grid-column:1/-1
    }

    label{
      display:block;
      font-size:.85rem;
      font-weight:600;
      color:#333;
      margin-bottom:.4rem
    }

    input,select,textarea{
      width:100%;
      padding:.7rem 1rem;
      border:1.5px solid #ddd;
      border-radius:8px;
      font-size:.9rem;
      box-sizing:border-box;
      outline:none;
      transition:border .2s;
      font-family:inherit
    }

    input:focus,
    select:focus,
    textarea:focus{
      border-color:#1a237e
    }

    .upload-zone{
      border: 2px dashed #c5cae9;
      border-radius: 12px;
      padding: 1.5rem 1rem;
      text-align: center;
      cursor: pointer;
      transition: border-color .2s, background .2s;
      background: #fafbff;
      position: relative;
      min-height: 130px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: .3rem;
    }

    .upload-zone:hover{
      border-color: #1a237e;
      background: #f0f4ff;
    }

    .upload-zone.has-image{
      border-style: solid;
      border-color: #1a237e;
      padding: .5rem;
    }

    .upload-icon{ font-size: 2.2rem; line-height: 1; }

    .upload-text{
      margin: 0;
      font-size: .9rem;
      font-weight: 600;
      color: #1a237e;
    }

    .upload-hint{
      margin: 0;
      font-size: .78rem;
      color: #999;
    }

    .preview-img-zone{
      width: 100%;
      max-height: 200px;
      object-fit: contain;
      border-radius: 8px;
      display: block;
    }

    .btn-remove-photo{
      margin-top: .5rem;
      background: #ffebee;
      color: #c62828;
      border: none;
      border-radius: 6px;
      padding: .35rem .85rem;
      font-size: .8rem;
      font-weight: 600;
      cursor: pointer;
      transition: background .2s;
    }

    .btn-remove-photo:hover{ background: #ffcdd2; }

    .alert-success{
      background:#e8f5e9;
      color:#2e7d32;
      padding:.75rem 1rem;
      border-radius:8px;
      margin-bottom:1rem;
      font-size:.85rem
    }

    .alert-error{
      background:#ffebee;
      color:#c62828;
      padding:.75rem 1rem;
      border-radius:8px;
      margin-bottom:1rem;
      font-size:.85rem
    }

    .form-actions{
      display:flex;
      gap:1rem;
      justify-content:flex-end;
      margin-top:1rem
    }

    .btn-cancel{
      padding:.7rem 1.5rem;
      border-radius:8px;
      border:2px solid #ddd;
      color:#666;
      text-decoration:none;
      font-weight:600;
      font-size:.9rem
    }

    button{
      padding:.7rem 2rem;
      background:#1a237e;
      color:white;
      border:none;
      border-radius:8px;
      font-size:.9rem;
      font-weight:600;
      cursor:pointer
    }

    button:disabled{
      opacity:.6
    }
  `]
})

export class PetFormComponent implements OnInit {

  form: FormGroup;

  isEdit = false;

  petId?: number;

  loading = false;

  successMsg = '';

  errorMsg = '';

  selectedFile: File | null = null;

  previewUrl: string | ArrayBuffer | null = null;

  constructor(
    private fb: FormBuilder,
    private petService: PetService,
    private auth: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {

    this.form = this.fb.group({

      nombre: ['', Validators.required],

      especie: ['', Validators.required],

      raza: [''],

      color: [''],

      tamanio: [''],

      descripcion: ['']
    });
  }

  ngOnInit() {

    this.petId = this.route.snapshot.params['id'];

    this.isEdit = !!this.petId;

    if (this.isEdit) {

      this.petService.getById(this.petId!).subscribe(p => {

        this.form.patchValue(p);

        if ((p as any).fotoUrl) {
          this.previewUrl = (p as any).fotoUrl;
        }
      });
    }
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) this.loadFile(file);
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    const file = event.dataTransfer?.files[0];
    if (file) this.loadFile(file);
  }

  removePhoto(event: MouseEvent): void {
    event.stopPropagation();
    this.previewUrl = null;
    this.selectedFile = null;
  }

  private loadFile(file: File): void {
    this.selectedFile = file;
    const reader = new FileReader();
    reader.onload = () => { this.previewUrl = reader.result; };
    reader.readAsDataURL(file);
  }

onSubmit() {
  if (this.form.invalid) {
    this.form.markAllAsTouched();
    return;
  }

  this.loading = true;
  this.errorMsg = '';

  const raw = this.form.value;
  const formData = new FormData();

  formData.append('nombre', raw.nombre);
  formData.append('especie', raw.especie);
  formData.append('raza', raw.raza || '');
  formData.append('color', raw.color || '');
  formData.append('tamanio', raw.tamanio || '');
  formData.append('descripcion', raw.descripcion || '');

  // ✅ ownerId solo al crear, al editar lo toma el backend del JWT
  if (!this.isEdit) {
    formData.append('ownerId', String(this.auth.getCurrentUser()?.userId || ''));
  }

  if (this.selectedFile) {
    formData.append('foto', this.selectedFile);
  }

  const req = this.isEdit
    ? this.petService.update(this.petId!, formData)
    : this.petService.create(formData);

  req.subscribe({
    next: () => {
      this.successMsg = this.isEdit ? 'Mascota actualizada' : 'Mascota registrada';
      setTimeout(() => this.router.navigate(['/pets']), 1200);
    },
    error: () => {
      this.errorMsg = 'Error al guardar.';
      this.loading = false;
    }
  });
}
}