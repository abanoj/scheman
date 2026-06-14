import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { EmployeeService } from '../../../core/services/employee.service';
import { ShiftAssignmentService } from '../../../core/services/shift-assignment.service';
import { EmployeeResponse } from '../../../core/models/employee.models';
import { WeeklyAssignmentResponse } from '../../../core/models/shift-assignment.models';
import { ShiftTypeLabelPipe } from '../../../shared/pipes/shift-type-label.pipe';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

interface DayCell {
  iso: string;
  dayLabel: string;
  dayNumber: string;
  isToday: boolean;
  assignments: WeeklyAssignmentResponse[];
}

const DAY_LABELS = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];

@Component({
  selector: 'app-employee-detail',
  standalone: true,
  imports: [
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    MatSnackBarModule,
    MatTooltipModule,
    ShiftTypeLabelPipe,
    PageHeaderComponent,
  ],
  template: `
    @if (loading()) {
      <div class="spinner-center"><mat-spinner /></div>
    } @else if (employee()) {
      <app-page-header
        [title]="employee()!.firstName + ' ' + employee()!.lastName"
        subtitle="Detalle de empleado"
      />

      <div class="detail-layout">
        <!-- Información principal -->
        <mat-card>
          <mat-card-content>
            <div class="profile-header">
              <div class="avatar-lg">{{ initials() }}</div>
              <div class="profile-meta">
                <h2 class="profile-name">{{ employee()!.firstName }} {{ employee()!.lastName }}</h2>
                <span class="profile-email">{{ employee()!.email }}</span>
              </div>
              <div class="profile-actions">
                <a mat-stroked-button [routerLink]="['..', employee()!.id, 'edit']">
                  <mat-icon>edit</mat-icon> Editar
                </a>
                <button mat-stroked-button color="warn" (click)="confirmDisable()">
                  <mat-icon>person_off</mat-icon> Deshabilitar
                </button>
              </div>
            </div>

            <div class="divider"></div>

            <dl class="info-grid">
              <dt>DNI</dt>
              <dd>{{ employee()!.dni }}</dd>

              <dt>Email</dt>
              <dd>{{ employee()!.email }}</dd>

              <dt>Turno preferente</dt>
              <dd>{{ employee()!.preferredShift | shiftTypeLabel }}</dd>

              <dt>Horas / semana</dt>
              <dd>{{ employee()!.weeklyContractedHours }}h</dd>
            </dl>
          </mat-card-content>
        </mat-card>

        <!-- Tiendas -->
        <mat-card>
          <mat-card-content>
            <h3 class="section-title">Tiendas preferentes</h3>
            @if (employee()!.preferredStores.length) {
              <div class="chip-list">
                @for (store of employee()!.preferredStores; track store.id) {
                  <span class="store-chip">
                    <mat-icon class="chip-icon">store</mat-icon>
                    {{ store.name }}
                  </span>
                }
              </div>
            } @else {
              <p class="empty-msg">Sin tiendas asignadas</p>
            }
          </mat-card-content>
        </mat-card>
      </div>

      <!-- ── Horario semanal ─────────────────────────────────────────────────── -->
      <div class="schedule-section">
        <div class="schedule-header">
          <h3 class="section-title">Horario semanal</h3>

          <div class="week-nav">
            <button mat-stroked-button (click)="changeWeek(-1)">
              <mat-icon>chevron_left</mat-icon>
            </button>
            <div class="week-center">
              <span class="week-range">{{ weekRangeLabel() }}</span>
              @if (!isCurrentWeek()) {
                <button mat-button color="primary" (click)="goToCurrentWeek()">Hoy</button>
              }
            </div>
            <button mat-stroked-button (click)="changeWeek(1)">
              <mat-icon>chevron_right</mat-icon>
            </button>
          </div>
        </div>

        @if (scheduleLoading()) {
          <div class="spinner-center"><mat-spinner diameter="32" /></div>
        } @else {
          <!-- Barra resumen horas -->
          <div class="hours-bar">
            <mat-icon class="hours-icon">schedule</mat-icon>
            <span>
              <strong>{{ totalShifts() }}</strong> turno(s) esta semana
            </span>
            <span class="dot">·</span>
            <span [class.hours-ok]="!hoursExceeded()" [class.hours-warn]="hoursExceeded()">
              <strong>{{ totalHoursLabel() }}</strong>
              @if (employee()!.weeklyContractedHours) {
                / {{ employee()!.weeklyContractedHours }}h contratadas
                @if (hoursExceeded()) {
                  <mat-icon class="warn-icon" matTooltip="Horas contratadas superadas">warning</mat-icon>
                }
              }
            </span>
          </div>

          <div class="calendar">
            @for (cell of days(); track cell.iso) {
              <div class="day-cell" [class.today]="cell.isToday" [class.empty]="!cell.assignments.length">
                <div class="day-head">
                  <span class="day-name">{{ cell.dayLabel }}</span>
                  <span class="day-num" [class.today-num]="cell.isToday">{{ cell.dayNumber }}</span>
                </div>
                <div class="day-body">
                  @for (a of cell.assignments; track a.id) {
                    <div class="shift-pill" [class]="a.shiftType.toLowerCase()">
                      <span class="shift-name">{{ a.shiftName }}</span>
                      <span class="shift-time">
                        <mat-icon>schedule</mat-icon>
                        {{ a.startTime.slice(0,5) }}–{{ a.endTime.slice(0,5) }}
                        @if (a.crossesMidnight) { <span class="cross">+1</span> }
                      </span>
                      <span class="shift-store">
                        <mat-icon>store</mat-icon> {{ a.storeName }}
                      </span>
                    </div>
                  } @empty {
                    <span class="rest-label">Libre</span>
                  }
                </div>
              </div>
            }
          </div>
        }
      </div>

      <div class="back-row">
        <a mat-button routerLink="/employees">
          <mat-icon>arrow_back</mat-icon> Volver a empleados
        </a>
      </div>
    } @else {
      <p class="not-found">Empleado no encontrado.</p>
    }
  `,
  styles: [`
    .spinner-center { display: flex; justify-content: center; padding: 48px; }

    .detail-layout {
      display: grid;
      grid-template-columns: 1fr 300px;
      gap: 20px;
      max-width: 960px;
    }

    .profile-header { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; }

    .avatar-lg {
      width: 56px; height: 56px; border-radius: 50%;
      background: #dbeafe; color: #1d4ed8;
      display: flex; align-items: center; justify-content: center;
      font-size: 1.4rem; font-weight: 700; flex-shrink: 0;
    }

    .profile-meta { flex: 1; }
    .profile-name { margin: 0 0 2px; font-size: 1.2rem; font-weight: 600; color: #0f172a; }
    .profile-email { font-size: 0.875rem; color: #64748b; }
    .profile-actions { display: flex; gap: 8px; margin-left: auto; }

    .divider { height: 1px; background: #e2e8f0; margin: 20px 0; }

    .info-grid { display: grid; grid-template-columns: 160px 1fr; gap: 12px 16px; }
    dt { font-size: 0.78rem; color: #64748b; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; align-self: center; }
    dd { margin: 0; color: #1e293b; font-weight: 500; }

    .section-title { margin: 0; font-size: 0.95rem; font-weight: 600; color: #0f172a; }

    .chip-list { display: flex; flex-direction: column; gap: 8px; }
    .store-chip {
      display: flex; align-items: center; gap: 8px;
      padding: 8px 12px; background: #f1f5f9;
      border-radius: 8px; font-size: 0.875rem; color: #1e293b;
    }
    .chip-icon { font-size: 1rem; width: 1rem; height: 1rem; color: #64748b; }
    .empty-msg { color: #94a3b8; font-size: 0.875rem; }

    /* ── Schedule section ── */
    .schedule-section { margin-top: 24px; max-width: 960px; }

    .schedule-header {
      display: flex; align-items: center; justify-content: space-between;
      margin-bottom: 16px; flex-wrap: wrap; gap: 12px;
    }

    .week-nav { display: flex; align-items: center; gap: 8px; }
    .week-center { display: flex; flex-direction: column; align-items: center; gap: 2px; min-width: 220px; }
    .week-range { font-size: 0.9rem; font-weight: 600; color: #334155; }

    .hours-bar {
      display: flex; align-items: center; gap: 10px;
      color: #475569; margin-bottom: 16px; font-size: 0.9rem;
      background: #f8fafc; border: 1px solid #e2e8f0;
      border-radius: 10px; padding: 10px 16px;
    }
    .hours-bar strong { color: #0f172a; }
    .hours-icon { font-size: 1.1rem; width: 1.1rem; height: 1.1rem; color: #64748b; }
    .dot { color: #cbd5e1; }

    .hours-ok strong { color: #059669; }
    .hours-warn { display: flex; align-items: center; gap: 4px; }
    .hours-warn strong { color: #d97706; }
    .warn-icon { font-size: 1rem; width: 1rem; height: 1rem; color: #d97706; }

    .calendar { display: grid; grid-template-columns: repeat(7, 1fr); gap: 10px; }
    @media (max-width: 900px) { .calendar { grid-template-columns: repeat(2, 1fr); } }

    .day-cell {
      background: #fff; border: 1px solid #e2e8f0; border-radius: 12px;
      min-height: 120px; display: flex; flex-direction: column; overflow: hidden;
    }
    .day-cell.today { border-color: #2563eb; box-shadow: 0 0 0 1px #2563eb; }
    .day-cell.empty { background: #f8fafc; }

    .day-head {
      display: flex; align-items: center; justify-content: space-between;
      padding: 8px 10px; border-bottom: 1px solid #f1f5f9;
    }
    .day-name { font-size: 0.72rem; font-weight: 600; color: #64748b; text-transform: uppercase; letter-spacing: 0.05em; }
    .day-num { font-size: 0.95rem; font-weight: 700; color: #334155; }
    .today-num {
      background: #2563eb; color: #fff; width: 24px; height: 24px; border-radius: 50%;
      display: inline-flex; align-items: center; justify-content: center; font-size: 0.8rem;
    }

    .day-body { padding: 8px; display: flex; flex-direction: column; gap: 8px; flex: 1; }

    .shift-pill {
      border-radius: 8px; padding: 8px; display: flex; flex-direction: column; gap: 3px;
      border-left: 3px solid;
    }
    .shift-pill.morning   { background: #fffbeb; border-color: #f59e0b; }
    .shift-pill.afternoon { background: #eff6ff; border-color: #3b82f6; }
    .shift-pill.night     { background: #f5f3ff; border-color: #8b5cf6; }

    .shift-name { font-size: 0.82rem; font-weight: 600; color: #0f172a; }
    .shift-time, .shift-store { display: flex; align-items: center; gap: 3px; font-size: 0.74rem; color: #475569; }
    .shift-time mat-icon, .shift-store mat-icon { font-size: 0.85rem; width: 0.85rem; height: 0.85rem; }
    .cross { font-size: 0.62rem; font-weight: 700; color: #7c3aed; }

    .rest-label {
      color: #cbd5e1; font-size: 0.8rem; font-style: italic;
      display: flex; align-items: center; justify-content: center; flex: 1;
    }

    .back-row { margin-top: 16px; }
    .not-found { color: #64748b; }
  `],
})
export class EmployeeDetailComponent implements OnInit {
  private readonly employeeService = inject(EmployeeService);
  private readonly assignmentService = inject(ShiftAssignmentService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(true);
  readonly scheduleLoading = signal(false);
  readonly employee = signal<EmployeeResponse | null>(null);

  private readonly weekMonday = signal<Date>(this.mondayOf(new Date()));
  private readonly assignments = signal<WeeklyAssignmentResponse[]>([]);
  readonly weekRangeLabel = signal('');

  readonly days = computed<DayCell[]>(() => {
    const monday = this.weekMonday();
    const todayIso = this.toIso(new Date());
    const byDate = new Map<string, WeeklyAssignmentResponse[]>();
    for (const a of this.assignments()) {
      const list = byDate.get(a.date) ?? [];
      list.push(a);
      byDate.set(a.date, list);
    }
    const cells: DayCell[] = [];
    for (let i = 0; i < 7; i++) {
      const d = new Date(monday);
      d.setDate(monday.getDate() + i);
      const iso = this.toIso(d);
      cells.push({
        iso,
        dayLabel: DAY_LABELS[i],
        dayNumber: String(d.getDate()).padStart(2, '0'),
        isToday: iso === todayIso,
        assignments: (byDate.get(iso) ?? []).sort((a, b) => a.startTime.localeCompare(b.startTime)),
      });
    }
    return cells;
  });

  readonly totalShifts = computed(() => this.assignments().length);
  readonly totalHours = computed(() =>
    this.assignments().reduce((sum, a) => sum + a.totalHours, 0)
  );
  readonly totalHoursLabel = computed(() => {
    const h = this.totalHours();
    return Number.isInteger(h) ? `${h}h` : `${h.toFixed(1)}h`;
  });
  readonly hoursExceeded = computed(() => {
    const contracted = this.employee()?.weeklyContractedHours;
    return contracted != null && this.totalHours() > contracted;
  });
  readonly isCurrentWeek = computed(
    () => this.toIso(this.weekMonday()) === this.toIso(this.mondayOf(new Date()))
  );

  initials(): string {
    const emp = this.employee();
    if (!emp) return '';
    return ((emp.firstName?.[0] ?? '') + (emp.lastName?.[0] ?? '')).toUpperCase();
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.updateRangeLabel();
    this.employeeService.findById(id).subscribe({
      next: (e) => {
        this.employee.set(e);
        this.loading.set(false);
        this.loadSchedule(e.id);
      },
      error: () => this.loading.set(false),
    });
  }

  changeWeek(delta: number): void {
    const m = new Date(this.weekMonday());
    m.setDate(m.getDate() + delta * 7);
    this.weekMonday.set(m);
    this.updateRangeLabel();
    this.loadSchedule(this.employee()!.id);
  }

  goToCurrentWeek(): void {
    this.weekMonday.set(this.mondayOf(new Date()));
    this.updateRangeLabel();
    this.loadSchedule(this.employee()!.id);
  }

  confirmDisable(): void {
    const emp = this.employee()!;
    this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Deshabilitar empleado',
        message: `¿Deshabilitar la cuenta de ${emp.firstName} ${emp.lastName}?`,
        confirmLabel: 'Deshabilitar',
      },
    }).afterClosed().subscribe((ok) => {
      if (!ok) return;
      this.employeeService.disable(emp.id).subscribe({
        next: () => {
          this.snackBar.open('Empleado deshabilitado', 'OK', { duration: 3000 });
          this.router.navigate(['/employees']);
        },
        error: () => this.snackBar.open('Error al deshabilitar', 'OK', { duration: 3000 }),
      });
    });
  }

  private loadSchedule(employeeId: string): void {
    this.scheduleLoading.set(true);
    this.assignmentService.findWeeklyByEmployee(employeeId, this.toIso(this.weekMonday())).subscribe({
      next: (list) => { this.assignments.set(list); this.scheduleLoading.set(false); },
      error: () => {
        this.scheduleLoading.set(false);
        this.snackBar.open('Error al cargar el horario', 'OK', { duration: 3000 });
      },
    });
  }

  private updateRangeLabel(): void {
    const monday = this.weekMonday();
    const sunday = new Date(monday);
    sunday.setDate(monday.getDate() + 6);
    const fmt = (d: Date) => d.toLocaleDateString('es-ES', { day: '2-digit', month: 'long' });
    this.weekRangeLabel.set(`${fmt(monday)} – ${fmt(sunday)} ${sunday.getFullYear()}`);
  }

  private mondayOf(ref: Date): Date {
    const d = new Date(ref.getFullYear(), ref.getMonth(), ref.getDate());
    const jsDay = d.getDay();
    const isoDay = jsDay === 0 ? 7 : jsDay;
    d.setDate(d.getDate() - (isoDay - 1));
    return d;
  }

  private toIso(d: Date): string {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }
}
