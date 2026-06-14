import { Component, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { StoreService } from '../../../core/services/store.service';
import { ShiftService } from '../../../core/services/shift.service';
import { StoreResponse } from '../../../core/models/store.models';
import { ShiftResponse } from '../../../core/models/shift.models';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ShiftTypeLabelPipe } from '../../../shared/pipes/shift-type-label.pipe';
import { DaysOfWeekLabelPipe } from '../../../shared/pipes/day-of-week-label.pipe';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-store-detail',
  standalone: true,
  imports: [
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    MatSnackBarModule,
    MatTooltipModule,
    ShiftTypeLabelPipe,
    DaysOfWeekLabelPipe,
    PageHeaderComponent,
  ],
  template: `
    @if (loading()) {
      <div class="spinner-center"><mat-spinner /></div>
    } @else if (store()) {
      <app-page-header [title]="store()!.name" subtitle="Detalle de tienda" />

      <div class="detail-grid">
        <mat-card>
          <mat-card-header>
            <mat-card-title>Información general</mat-card-title>
            <div class="header-actions">
              <a mat-stroked-button [routerLink]="['../', store()!.id, 'edit']">
                <mat-icon>edit</mat-icon> Editar
              </a>
            </div>
          </mat-card-header>
          <mat-card-content>
            <dl class="info-list">
              <dt>Dirección</dt><dd>{{ store()!.address || '-' }}</dd>
              <dt>Teléfono</dt><dd>{{ store()!.phone || '-' }}</dd>
              <dt>24 horas</dt><dd>{{ store()!.is24h ? 'Sí' : 'No' }}</dd>
            </dl>
          </mat-card-content>
        </mat-card>

        <mat-card>
          <mat-card-header>
            <mat-card-title>Empleados preferentes</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <mat-chip-set>
              @for (emp of store()!.preferredEmployees; track emp.id) {
                <mat-chip>{{ emp.name }}</mat-chip>
              } @empty {
                <span class="empty">Sin empleados asignados</span>
              }
            </mat-chip-set>
          </mat-card-content>
        </mat-card>
      </div>

      <div class="shifts-section">
        <div class="section-header">
          <h2>Turnos</h2>
          <div class="section-actions">
            <a mat-stroked-button [routerLink]="['/', 'stores', store()!.id, 'coverage']">
              <mat-icon>event_available</mat-icon> Cobertura semanal
            </a>
            <a mat-flat-button color="primary" [routerLink]="['/', 'stores', store()!.id, 'shifts', 'new']">
              <mat-icon>add</mat-icon> Nuevo turno
            </a>
          </div>
        </div>

        <div class="table-wrapper mat-elevation-z1">
          <table mat-table [dataSource]="store()!.shifts">
            <ng-container matColumnDef="name">
              <th mat-header-cell *matHeaderCellDef>Nombre</th>
              <td mat-cell *matCellDef="let s">
                <a class="shift-link" [routerLink]="['/', 'stores', store()!.id, 'shifts', s.id]">{{ s.name }}</a>
              </td>
            </ng-container>

            <ng-container matColumnDef="type">
              <th mat-header-cell *matHeaderCellDef>Tipo</th>
              <td mat-cell *matCellDef="let s">{{ s.shiftType | shiftTypeLabel }}</td>
            </ng-container>

            <ng-container matColumnDef="time">
              <th mat-header-cell *matHeaderCellDef>Horario</th>
              <td mat-cell *matCellDef="let s">{{ s.startTime }} - {{ s.endTime }}</td>
            </ng-container>

            <ng-container matColumnDef="days">
              <th mat-header-cell *matHeaderCellDef>Días</th>
              <td mat-cell *matCellDef="let s">{{ s.availableDays | daysOfWeekLabel }}</td>
            </ng-container>

            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef></th>
              <td mat-cell *matCellDef="let s">
                <a mat-icon-button [routerLink]="['/', 'stores', store()!.id, 'shifts', s.id]" matTooltip="Ver asignaciones">
                  <mat-icon>group</mat-icon>
                </a>
                <a mat-icon-button [routerLink]="['/', 'stores', store()!.id, 'shifts', s.id, 'edit']" matTooltip="Editar">
                  <mat-icon>edit</mat-icon>
                </a>
                <button mat-icon-button color="warn" (click)="confirmDeleteShift(s)" matTooltip="Eliminar">
                  <mat-icon>delete</mat-icon>
                </button>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="shiftColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: shiftColumns;"></tr>

            @if (!store()!.shifts.length) {
              <tr class="mat-mdc-no-data-row">
                <td [attr.colspan]="shiftColumns.length" class="no-data">
                  Esta tienda no tiene turnos. Crea el primero con "Nuevo turno".
                </td>
              </tr>
            }
          </table>
        </div>
      </div>
    }
  `,
  styles: [`
    .spinner-center { display: flex; justify-content: center; padding: 48px; }
    .detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 24px; }
    .header-actions { margin-left: auto; }
    .info-list { display: grid; grid-template-columns: auto 1fr; gap: 4px 16px; }
    dt { color: #64748b; font-size: 0.875rem; }
    dd { margin: 0; font-weight: 500; }
    .shifts-section { margin-top: 8px; }
    .section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
    .section-header h2 { margin: 0; }
    .section-actions { display: flex; gap: 8px; }
    .table-wrapper { border-radius: 8px; overflow: hidden; background: #fff; }
    table { width: 100%; }
    .empty { color: #94a3b8; font-size: 0.875rem; }
    .shift-link { color: #2563eb; text-decoration: none; font-weight: 500; }
    .shift-link:hover { text-decoration: underline; }
    .no-data { padding: 28px; text-align: center; color: #94a3b8; }
  `],
})
export class StoreDetailComponent implements OnInit {
  private readonly storeService = inject(StoreService);
  private readonly shiftService = inject(ShiftService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(true);
  readonly store = signal<StoreResponse | null>(null);
  readonly shiftColumns = ['name', 'type', 'time', 'days', 'actions'];

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.storeService.findById(id).subscribe({
      next: (s) => { this.store.set(s); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  confirmDeleteShift(shift: ShiftResponse): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Eliminar turno', message: `¿Eliminar el turno "${shift.name}"?`, confirmLabel: 'Eliminar' },
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.shiftService.delete(this.store()!.id, shift.id).subscribe({
          next: () => {
            this.snackBar.open('Turno eliminado', 'OK', { duration: 3000 });
            const storeId = this.store()!.id;
            this.storeService.findById(storeId).subscribe((s) => this.store.set(s));
          },
          error: (err) => this.snackBar.open(err.error?.message ?? 'Error al eliminar', 'OK', { duration: 3000 }),
        });
      }
    });
  }
}
