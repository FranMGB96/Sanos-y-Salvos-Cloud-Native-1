import { Injectable, signal } from '@angular/core';

export interface ErrorInfo {
  status: number;
  message: string;
  url?: string;
}

@Injectable({ providedIn: 'root' })
export class ErrorToastService {
  current = signal<ErrorInfo | null>(null);
  private timer: any;

  show(info: ErrorInfo) {
    this.current.set(info);
    clearTimeout(this.timer);
    this.timer = setTimeout(() => this.current.set(null), 6000);
  }

  dismiss() {
    clearTimeout(this.timer);
    this.current.set(null);
  }
}
