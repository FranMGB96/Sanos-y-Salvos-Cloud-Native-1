import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ErrorToastService } from '../../../core/services/error-toast.service';

@Component({
  selector: 'app-error-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="error-toast" *ngIf="toast.current() as e">
      <div class="error-toast-header">
        <span class="error-code">HTTP {{ e.status }}</span>
        <button class="close-btn" (click)="toast.dismiss()">✕</button>
      </div>
      <div class="error-toast-body">{{ e.message }}</div>
    </div>
  `,
  styles: [`
    .error-toast{position:fixed;bottom:20px;right:20px;background:#fff;border-left:4px solid #c62828;border-radius:8px;box-shadow:0 4px 16px rgba(0,0,0,.2);padding:.75rem 1rem;min-width:260px;max-width:360px;z-index:9999;animation:slideIn .25s ease-out}
    .error-toast-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:.35rem}
    .error-code{font-weight:700;color:#c62828;font-size:.9rem}
    .close-btn{background:none;border:none;cursor:pointer;font-size:.9rem;color:#999}
    .close-btn:hover{color:#333}
    .error-toast-body{font-size:.85rem;color:#444}
    @keyframes slideIn{from{transform:translateX(20px);opacity:0}to{transform:translateX(0);opacity:1}}
  `]
})
export class ErrorToastComponent {
  constructor(public toast: ErrorToastService) {}
}
