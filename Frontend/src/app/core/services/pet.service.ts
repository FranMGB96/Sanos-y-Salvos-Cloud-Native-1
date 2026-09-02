import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Pet } from '../models/pet.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })

export class PetService {

  private apiUrl = `${environment.apiUrl}/pets`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Pet[]> {
    return this.http.get<Pet[]>(this.apiUrl);
  }

  getById(id: number): Observable<Pet> {
    return this.http.get<Pet>(`${this.apiUrl}/${id}`);
  }

  getByOwner(ownerId: number): Observable<Pet[]> {
    return this.http.get<Pet[]>(`${this.apiUrl}/owner/${ownerId}`);
  }

  create(pet: FormData): Observable<Pet> {
    return this.http.post<Pet>(this.apiUrl, pet);
  }

  update(id: number, pet: FormData): Observable<Pet> {
    return this.http.put<Pet>(`${this.apiUrl}/${id}`, pet);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}