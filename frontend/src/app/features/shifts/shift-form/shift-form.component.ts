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
import { ShiftService } from '../../../core/services/shift.service';
import { DayOfWeek, ShiftType } from '../../../core/models/shift.models';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

const DAYS: { value: DayOfWeek; label: string }[] = [
  { value: 'MONDAY', label: 'Lunes' },
  { value: 'TUESDAY', label: 'Martes' },
  { value: 'WEDNESDAY', label: 'Miércoles' },
  { value: 'THURSDAY', label: 'Jueves' },
  { value: 'FRIDAY', label: 'Viernes' },
  { value: 'SATURDAY', label: 'Sábado' },
  { value: 'SUNDAY', label: 'Domingo' },
];

const SHIFT_TYPES: { value: ShiftType; label: string }[] = [
  { value: 'MORNING', label: 'Mañana' },
  { value: 'AFTERNOON', label: 'Tarde' },
  { value: 'NIGHT', label: 'Noche' },
];

@Component({
  selector: 'app-shift-form',
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
    <app-page-header [title]="isEdit ? 'Editar turno' : 'Nuevo turno'" />

    <mat-card class="form-card">
      <mat-card-content>
        <form [formGroup]="form" (ngSubmit)="onSubmit()" class="form">
          <mat-form-field appearance="outline">
            <mat-label>Nombre del turno</mat-label>
            <input matInput formControlName="name" />
            @if (form.get('name')?.invalid && form.get('name')?.touched) {
              <mat-error>Nombre obligatorio</mat-error>
            }
          </mat-form-field>

          <div class="row">
            <mat-form-field appearance="outline">
              <mat-label>Tipo</mat-label>
              <mat-select formControlName="shiftType">
                @for (t of shiftTypes; track t.value) {
                  <mat-option [value]="t.value">{{ t.label }}</mat-option>
                }
              </mat-select>
            </mat-form-field>

            @if (!isEdit) {
              <mat-form-field appearance="outline">
                <mat-label>Fecha de inicio</mat-label>
                <input matInput type="date" formControlName="effectiveFrom" />
                @if (form.get('effectiveFrom')?.invalid && form.get('effectiveFrom')?.touched) {
                  <mat-error>Fecha obligatoria</mat-error>
                }
              </mat-form-field>
            }
          </div>

          <div class="row">
            <mat-form-field appearance="outline">
              <mat-label>Hora inicio</mat-label>
              <input matInput type="time" formControlName="startTime" />
              @if (form.get('startTime')?.invalid && form.get('startTime')?.touched) {
                <mat-error>Obligatorio</mat-error>
              }
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Hora fin</mat-label>
              <input matInput type="time" formControlName="endTime" />
              @if (form.get('endTime')?.invalid && form.get('endTime')?.touched) {
                <mat-error>Obligatorio</mat-error>
              }
            </mat-form-field>
          </div>

          <mat-form-field appearance="outline">
            <mat-label>Días disponibles</mat-label>
            <mat-select formControlName="availableDays" multiple>
              @for (d of days; track d.value) {
                <mat-option [value]="d.value">{{ d.label }}</mat-option>
              }
            </mat-select>
            @if (form.get('availableDays')?.invalid && form.get('availableDays')?.touched) {
              <mat-error>Selecciona al menos un día</mat-error>
            }
          </mat-form-field>

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
    .form-card { max-width: 600px; }
    .form { display: flex; flex-direction: column; gap: 8px; }
    .row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
    .actions { display: flex; gap: 8px; justify-content: flex-end; }
    .error-msg { color: #ef4444; font-size: 0.875rem; }
  `],
})
export class ShiftFormComponent implements OnInit {
  private readonly shiftService = inject(ShiftService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly days = DAYS;
  readonly shiftTypes = SHIFT_TYPES;

  isEdit = false;
  private storeId = '';
  private shiftId = '';

  readonly form = this.fb.group({
    name: ['', Validators.required],
    shiftType: ['MORNING' as ShiftType, Validators.required],
    startTime: ['', Validators.required],
    endTime: ['', Validators.required],
    effectiveFrom: [''],
    availableDays: [[] as DayOfWeek[], Validators.required],
  });

  ngOnInit(): void {
    this.storeId = this.route.snapshot.paramMap.get('storeId') ?? '';
    this.shiftId = this.route.snapshot.paramMap.get('shiftId') ?? '';
    this.isEdit = !!this.shiftId;

    if (this.isEdit) {
      this.shiftService.findById(this.storeId, this.shiftId).subscribe((s) => {
        this.form.patchValue({
          name: s.name,
          shiftType: s.shiftType,
          startTime: s.startTime,
          endTime: s.endTime,
          availableDays: s.availableDays,
        });
      });
    } else {
      this.form.get('effectiveFrom')?.setValidators(Validators.required);
      this.form.get('effectiveFrom')?.updateValueAndValidity();
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    const v = this.form.getRawValue();

    const obs = this.isEdit
      ? this.shiftService.update(this.storeId, this.shiftId, {
          name: v.name!,
          startTime: v.startTime!,
          endTime: v.endTime!,
          shiftType: v.shiftType as ShiftType,
          availableDays: v.availableDays as DayOfWeek[],
        })
      : this.shiftService.create(this.storeId, {
          name: v.name!,
          startTime: v.startTime!,
          endTime: v.endTime!,
          effectiveFrom: v.effectiveFrom!,
          shiftType: v.shiftType as ShiftType,
          availableDays: v.availableDays as DayOfWeek[],
        });

    obs.subscribe({
      next: () => {
        this.snackBar.open('Turno guardado', 'OK', { duration: 3000 });
        this.router.navigate(['/stores', this.storeId]);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message ?? 'Error al guardar el turno.');
      },
      complete: () => this.loading.set(false),
    });
  }

  cancel(): void {
    this.router.navigate(['/stores', this.storeId]);
  }
}
