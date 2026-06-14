import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ShiftService } from '../../../core/services/shift.service';
import { StoreService } from '../../../core/services/store.service';
import { ShiftCoverageSlot } from '../../../core/models/shift.models';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

interface DayColumn {
  iso: string;
  dayLabel: string;
  dayNumber: string;
  isToday: boolean;
  slots: ShiftCoverageSlot[];
}

const DAY_LABELS = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];

@Component({
  selector: 'app-shift-coverage',
  standalone: true,
  imports: [
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatSnackBarModule,
    PageHeaderComponent,
  ],
  template: `
    <app-page-header
      [title]="'Cobertura semanal' + (storeName() ? ' · ' + storeName() : '')"
      subtitle="Turnos de cada día y su estado de asignación"
    />

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
      <div class="legend">
        <span class="legend-item"><span class="dot assigned"></span> Asignado ({{ assignedCount() }})</span>
        <span class="legend-item"><span class="dot unassigned"></span> Sin asignar ({{ unassignedCount() }})</span>
      </div>

      @if (totalSlots() === 0) {
        <div class="empty-state">
          <mat-icon>event_busy</mat-icon>
          <p>No hay turnos activos para esta semana.</p>
        </div>
      } @else {
        <div class="calendar">
          @for (col of days(); track col.iso) {
            <div class="day-col" [class.today]="col.isToday">
              <div class="day-head">
                <span class="day-name">{{ col.dayLabel }}</span>
                <span class="day-num" [class.today-num]="col.isToday">{{ col.dayNumber }}</span>
              </div>
              <div class="day-body">
                @for (slot of col.slots; track slot.shiftId + slot.date) {
                  <div class="slot" [class.assigned]="slot.assigned" [class.unassigned]="!slot.assigned">
                    <a class="slot-name" [class]="slot.shiftType.toLowerCase()"
                       [routerLink]="['/stores', storeId, 'shifts', slot.shiftId]">
                      {{ slot.shiftName }}
                    </a>
                    <span class="slot-time">
                      <mat-icon>schedule</mat-icon>
                      {{ slot.startTime.slice(0,5) }}–{{ slot.endTime.slice(0,5) }}
                      @if (slot.crossesMidnight) { <span class="cross">+1</span> }
                    </span>

                    @if (slot.assigned) {
                      <a class="slot-emp"
                         [routerLink]="['/stores', storeId, 'shifts', slot.shiftId, 'assignments', slot.assignmentId, 'edit']"
                         matTooltip="Editar asignación">
                        <mat-icon>person</mat-icon> {{ slot.employeeName }}
                      </a>
                    } @else {
                      <button class="slot-assign" (click)="assign(slot)">
                        <mat-icon>person_add</mat-icon> Asignar
                      </button>
                    }
                  </div>
                } @empty {
                  <span class="rest-label">Sin turnos</span>
                }
              </div>
            </div>
          }
        </div>
      }
    }

    <div class="back-row">
      <a mat-button [routerLink]="['/stores', storeId]">
        <mat-icon>arrow_back</mat-icon> Volver a la tienda
      </a>
    </div>
  `,
  styles: [`
    .week-nav { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; gap: 12px; }
    .week-center { display: flex; flex-direction: column; align-items: center; gap: 2px; }
    .week-range { font-weight: 600; color: #0f172a; font-size: 1.05rem; }

    .spinner-center { display: flex; justify-content: center; padding: 48px; }

    .legend { display: flex; gap: 18px; margin-bottom: 14px; }
    .legend-item { display: flex; align-items: center; gap: 6px; font-size: 0.85rem; color: #475569; }
    .dot { width: 10px; height: 10px; border-radius: 50%; }
    .dot.assigned { background: #059669; }
    .dot.unassigned { background: #f59e0b; }

    .empty-state {
      display: flex; flex-direction: column; align-items: center; gap: 8px;
      padding: 48px; color: #94a3b8;
    }
    .empty-state mat-icon { font-size: 2.4rem; width: 2.4rem; height: 2.4rem; }
    .empty-state p { margin: 0; }

    .calendar { display: grid; grid-template-columns: repeat(7, 1fr); gap: 10px; }
    @media (max-width: 1100px) { .calendar { grid-template-columns: repeat(2, 1fr); } }

    .day-col {
      background: #fff; border: 1px solid #e2e8f0; border-radius: 12px;
      display: flex; flex-direction: column; overflow: hidden; min-height: 140px;
    }
    .day-col.today { border-color: #2563eb; box-shadow: 0 0 0 1px #2563eb; }

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

    .slot {
      border-radius: 8px; padding: 8px; display: flex; flex-direction: column; gap: 4px;
      border: 1px solid; border-left-width: 3px;
    }
    .slot.assigned   { background: #f0fdf4; border-color: #bbf7d0; border-left-color: #059669; }
    .slot.unassigned { background: #fffbeb; border-color: #fde68a; border-left-color: #f59e0b; }

    .slot-name { font-size: 0.82rem; font-weight: 600; text-decoration: none; }
    .slot-name.morning   { color: #b45309; }
    .slot-name.afternoon { color: #1d4ed8; }
    .slot-name.night     { color: #6d28d9; }
    .slot-name:hover { text-decoration: underline; }

    .slot-time { display: flex; align-items: center; gap: 3px; font-size: 0.74rem; color: #475569; }
    .slot-time mat-icon { font-size: 0.85rem; width: 0.85rem; height: 0.85rem; }
    .cross { font-size: 0.62rem; font-weight: 700; color: #7c3aed; }

    .slot-emp {
      display: flex; align-items: center; gap: 4px; font-size: 0.76rem; font-weight: 500;
      color: #047857; text-decoration: none; margin-top: 2px;
    }
    .slot-emp:hover { text-decoration: underline; }
    .slot-emp mat-icon { font-size: 0.9rem; width: 0.9rem; height: 0.9rem; }

    .slot-assign {
      display: inline-flex; align-items: center; gap: 4px; align-self: flex-start;
      border: none; background: #f59e0b; color: #fff; cursor: pointer;
      border-radius: 14px; padding: 3px 10px 3px 8px; font-size: 0.74rem; font-weight: 600;
      margin-top: 2px; transition: background 0.15s;
    }
    .slot-assign:hover { background: #d97706; }
    .slot-assign mat-icon { font-size: 0.9rem; width: 0.9rem; height: 0.9rem; }

    .rest-label { color: #cbd5e1; font-size: 0.78rem; font-style: italic; padding: 4px; }

    .back-row { margin-top: 24px; }
  `],
})
export class ShiftCoverageComponent implements OnInit {
  private readonly shiftService = inject(ShiftService);
  private readonly storeService = inject(StoreService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(true);
  readonly storeName = signal('');
  readonly weekRangeLabel = signal('');
  private readonly weekMonday = signal<Date>(this.mondayOf(new Date()));
  private readonly slots = signal<ShiftCoverageSlot[]>([]);

  storeId = '';

  readonly days = computed<DayColumn[]>(() => {
    const monday = this.weekMonday();
    const todayIso = this.toIso(new Date());
    const byDate = new Map<string, ShiftCoverageSlot[]>();
    for (const s of this.slots()) {
      const list = byDate.get(s.date) ?? [];
      list.push(s);
      byDate.set(s.date, list);
    }
    const cols: DayColumn[] = [];
    for (let i = 0; i < 7; i++) {
      const d = new Date(monday);
      d.setDate(monday.getDate() + i);
      const iso = this.toIso(d);
      cols.push({
        iso,
        dayLabel: DAY_LABELS[i],
        dayNumber: String(d.getDate()).padStart(2, '0'),
        isToday: iso === todayIso,
        slots: (byDate.get(iso) ?? []).sort((a, b) => a.startTime.localeCompare(b.startTime)),
      });
    }
    return cols;
  });

  readonly totalSlots = computed(() => this.slots().length);
  readonly assignedCount = computed(() => this.slots().filter((s) => s.assigned).length);
  readonly unassignedCount = computed(() => this.slots().filter((s) => !s.assigned).length);
  readonly isCurrentWeek = computed(
    () => this.toIso(this.weekMonday()) === this.toIso(this.mondayOf(new Date()))
  );

  ngOnInit(): void {
    this.storeId = this.route.snapshot.paramMap.get('storeId') ?? '';
    const weekParam = this.route.snapshot.queryParamMap.get('week');
    if (weekParam) {
      this.weekMonday.set(this.parseIso(weekParam));
    }
    this.storeService.findById(this.storeId).subscribe((s) => this.storeName.set(s.name));
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

  assign(slot: ShiftCoverageSlot): void {
    this.router.navigate(['/stores', this.storeId, 'shifts', slot.shiftId, 'assignments', 'new'], {
      queryParams: { date: slot.date },
    });
  }

  private load(): void {
    this.loading.set(true);
    const ref = this.toIso(this.weekMonday());
    this.shiftService.findWeeklyCoverage(this.storeId, ref).subscribe({
      next: (list) => { this.slots.set(list); this.loading.set(false); },
      error: () => {
        this.loading.set(false);
        this.snackBar.open('Error al cargar la cobertura', 'OK', { duration: 3000 });
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

  private parseIso(value: string): Date {
    const [y, m, d] = value.split('-').map(Number);
    return new Date(y, m - 1, d);
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
