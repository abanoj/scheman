import { Component, inject, signal, computed } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatToolbarModule } from '@angular/material/toolbar';
import { AuthService } from '../../../core/auth/auth.service';
import { TokenService } from '../../../core/auth/token.service';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d]).{8,}$/;

function passwordsMatchValidator(group: AbstractControl): ValidationErrors | null {
  const newPwd = group.get('newPassword')?.value;
  const repeat = group.get('repeatNewPassword')?.value;
  return newPwd && repeat && newPwd !== repeat ? { passwordsMismatch: true } : null;
}

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatToolbarModule,
    PageHeaderComponent,
  ],
  template: `
    <div class="page-wrapper">
      <mat-toolbar class="top-bar">
        <div class="brand">
          <div class="brand-icon">S</div>
          <span class="brand-name">Scheman</span>
        </div>
        <button mat-stroked-button (click)="logout()">
          <mat-icon>logout</mat-icon> Cerrar sesión
        </button>
      </mat-toolbar>

      <div class="page-inner">
        <app-page-header
          title="Cambiar contraseña"
          subtitle="Elige una contraseña segura de al menos 8 caracteres"
        />

        @if (isForced()) {
          <div class="forced-banner">
            <mat-icon>lock_reset</mat-icon>
            <div>
              <strong>Debes cambiar tu contraseña antes de continuar</strong>
              <span>Es la primera vez que accedes al sistema. Establece una contraseña personal para poder usar la aplicación.</span>
            </div>
          </div>
        }

        <mat-card class="form-card">
          <mat-card-content>
            <form [formGroup]="form" (ngSubmit)="onSubmit()" class="form">

          <mat-form-field appearance="outline">
            <mat-label>Contraseña actual</mat-label>
            <input matInput type="password" formControlName="oldPassword" autocomplete="current-password" />
            <mat-icon matSuffix>lock_outline</mat-icon>
            @if (form.get('oldPassword')?.hasError('required') && form.get('oldPassword')?.touched) {
              <mat-error>Campo obligatorio</mat-error>
            }
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Nueva contraseña</mat-label>
            <input matInput type="password" formControlName="newPassword" autocomplete="new-password" />
            <mat-icon matSuffix>lock_reset</mat-icon>
            @if (form.get('newPassword')?.hasError('required') && form.get('newPassword')?.touched) {
              <mat-error>Campo obligatorio</mat-error>
            }
            @if (form.get('newPassword')?.hasError('pattern') && form.get('newPassword')?.touched) {
              <mat-error>Mín. 8 caracteres con mayúscula, minúscula, número y símbolo (ej: @, !, #, $, -)</mat-error>
            }
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Repetir nueva contraseña</mat-label>
            <input matInput type="password" formControlName="repeatNewPassword" autocomplete="new-password" />
            <mat-icon matSuffix>lock_reset</mat-icon>
            @if (form.hasError('passwordsMismatch') && form.get('repeatNewPassword')?.touched) {
              <mat-error>Las contraseñas no coinciden</mat-error>
            }
          </mat-form-field>

          @if (errorMessage()) {
            <div class="error-banner">
              <mat-icon>error_outline</mat-icon>
              <span>{{ errorMessage() }}</span>
            </div>
          }

          <div class="actions">
            @if (!isForced()) {
              <button mat-stroked-button type="button" (click)="cancel()">Cancelar</button>
            }
            <button
              mat-flat-button
              color="primary"
              type="submit"
              [disabled]="loading() || form.invalid"
            >
              @if (loading()) { <mat-spinner diameter="20" /> } @else { Guardar contraseña }
            </button>
          </div>
              </form>
            </mat-card-content>
          </mat-card>
        </div>
      </div>
  `,
  styles: [`
    .page-wrapper { min-height: 100vh; background: #f1f5f9; display: flex; flex-direction: column; }

    .top-bar {
      background: #1e293b !important; color: #f8fafc;
      display: flex; justify-content: space-between; align-items: center;
      padding: 0 24px; height: 56px; flex-shrink: 0;
      box-shadow: 0 1px 4px rgba(0,0,0,0.2);
    }
    .brand { display: flex; align-items: center; gap: 10px; }
    .brand-icon {
      width: 30px; height: 30px; background: #3b82f6; border-radius: 7px;
      display: flex; align-items: center; justify-content: center;
      font-size: 1rem; font-weight: 800; color: #fff;
    }
    .brand-name { font-size: 1.05rem; font-weight: 700; color: #f8fafc; letter-spacing: 0.02em; }
    .top-bar button { color: #cbd5e1; border-color: #334155; font-size: 0.85rem; }
    .top-bar button mat-icon { font-size: 1rem; width: 1rem; height: 1rem; margin-right: 4px; }

    .page-inner {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 48px 32px;
      width: 100%;
      box-sizing: border-box;
    }
    .page-inner app-page-header,
    .page-inner .forced-banner,
    .page-inner mat-card { width: 100%; max-width: 480px; }

    .form-card { max-width: 480px; }
    .form { display: flex; flex-direction: column; gap: 8px; }
    .actions { display: flex; gap: 8px; justify-content: flex-end; margin-top: 8px; }

    .forced-banner {
      display: flex; align-items: flex-start; gap: 14px;
      max-width: 480px; margin-bottom: 20px;
      background: #fff7ed; border: 1px solid #fed7aa; border-left: 4px solid #f97316;
      border-radius: 10px; padding: 16px;
      color: #7c2d12;
    }
    .forced-banner mat-icon { color: #f97316; flex-shrink: 0; font-size: 1.5rem; width: 1.5rem; height: 1.5rem; margin-top: 1px; }
    .forced-banner div { display: flex; flex-direction: column; gap: 4px; }
    .forced-banner strong { font-size: 0.9rem; font-weight: 600; color: #7c2d12; }
    .forced-banner span { font-size: 0.845rem; color: #92400e; line-height: 1.5; }

    .error-banner {
      display: flex; align-items: center; gap: 8px;
      background: #fef2f2; border: 1px solid #fecaca;
      border-radius: 8px; padding: 10px 14px;
      color: #dc2626; font-size: 0.875rem;
    }
    .error-banner mat-icon { font-size: 1.1rem; width: 1.1rem; height: 1.1rem; }
  `],
})
export class ChangePasswordComponent {
  private readonly authService = inject(AuthService);
  private readonly tokenService = inject(TokenService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly isForced = computed(() => this.tokenService.currentUser()?.mustChangePassword ?? false);

  readonly form = this.fb.group(
    {
      oldPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.pattern(PASSWORD_PATTERN)]],
      repeatNewPassword: ['', Validators.required],
    },
    { validators: passwordsMatchValidator }
  );

  onSubmit(): void {
    if (this.form.invalid) return;
    const userId = this.tokenService.currentUser()?.id;
    if (!userId) return;

    this.loading.set(true);
    this.errorMessage.set('');
    const { oldPassword, newPassword, repeatNewPassword } = this.form.getRawValue();

    this.authService
      .changePassword(userId, {
        oldPassword: oldPassword!,
        newPassword: newPassword!,
        repeatNewPassword: repeatNewPassword!,
      })
      .subscribe({
        next: () => {
          this.snackBar.open('Contraseña actualizada correctamente', 'OK', { duration: 3000 });
          this.router.navigate(['/dashboard']);
        },
        error: () => {
          this.loading.set(false);
          this.errorMessage.set('La contraseña actual no es correcta.');
        },
        complete: () => this.loading.set(false),
      });
  }

  cancel(): void {
    this.router.navigate(['/dashboard']);
  }

  logout(): void {
    this.authService.logout();
  }
}
