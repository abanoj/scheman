import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { provideNativeDateAdapter, MAT_DATE_LOCALE } from '@angular/material/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Subscription, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { ShiftAssignmentService } from '../../../core/services/shift-assignment.service';
import { ShiftService } from '../../../core/services/shift.service';
import { EmployeeService } from '../../../core/services/employee.service';
import { EmployeeResponse } from '../../../core/models/employee.models';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-assignment-form',
  standalone: true,
  providers: [
    provideNativeDateAdapter(),
    { provide: MAT_DATE_LOCALE, useValue: 'es-ES' },
  ],
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatDatepickerModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    PageHeaderComponent,
  ],
  template: `
    <app-page-header
      [title]="isEdit ? 'Editar asignación' : 'Asignar empleado'"
      [subtitle]="shiftName() ? 'Turno: ' + shiftName() : undefined"
    />

    <mat-card class="form-card">
      <mat-card-content>
        <form [formGroup]="form" (ngSubmit)="onSubmit()" class="form">
          <mat-form-field appearance="outline">
            <mat-label>Empleado</mat-label>
            <mat-select formControlName="employeeId">
              @for (emp of employees(); track emp.id) {
                <mat-option [value]="emp.id">{{ emp.firstName }} {{ emp.lastName }} ({{ emp.dni }})</mat-option>
              }
            </mat-select>
            <mat-icon matSuffix>person</mat-icon>
            @if (form.get('employeeId')?.invalid && form.get('employeeId')?.touched) {
              <mat-error>Selecciona un empleado</mat-error>
            }
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Fecha</mat-label>
            <input
              matInput
              [matDatepicker]="picker"
              formControlName="date"
              (click)="picker.open()"
              readonly
            />
            <mat-datepicker-toggle matSuffix [for]="picker" />
            <mat-datepicker #picker />
            @if (form.get('date')?.invalid && form.get('date')?.touched) {
              <mat-error>Fecha obligatoria</mat-error>
            }
          </mat-form-field>

          @if (hoursWarning()) {
            <div class="hours-warning">
              <mat-icon>warning</mat-icon>
              <div>
                <strong>Aviso: horas semanales superadas</strong>
                <span>{{ hoursWarning() }}</span>
              </div>
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
              @if (loading()) { <mat-spinner diameter="20" /> } @else { Guardar }
            </button>
          </div>
        </form>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .form-card { max-width: 480px; }
    .form { display: flex; flex-direction: column; gap: 12px; }
    .actions { display: flex; gap: 8px; justify-content: flex-end; }

    .hours-warning {
      display: flex; align-items: flex-start; gap: 10px;
      background: #fffbeb; border: 1px solid #fde68a; border-left: 4px solid #f59e0b;
      border-radius: 8px; padding: 12px 14px; color: #92400e;
    }
    .hours-warning mat-icon { color: #f59e0b; flex-shrink: 0; margin-top: 1px; }
    .hours-warning div { display: flex; flex-direction: column; gap: 2px; font-size: 0.875rem; }
    .hours-warning strong { font-weight: 600; }

    .error-banner {
      display: flex; align-items: center; gap: 8px;
      background: #fef2f2; border: 1px solid #fecaca; border-radius: 8px;
      padding: 10px 14px; color: #dc2626; font-size: 0.875rem;
    }
    .error-banner mat-icon { font-size: 1.1rem; width: 1.1rem; height: 1.1rem; }
  `],
})
export class AssignmentFormComponent implements OnInit, OnDestroy {
  private readonly assignmentService = inject(ShiftAssignmentService);
  private readonly shiftService = inject(ShiftService);
  private readonly employeeService = inject(EmployeeService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly employees = signal<EmployeeResponse[]>([]);
  readonly shiftName = signal('');
  readonly hoursWarning = signal<string | null>(null);

  isEdit = false;
  private storeId = '';
  private shiftId = '';
  private assignmentId = '';
  private shiftHours = 0;
  private formSub?: Subscription;

  readonly form = this.fb.group({
    employeeId: ['', Validators.required],
    date: [null as Date | null, Validators.required],
  });

  ngOnInit(): void {
    this.storeId = this.route.snapshot.paramMap.get('storeId') ?? '';
    this.shiftId = this.route.snapshot.paramMap.get('shiftId') ?? '';
    this.assignmentId = this.route.snapshot.paramMap.get('assignmentId') ?? '';
    this.isEdit = !!this.assignmentId;

    const presetDate = this.route.snapshot.queryParamMap.get('date');
    if (presetDate && !this.isEdit) {
      this.form.patchValue({ date: this.parseIso(presetDate) });
    }

    this.employeeService.findAll({ size: 200 }).subscribe((p) => this.employees.set(p.content));

    if (this.storeId) {
      this.shiftService.findById(this.storeId, this.shiftId).subscribe((s) => {
        this.shiftName.set(s.name);
        this.shiftHours = this.calcShiftHours(s.startTime, s.endTime);
        this.setupHoursWarning();
      });
    }

    if (this.isEdit) {
      this.assignmentService.findById(this.shiftId, this.assignmentId).subscribe((a) => {
        this.form.patchValue({ employeeId: a.employeeId, date: this.parseIso(a.date) });
      });
    }
  }

  ngOnDestroy(): void {
    this.formSub?.unsubscribe();
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.errorMessage.set('');
    const v = this.form.getRawValue();
    const isoDate = this.toIso(v.date!);

    const obs = this.isEdit
      ? this.assignmentService.update(this.shiftId, this.assignmentId, { employeeId: v.employeeId!, date: isoDate })
      : this.assignmentService.create(this.shiftId, { employeeId: v.employeeId!, date: isoDate });

    obs.subscribe({
      next: (res) => {
        const message = res.warning ? `Asignación guardada · ${res.warning}` : 'Asignación guardada';
        this.snackBar.open(message, 'OK', { duration: res.warning ? 7000 : 3000 });
        this.goToCoverage(v.date!);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(
          err.status === 409
            ? 'El empleado ya tiene un turno asignado en esa fecha.'
            : err.error?.message ?? 'Error al guardar la asignación.'
        );
      },
      complete: () => this.loading.set(false),
    });
  }

  cancel(): void {
    if (this.storeId) {
      this.router.navigate(['/stores', this.storeId, 'coverage']);
    } else {
      this.router.navigate(['..'], { relativeTo: this.route });
    }
  }

  private setupHoursWarning(): void {
    this.formSub = this.form.valueChanges.pipe(
      debounceTime(400),
      distinctUntilChanged((a, b) =>
        a.employeeId === b.employeeId && a.date?.getTime() === b.date?.getTime()
      ),
      switchMap((v) => {
        if (!v.employeeId || !v.date) {
          this.hoursWarning.set(null);
          return of(null);
        }
        return this.assignmentService.findWeeklyByEmployee(v.employeeId, this.toIso(v.date));
      })
    ).subscribe((assignments) => {
      if (!assignments) return;

      const employeeId = this.form.getRawValue().employeeId;
      const employee = this.employees().find((e) => e.id === employeeId);
      const contracted = employee?.weeklyContractedHours;
      if (!contracted) { this.hoursWarning.set(null); return; }

      const existingHours = assignments
        .filter((a) => !this.isEdit || a.id !== this.assignmentId)
        .reduce((sum, a) => sum + a.totalHours, 0);

      const total = existingHours + this.shiftHours;

      if (total > contracted) {
        const totalLabel = Number.isInteger(total) ? `${total}h` : `${total.toFixed(1)}h`;
        this.hoursWarning.set(`Con este turno el empleado acumularía ${totalLabel} de ${contracted}h contratadas.`);
      } else {
        this.hoursWarning.set(null);
      }
    });
  }

  private goToCoverage(date: Date): void {
    if (this.storeId) {
      this.router.navigate(['/stores', this.storeId, 'coverage'], {
        queryParams: { week: this.mondayIso(date) },
      });
    } else {
      this.router.navigate(['..'], { relativeTo: this.route });
    }
  }

  private calcShiftHours(startTime: string, endTime: string): number {
    const [sh, sm] = startTime.split(':').map(Number);
    const [eh, em] = endTime.split(':').map(Number);
    let diff = (eh * 60 + em) - (sh * 60 + sm);
    if (diff <= 0) diff += 1440;
    return diff / 60;
  }

  private mondayIso(date: Date): string {
    const d = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    const jsDay = d.getDay();
    const isoDay = jsDay === 0 ? 7 : jsDay;
    d.setDate(d.getDate() - (isoDay - 1));
    return this.toIso(d);
  }

  private parseIso(value: string): Date {
    const [y, m, d] = value.split('-').map(Number);
    return new Date(y, m - 1, d);
  }

  private toIso(date: Date): string {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }
}
