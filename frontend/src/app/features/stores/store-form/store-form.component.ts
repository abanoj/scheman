import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { StoreService } from '../../../core/services/store.service';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

const PHONE_PATTERN = /^[6-9][0-9]{8}$/;

@Component({
  selector: 'app-store-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatButtonModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    PageHeaderComponent,
  ],
  template: `
    <app-page-header
      [title]="isEdit ? 'Editar tienda' : 'Nueva tienda'"
    />

    <mat-card class="form-card">
      <mat-card-content>
        <form [formGroup]="form" (ngSubmit)="onSubmit()" class="form">
          <mat-form-field appearance="outline">
            <mat-label>Nombre</mat-label>
            <input matInput formControlName="name" />
            @if (form.get('name')?.invalid && form.get('name')?.touched) {
              <mat-error>Nombre obligatorio</mat-error>
            }
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Dirección</mat-label>
            <input matInput formControlName="address" />
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Teléfono</mat-label>
            <input matInput formControlName="phone" placeholder="6XXXXXXXX" />
            @if (form.get('phone')?.hasError('pattern') && form.get('phone')?.touched) {
              <mat-error>Introduce un teléfono español válido (6-9, 9 dígitos)</mat-error>
            }
          </mat-form-field>

          <mat-checkbox formControlName="is24h" color="primary">Tienda 24 horas</mat-checkbox>

          @if (errorMessage()) {
            <p class="error-msg">{{ errorMessage() }}</p>
          }

          <div class="actions">
            <button mat-stroked-button type="button" (click)="cancel()">Cancelar</button>
            <button
              mat-raised-button
              color="primary"
              type="submit"
              [disabled]="loading() || form.invalid"
            >
              @if (loading()) { <mat-spinner diameter="20" /> } @else { Guardar }
            </button>
          </div>
        </form>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .form-card { max-width: 520px; }
    .form { display: flex; flex-direction: column; gap: 8px; }
    .actions { display: flex; gap: 8px; justify-content: flex-end; margin-top: 8px; }
    .error-msg { color: #ef4444; font-size: 0.875rem; }
  `],
})
export class StoreFormComponent implements OnInit {
  private readonly storeService = inject(StoreService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(false);
  readonly errorMessage = signal('');
  isEdit = false;
  private storeId = '';

  readonly form = this.fb.group({
    name: ['', Validators.required],
    address: [''],
    phone: ['', Validators.pattern(PHONE_PATTERN)],
    is24h: [false],
  });

  ngOnInit(): void {
    this.storeId = this.route.snapshot.paramMap.get('id') ?? '';
    this.isEdit = this.storeId !== '' && this.storeId !== 'new';

    if (this.isEdit) {
      this.storeService.findById(this.storeId).subscribe((s) => {
        this.form.patchValue({ name: s.name, address: s.address, phone: s.phone, is24h: s.is24h });
      });
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    const v = this.form.getRawValue();

    const obs = this.isEdit
      ? this.storeService.update(this.storeId, {
          name: v.name ?? undefined,
          address: v.address || undefined,
          phone: v.phone || undefined,
          is24h: v.is24h ?? undefined,
        })
      : this.storeService.create({
          name: v.name!,
          address: v.address || undefined,
          phone: v.phone || undefined,
          is24h: v.is24h ?? undefined,
        });

    obs.subscribe({
      next: () => {
        this.snackBar.open('Tienda guardada', 'OK', { duration: 3000 });
        this.router.navigate(['/stores']);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message ?? 'Error al guardar la tienda.');
      },
      complete: () => this.loading.set(false),
    });
  }

  cancel(): void {
    this.router.navigate(['/stores']);
  }
}
