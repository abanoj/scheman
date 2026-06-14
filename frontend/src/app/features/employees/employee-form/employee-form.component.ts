import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { EmployeeService } from '../../../core/services/employee.service';
import { StoreService } from '../../../core/services/store.service';
import { StoreListResponse } from '../../../core/models/store.models';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

const SHIFT_TYPES = [
  { value: 'MORNING', label: 'Mañana' },
  { value: 'AFTERNOON', label: 'Tarde' },
  { value: 'NIGHT', label: 'Noche' },
];

@Component({
  selector: 'app-employee-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    PageHeaderComponent,
  ],
  template: `
    <app-page-header
      [title]="isEdit ? 'Editar empleado' : 'Nuevo empleado'"
      [subtitle]="isEdit ? 'Modifica los datos del empleado' : 'Registra un nuevo empleado en el sistema'"
    />

    <mat-card class="form-card">
      <mat-card-content>
        <form [formGroup]="form" (ngSubmit)="onSubmit()" class="form">
          <div class="row">
            <mat-form-field appearance="outline">
              <mat-label>Nombre</mat-label>
              <input matInput formControlName="firstName" />
              @if (form.get('firstName')?.invalid && form.get('firstName')?.touched) {
                <mat-error>Nombre obligatorio (máx. 100 caracteres)</mat-error>
              }
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Apellidos</mat-label>
              <input matInput formControlName="lastName" />
              @if (form.get('lastName')?.invalid && form.get('lastName')?.touched) {
                <mat-error>Apellidos obligatorios (máx. 100 caracteres)</mat-error>
              }
            </mat-form-field>
          </div>

          <div class="row">
            <mat-form-field appearance="outline">
              <mat-label>DNI</mat-label>
              <input matInput formControlName="dni" maxlength="9" />
              @if (form.get('dni')?.hasError('minlength') && form.get('dni')?.touched) {
                <mat-error>El DNI debe tener 9 caracteres</mat-error>
              }
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Email</mat-label>
              <input matInput type="email" formControlName="email" autocomplete="off" />
              @if (form.get('email')?.invalid && form.get('email')?.touched) {
                <mat-error>Email válido obligatorio</mat-error>
              }
            </mat-form-field>
          </div>

          <div class="row">
            <mat-form-field appearance="outline">
              <mat-label>Turno preferente</mat-label>
              <mat-select formControlName="preferredShift">
                @for (s of shiftTypes; track s.value) {
                  <mat-option [value]="s.value">{{ s.label }}</mat-option>
                }
              </mat-select>
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Horas/semana</mat-label>
              <input matInput type="number" formControlName="weeklyContractedHours" min="1" />
            </mat-form-field>
          </div>

          <mat-form-field appearance="outline">
            <mat-label>Tiendas preferentes</mat-label>
            <mat-select formControlName="preferredStoresIds" multiple>
              @for (store of stores(); track store.id) {
                <mat-option [value]="store.id">{{ store.name }}</mat-option>
              }
            </mat-select>
          </mat-form-field>

          @if (!isEdit) {
            <p class="hint">
              La contraseña inicial del empleado será su DNI y deberá cambiarla en el primer acceso.
            </p>
          }

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
    .form-card { max-width: 700px; }
    .form { display: flex; flex-direction: column; gap: 8px; }
    .row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
    .actions { display: flex; gap: 8px; justify-content: flex-end; margin-top: 8px; }
    .hint { color: #64748b; font-size: 0.85rem; margin: 0; }
    .error-msg { color: #ef4444; font-size: 0.875rem; }
  `],
})
export class EmployeeFormComponent implements OnInit {
  private readonly employeeService = inject(EmployeeService);
  private readonly storeService = inject(StoreService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly stores = signal<StoreListResponse[]>([]);
  readonly shiftTypes = SHIFT_TYPES;

  isEdit = false;
  private employeeId = '';

  readonly form = this.fb.group({
    firstName: ['', [Validators.required, Validators.maxLength(100)]],
    lastName: ['', [Validators.required, Validators.maxLength(100)]],
    dni: ['', [Validators.required, Validators.minLength(9), Validators.maxLength(9)]],
    email: ['', [Validators.required, Validators.email]],
    preferredShift: ['MORNING', Validators.required],
    weeklyContractedHours: [40, [Validators.required, Validators.min(1)]],
    preferredStoresIds: [[] as string[]],
  });

  ngOnInit(): void {
    this.employeeId = this.route.snapshot.paramMap.get('id') ?? '';
    this.isEdit = this.employeeId !== '' && this.employeeId !== 'new';

    this.storeService.findAll({ size: 100 }).subscribe((p) => this.stores.set(p.content));

    if (this.isEdit) {
      this.employeeService.findById(this.employeeId).subscribe((emp) => {
        this.form.patchValue({
          firstName: emp.firstName,
          lastName: emp.lastName,
          dni: emp.dni,
          email: emp.email,
          preferredShift: emp.preferredShift,
          weeklyContractedHours: emp.weeklyContractedHours,
          preferredStoresIds: emp.preferredStores.map((s) => s.id),
        });
        this.form.get('email')?.disable();
      });
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.errorMessage.set('');
    const v = this.form.getRawValue();

    const obs = this.isEdit
      ? this.employeeService.update(this.employeeId, {
          firstName: v.firstName ?? undefined,
          lastName: v.lastName ?? undefined,
          dni: v.dni ?? undefined,
          preferredShift: (v.preferredShift as any) ?? undefined,
          weeklyContractedHours: v.weeklyContractedHours ?? undefined,
          preferredStoresIds: v.preferredStoresIds ?? undefined,
        })
      : this.employeeService.create({
          firstName: v.firstName!,
          lastName: v.lastName!,
          dni: v.dni!,
          email: v.email!,
          preferredShift: v.preferredShift as any,
          weeklyContractedHours: v.weeklyContractedHours!,
          preferredStoresIDs: v.preferredStoresIds ?? [],
        });

    obs.subscribe({
      next: () => {
        this.snackBar.open('Empleado guardado', 'OK', { duration: 3000 });
        this.router.navigate(['/employees']);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message ?? 'Error al guardar el empleado.');
      },
      complete: () => this.loading.set(false),
    });
  }

  cancel(): void {
    this.router.navigate(['/employees']);
  }
}
