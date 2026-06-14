import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TokenService } from '../../core/auth/token.service';
import { ShiftAssignmentService } from '../../core/services/shift-assignment.service';
import { WeeklyAssignmentResponse } from '../../core/models/shift-assignment.models';
import { ShiftType } from '../../core/models/shift.models';
import { ShiftTypeLabelPipe } from '../../shared/pipes/shift-type-label.pipe';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';

interface DayCell {
  iso: string;
  dayLabel: string;   // Lun, Mar...
  dayNumber: string;  // 09
  isToday: boolean;
  assignments: WeeklyAssignmentResponse[];
}

const DAY_LABELS = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];

@Component({
  selector: 'app-my-schedule',
  standalone: true,
  imports: [
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    ShiftTypeLabelPipe,
    PageHeaderComponent,
  ],
  template: `
    <app-page-header title="Mi horario" subtitle="Consulta tus turnos semana a semana" />

    <div class="week-nav">
      <button mat-stroked-button (click)="changeWeek(-1)">
        <mat-icon>chevron_left</mat-icon> Semana anterior
      </button>
      <div class="week-center">
        <span class="week-range">{{ weekRangeLabel() }}</span>
        @if (!isCurrentWeek()) {
          <button mat-button color="primary" (click)="goToCurrentWeek()">Semana actual</button>
        }
      </div>
      <button mat-stroked-button (click)="changeWeek(1)">
        Semana siguiente <mat-icon>chevron_right</mat-icon>
      </button>
    </div>

    @if (loading()) {
      <div class="spinner-center"><mat-spinner /></div>
    } @else {
      <div class="summary-bar">
        <span><strong>{{ totalShifts() }}</strong> turno(s)</span>
        <span class="dot">·</span>
        <span><strong>{{ totalHours() }}</strong> h en total</span>
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
  `,
  styles: [`
    .week-nav { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; gap: 12px; }
    .week-center { display: flex; flex-direction: column; align-items: center; gap: 2px; }
    .week-range { font-weight: 600; color: #0f172a; font-size: 1.05rem; }

    .spinner-center { display: flex; justify-content: center; padding: 48px; }

    .summary-bar { display: flex; align-items: center; gap: 10px; color: #475569; margin-bottom: 16px; font-size: 0.9rem; }
    .summary-bar strong { color: #0f172a; }
    .dot { color: #cbd5e1; }

    .calendar {
      display: grid;
      grid-template-columns: repeat(7, 1fr);
      gap: 10px;
    }
    @media (max-width: 900px) {
      .calendar { grid-template-columns: repeat(2, 1fr); }
    }
    @media (max-width: 480px) {
      .calendar { grid-template-columns: 1fr; }
      .week-nav { flex-wrap: wrap; gap: 8px; }
      .week-center { width: 100%; order: -1; }
    }

    .day-cell {
      background: #fff; border: 1px solid #e2e8f0; border-radius: 12px;
      min-height: 150px; display: flex; flex-direction: column; overflow: hidden;
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
    .shift-time, .shift-store {
      display: flex; align-items: center; gap: 3px; font-size: 0.74rem; color: #475569;
    }
    .shift-time mat-icon, .shift-store mat-icon { font-size: 0.85rem; width: 0.85rem; height: 0.85rem; }
    .cross { font-size: 0.62rem; font-weight: 700; color: #7c3aed; }

    .rest-label {
      color: #cbd5e1; font-size: 0.8rem; font-style: italic;
      display: flex; align-items: center; justify-content: center; flex: 1;
    }
  `],
})
export class MyScheduleComponent implements OnInit {
  private readonly tokenService = inject(TokenService);
  private readonly assignmentService = inject(ShiftAssignmentService);
  private readonly snackBar = inject(MatSnackBar);

  readonly loading = signal(true);
  readonly weekRangeLabel = signal('');
  private readonly weekMonday = signal<Date>(this.mondayOf(new Date()));
  private readonly assignments = signal<WeeklyAssignmentResponse[]>([]);

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
    this.assignments().reduce((sum, a) => sum + a.totalHours, 0).toFixed(1).replace(/\.0$/, '')
  );
  readonly isCurrentWeek = computed(
    () => this.toIso(this.weekMonday()) === this.toIso(this.mondayOf(new Date()))
  );

  ngOnInit(): void {
    this.updateRangeLabel();
    this.load();
  }

  changeWeek(delta: number): void {
    const m = new Date(this.weekMonday());
    m.setDate(m.getDate() + delta * 7);
    this.weekMonday.set(m);
    this.updateRangeLabel();
    this.load();
  }

  goToCurrentWeek(): void {
    this.weekMonday.set(this.mondayOf(new Date()));
    this.updateRangeLabel();
    this.load();
  }

  private load(): void {
    const userId = this.tokenService.currentUser()?.id;
    if (!userId) return;
    this.loading.set(true);
    const refDate = this.toIso(this.weekMonday());
    this.assignmentService.findWeeklyByEmployee(userId, refDate).subscribe({
      next: (list) => { this.assignments.set(list); this.loading.set(false); },
      error: () => {
        this.loading.set(false);
        this.snackBar.open('Error al cargar tu horario', 'OK', { duration: 3000 });
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
