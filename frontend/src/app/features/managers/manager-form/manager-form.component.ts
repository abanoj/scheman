import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ManagerService } from '../../../core/services/manager.service';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d]).{8,}$/;

@Component({
  selector: 'app-manager-form',
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
    PageHeaderComponent,
  ],
  template: `
    <app-page-header
      [title]="isEdit ? 'Editar Manager' : 'Crear Manager'"
      [subtitle]="isEdit ? 'Modifica los datos del manager' : 'Registra una nueva cuenta con rol de Manager'"
    />

    <mat-card class="form-card">
      <mat-card-content>
        <form [formGroup]="form" (ngSubmit)="onSubmit()" class="form">
          <div class="row">
            <mat-form-field appearance="outline">
              <mat-label>Nombre</mat-label>
              <input matInput formControlName="firstName" />
              @if (form.get('firstName')?.invalid && form.get('firstName')?.touched) {
                <mat-error>Nombre obligatorio</mat-error>
              }
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Apellidos</mat-label>
              <input matInput formControlName="lastName" />
              @if (form.get('lastName')?.invalid && form.get('lastName')?.touched) {
                <mat-error>Apellidos obligatorios</mat-error>
              }
            </mat-form-field>
          </div>

          <mat-form-field appearance="outline">
            <mat-label>Email</mat-label>
            <input matInput type="email" formControlName="email" autocomplete="off" />
            <mat-icon matSuffix>mail_outline</mat-icon>
            @if (form.get('email')?.hasError('required') && form.get('email')?.touched) {
              <mat-error>Email obligatorio</mat-error>
            }
            @if (form.get('email')?.hasError('email') && form.get('email')?.touched) {
              <mat-error>Introduce un email válido</mat-error>
            }
          </mat-form-field>

          @if (!isEdit) {
            <mat-form-field appearance="outline">
              <mat-label>Contraseña inicial</mat-label>
              <input matInput [type]="showPwd() ? 'text' : 'password'" formControlName="password" autocomplete="new-password" />
              <button mat-icon-button matSuffix type="button" (click)="showPwd.set(!showPwd())">
                <mat-icon>{{ showPwd() ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              @if (form.get('password')?.hasError('required') && form.get('password')?.touched) {
                <mat-error>Contraseña obligatoria</mat-error>
              }
              @if (form.get('password')?.hasError('pattern') && form.get('password')?.touched) {
                <mat-error>Mín. 8 caracteres con mayúscula, minúscula, número y símbolo</mat-error>
              }
            </mat-form-field>

            <div class="info-box">
              <mat-icon>info_outline</mat-icon>
              <span>El manager podrá cambiar su contraseña desde su perfil una vez que acceda al sistema.</span>
            </div>
          }

          @if (errorMessage()) {
            <div class="error-banner">
              <mat-icon>error_outline</mat-icon>
              <span>{{ errorMessage() }}</span>
            </div>
          }

          <div class="actions">
            <button mat-stroked-button type="button" (click)="cancel()">Cancelar</button>
            <button
              mat-flat-button
              color="primary"
              type="submit"
              [disabled]="loading() || form.invalid"
            >
              @if (loading()) { <mat-spinner diameter="20" /> } @else { {{ isEdit ? 'Guardar' : 'Crear Manager' }} }
            </button>
          </div>
        </form>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .form-card { max-width: 600px; }
    .form { display: flex; flex-direction: column; gap: 8px; }
    .row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
    @media (max-width: 600px) {
      .form-card { max-width: 100%; }
      .row { grid-template-columns: 1fr; }
    }
    .actions { display: flex; gap: 8px; justify-content: flex-end; margin-top: 8px; }
    .info-box {
      display: flex; align-items: flex-start; gap: 8px;
      background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 8px;
      padding: 10px 14px; color: #1d4ed8; font-size: 0.875rem;
    }
    .info-box mat-icon { font-size: 1.1rem; width: 1.1rem; height: 1.1rem; flex-shrink: 0; margin-top: 1px; }
    .error-banner {
      display: flex; align-items: center; gap: 8px;
      background: #fef2f2; border: 1px solid #fecaca; border-radius: 8px;
      padding: 10px 14px; color: #dc2626; font-size: 0.875rem;
    }
    .error-banner mat-icon { font-size: 1.1rem; width: 1.1rem; height: 1.1rem; }
  `],
})
export class ManagerFormComponent implements OnInit {
  private readonly managerService = inject(ManagerService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly showPwd = signal(false);

  isEdit = false;
  private managerId = '';

  readonly form = this.fb.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.pattern(PASSWORD_PATTERN)]],
  });

  ngOnInit(): void {
    this.managerId = this.route.snapshot.paramMap.get('id') ?? '';
    this.isEdit = this.managerId !== '';

    if (this.isEdit) {
      this.form.get('password')?.clearValidators();
      this.form.get('password')?.updateValueAndValidity();
      this.form.get('email')?.disable();

      this.managerService.findById(this.managerId).subscribe((manager) => {
        this.form.patchValue({ firstName: manager.firstName, lastName: manager.lastName, email: manager.email });
      });
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.errorMessage.set('');
    const v = this.form.getRawValue();

    const obs = this.isEdit
      ? this.managerService.update(this.managerId, { firstName: v.firstName!, lastName: v.lastName! })
      : this.managerService.create({ firstName: v.firstName!, lastName: v.lastName!, email: v.email!, password: v.password! });

    obs.subscribe({
      next: () => {
        this.snackBar.open(this.isEdit ? 'Manager actualizado' : 'Manager creado correctamente', 'OK', { duration: 3000 });
        this.router.navigate(['/managers']);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(
          err.status === 409
            ? 'Ya existe una cuenta con ese email.'
            : err.error?.message ?? 'Error al guardar el manager.'
        );
      },
      complete: () => this.loading.set(false),
    });
  }

  cancel(): void {
    this.router.navigate(['/managers']);
  }
}
