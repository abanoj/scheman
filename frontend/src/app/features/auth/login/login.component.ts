import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/auth/auth.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <div class="login-wrapper">
      <div class="login-card">
        <div class="login-header">
          <div class="logo-icon">S</div>
          <h1 class="brand">Scheman</h1>
          <p class="subtitle">Gestión de horarios</p>
        </div>

        <form [formGroup]="form" (ngSubmit)="onSubmit()" class="login-form">
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Email</mat-label>
            <input matInput type="email" formControlName="email" autocomplete="email" />
            <mat-icon matSuffix>mail_outline</mat-icon>
            @if (form.get('email')?.hasError('required') && form.get('email')?.touched) {
              <mat-error>El email es obligatorio</mat-error>
            }
            @if (form.get('email')?.hasError('email') && form.get('email')?.touched) {
              <mat-error>Introduce un email válido</mat-error>
            }
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Contraseña</mat-label>
            <input
              matInput
              [type]="showPassword() ? 'text' : 'password'"
              formControlName="password"
              autocomplete="current-password"
            />
            <button
              mat-icon-button
              matSuffix
              type="button"
              (click)="showPassword.set(!showPassword())"
              [attr.aria-label]="showPassword() ? 'Ocultar contraseña' : 'Mostrar contraseña'"
            >
              <mat-icon>{{ showPassword() ? 'visibility_off' : 'visibility' }}</mat-icon>
            </button>
            @if (form.get('password')?.hasError('required') && form.get('password')?.touched) {
              <mat-error>La contraseña es obligatoria</mat-error>
            }
          </mat-form-field>

          @if (errorMessage()) {
            <div class="error-banner">
              <mat-icon>error_outline</mat-icon>
              <span>{{ errorMessage() }}</span>
            </div>
          }

          <button
            mat-flat-button
            class="submit-btn"
            type="submit"
            [disabled]="loading() || form.invalid"
          >
            @if (loading()) {
              <mat-spinner diameter="22" />
            } @else {
              Iniciar sesión
            }
          </button>
        </form>

        <div class="demo-section">
          <div class="demo-divider">
            <span>Acceso rápido · Demo</span>
          </div>
          <div class="demo-buttons">
            <button mat-stroked-button class="demo-btn" (click)="loginAsDemo('admin')">
              <mat-icon>admin_panel_settings</mat-icon> Admin
            </button>
            <button mat-stroked-button class="demo-btn" (click)="loginAsDemo('manager')">
              <mat-icon>manage_accounts</mat-icon> Manager
            </button>
            <button mat-stroked-button class="demo-btn" (click)="loginAsDemo('employee')">
              <mat-icon>badge</mat-icon> Empleado
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .login-wrapper {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #0f172a 0%, #1e3a5f 100%);
      padding: 24px;
    }

    .login-card {
      background: #ffffff;
      border-radius: 16px;
      padding: 40px 36px;
      width: 100%;
      max-width: 420px;
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4);
    }

    .login-header {
      text-align: center;
      margin-bottom: 32px;
    }

    .logo-icon {
      width: 56px;
      height: 56px;
      background: #2563eb;
      border-radius: 14px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      font-size: 1.8rem;
      font-weight: 800;
      color: #ffffff;
      margin-bottom: 12px;
    }

    .brand {
      margin: 0 0 4px;
      font-size: 1.75rem;
      font-weight: 700;
      color: #0f172a;
    }

    .subtitle {
      margin: 0;
      color: #64748b;
      font-size: 0.9rem;
    }

    .login-form {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .full-width {
      width: 100%;
    }

    .error-banner {
      display: flex;
      align-items: center;
      gap: 8px;
      background: #fef2f2;
      border: 1px solid #fecaca;
      border-radius: 8px;
      padding: 10px 14px;
      color: #dc2626;
      font-size: 0.875rem;
    }

    .error-banner mat-icon {
      font-size: 1.1rem;
      width: 1.1rem;
      height: 1.1rem;
      flex-shrink: 0;
    }

    .submit-btn {
      width: 100%;
      height: 48px;
      background: #2563eb !important;
      color: #ffffff !important;
      font-size: 1rem;
      font-weight: 600;
      border-radius: 10px !important;
      margin-top: 8px;
      letter-spacing: 0.02em;
    }

    .submit-btn:disabled {
      background: #bfdbfe !important;
      color: #93c5fd !important;
    }

    .submit-btn mat-spinner {
      margin: 0 auto;
    }

    .demo-section {
      margin-top: 24px;
    }

    .demo-divider {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 14px;
    }
    .demo-divider::before,
    .demo-divider::after {
      content: '';
      flex: 1;
      height: 1px;
      background: #e2e8f0;
    }
    .demo-divider span {
      font-size: 0.75rem;
      color: #94a3b8;
      white-space: nowrap;
      font-weight: 500;
      letter-spacing: 0.04em;
    }

    .demo-buttons {
      display: flex;
      gap: 8px;
    }

    .demo-btn {
      flex: 1;
      font-size: 0.8rem !important;
      border-color: #e2e8f0 !important;
      color: #64748b !important;
      height: 38px;
    }
    .demo-btn:hover {
      background: #f8fafc !important;
      color: #334155 !important;
    }
    .demo-btn mat-icon {
      font-size: 1rem;
      width: 1rem;
      height: 1rem;
      margin-right: 4px;
    }
  `],
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  readonly showPassword = signal(false);
  readonly loading = signal(false);
  readonly errorMessage = signal('');

  readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  loginAsDemo(role: 'admin' | 'manager' | 'employee'): void {
    const credentials = {
      admin:    { email: 'admin@demo.com',    password: 'Demo@2026' },
      manager:  { email: 'manager@demo.com',  password: 'Demo@2026' },
      employee: { email: 'empleado@demo.com', password: 'Demo@2026' },
    };
    this.form.setValue(credentials[role]);
    this.onSubmit();
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.errorMessage.set('');

    const { email, password } = this.form.getRawValue();
    this.authService.login({ email: email!, password: password! }).subscribe({
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(
          err.status === 401
            ? 'Email o contraseña incorrectos'
            : 'Error de conexión. Inténtalo de nuevo.'
        );
      },
      complete: () => this.loading.set(false),
    });
  }
}
