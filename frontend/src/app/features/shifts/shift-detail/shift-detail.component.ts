import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ShiftService } from '../../../core/services/shift.service';
import { ShiftAssignmentService } from '../../../core/services/shift-assignment.service';
import { EmployeeService } from '../../../core/services/employee.service';
import { ShiftResponse } from '../../../core/models/shift.models';
import { ShiftAssignmentResponse } from '../../../core/models/shift-assignment.models';
import { Page } from '../../../core/models/page.models';
import { ShiftTypeLabelPipe } from '../../../shared/pipes/shift-type-label.pipe';
import { DaysOfWeekLabelPipe } from '../../../shared/pipes/day-of-week-label.pipe';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-shift-detail',
  standalone: true,
  imports: [
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatTableModule,
    MatPaginatorModule,
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
    } @else if (shift()) {
      <app-page-header [title]="shift()!.name" subtitle="Detalle de turno" />

      <mat-card class="info-card">
        <mat-card-content>
          <div class="info-top">
            <div class="shift-type-badge" [class]="shift()!.shiftType.toLowerCase()">
              {{ shift()!.shiftType | shiftTypeLabel }}
            </div>
            <div class="info-actions">
              <a mat-stroked-button [routerLink]="['edit']">
                <mat-icon>edit</mat-icon> Editar turno
              </a>
              <button mat-stroked-button color="warn" (click)="confirmDeleteShift()">
                <mat-icon>delete</mat-icon> Eliminar
              </button>
            </div>
          </div>

          <div class="divider"></div>

          <dl class="info-grid">
            <dt>Horario</dt>
            <dd>
              {{ shift()!.startTime }} – {{ shift()!.endTime }}
              @if (shift()!.crossesMidnight) {
                <span class="midnight-tag">cruza medianoche</span>
              }
            </dd>

            <dt>Días disponibles</dt>
            <dd>{{ shift()!.availableDays | daysOfWeekLabel }}</dd>
          </dl>
        </mat-card-content>
      </mat-card>

      <div class="assignments-section">
        <div class="section-header">
          <h2>Asignaciones de empleados</h2>
          <a mat-flat-button color="primary" [routerLink]="['assignments', 'new']">
            <mat-icon>person_add</mat-icon> Asignar empleado
          </a>
        </div>

        @if (assignmentsLoading()) {
          <div class="spinner-center"><mat-spinner diameter="32" /></div>
        } @else {
          <div class="table-wrapper mat-elevation-z1">
            <table mat-table [dataSource]="assignments()?.content ?? []">
              <ng-container matColumnDef="date">
                <th mat-header-cell *matHeaderCellDef>Fecha</th>
                <td mat-cell *matCellDef="let a">{{ a.date }}</td>
              </ng-container>

              <ng-container matColumnDef="employee">
                <th mat-header-cell *matHeaderCellDef>Empleado</th>
                <td mat-cell *matCellDef="let a">{{ employeeName(a.employeeId) }}</td>
              </ng-container>

              <ng-container matColumnDef="actions">
                <th mat-header-cell *matHeaderCellDef></th>
                <td mat-cell *matCellDef="let a">
                  <a mat-icon-button [routerLink]="['assignments', a.id, 'edit']" matTooltip="Editar">
                    <mat-icon>edit</mat-icon>
                  </a>
                  <button mat-icon-button color="warn" (click)="confirmDeleteAssignment(a)" matTooltip="Eliminar">
                    <mat-icon>delete</mat-icon>
                  </button>
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="columns"></tr>
              <tr mat-row *matRowDef="let row; columns: columns;"></tr>

              @if (!assignments()?.content?.length) {
                <tr class="mat-mdc-no-data-row">
                  <td [attr.colspan]="columns.length" class="no-data">
                    Este turno no tiene empleados asignados todavía
                  </td>
                </tr>
              }
            </table>

            <mat-paginator
              [length]="assignments()?.totalElements ?? 0"
              [pageSize]="pageSize"
              [pageSizeOptions]="[10, 25, 50]"
              (page)="onPageChange($event)"
              showFirstLastButtons
            />
          </div>
        }
      </div>

      <div class="back-row">
        <a mat-button [routerLink]="['/stores', storeId]">
          <mat-icon>arrow_back</mat-icon> Volver a la tienda
        </a>
      </div>
    } @else {
      <p class="not-found">Turno no encontrado.</p>
    }
  `,
  styles: [`
    .spinner-center { display: flex; justify-content: center; padding: 40px; }

    .info-card { max-width: 640px; margin-bottom: 28px; }
    .info-top { display: flex; align-items: center; justify-content: space-between; gap: 12px; flex-wrap: wrap; }
    .info-actions { display: flex; gap: 8px; }

    .shift-type-badge {
      padding: 6px 14px; border-radius: 20px; font-size: 0.8rem; font-weight: 600;
    }
    .shift-type-badge.morning   { background: #fef3c7; color: #b45309; }
    .shift-type-badge.afternoon { background: #dbeafe; color: #1d4ed8; }
    .shift-type-badge.night     { background: #ede9fe; color: #6d28d9; }

    .divider { height: 1px; background: #e2e8f0; margin: 18px 0; }

    .info-grid { display: grid; grid-template-columns: 160px 1fr; gap: 12px 16px; margin: 0; }
    dt { font-size: 0.78rem; color: #64748b; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; align-self: center; }
    dd { margin: 0; color: #1e293b; font-weight: 500; }
    .midnight-tag {
      margin-left: 8px; font-size: 0.72rem; font-weight: 600;
      background: #f1f5f9; color: #475569; padding: 2px 8px; border-radius: 12px;
    }

    .assignments-section { max-width: 760px; }
    .section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
    .section-header h2 { margin: 0; font-size: 1.1rem; font-weight: 600; color: #0f172a; }

    .table-wrapper { border-radius: 10px; overflow: hidden; background: #fff; }
    table { width: 100%; }
    .no-data { padding: 28px; text-align: center; color: #94a3b8; }

    .back-row { margin-top: 24px; }
    .not-found { color: #64748b; }
  `],
})
export class ShiftDetailComponent implements OnInit {
  private readonly shiftService = inject(ShiftService);
  private readonly assignmentService = inject(ShiftAssignmentService);
  private readonly employeeService = inject(EmployeeService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(true);
  readonly assignmentsLoading = signal(true);
  readonly shift = signal<ShiftResponse | null>(null);
  readonly assignments = signal<Page<ShiftAssignmentResponse> | null>(null);
  private readonly employeeMap = signal<Map<string, string>>(new Map());

  readonly columns = ['date', 'employee', 'actions'];
  storeId = '';
  shiftId = '';
  pageSize = 10;
  currentPage = 0;

  ngOnInit(): void {
    this.storeId = this.route.snapshot.paramMap.get('storeId') ?? '';
    this.shiftId = this.route.snapshot.paramMap.get('shiftId') ?? '';

    forkJoin({
      shift: this.shiftService.findById(this.storeId, this.shiftId),
      employees: this.employeeService.findAll({ size: 200 }),
    }).subscribe({
      next: ({ shift, employees }) => {
        this.shift.set(shift);
        const map = new Map<string, string>();
        for (const e of employees.content) {
          map.set(e.id, `${e.firstName} ${e.lastName}`);
        }
        this.employeeMap.set(map);
        this.loading.set(false);
        this.loadAssignments();
      },
      error: () => this.loading.set(false),
    });
  }

  employeeName(id: string): string {
    return this.employeeMap().get(id) ?? id;
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadAssignments();
  }

  confirmDeleteShift(): void {
    this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Eliminar turno',
        message: `¿Eliminar el turno "${this.shift()!.name}"? Las asignaciones asociadas dejarán de estar activas.`,
        confirmLabel: 'Eliminar',
      },
    }).afterClosed().subscribe((ok) => {
      if (!ok) return;
      this.shiftService.delete(this.storeId, this.shiftId).subscribe({
        next: () => {
          this.snackBar.open('Turno eliminado', 'OK', { duration: 3000 });
          this.router.navigate(['/stores', this.storeId]);
        },
        error: (err) => this.snackBar.open(err.error?.message ?? 'Error al eliminar', 'OK', { duration: 3000 }),
      });
    });
  }

  confirmDeleteAssignment(a: ShiftAssignmentResponse): void {
    this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Eliminar asignación',
        message: `¿Eliminar la asignación de ${this.employeeName(a.employeeId)} del ${a.date}?`,
        confirmLabel: 'Eliminar',
      },
    }).afterClosed().subscribe((ok) => {
      if (!ok) return;
      this.assignmentService.delete(this.shiftId, a.id).subscribe({
        next: () => { this.snackBar.open('Asignación eliminada', 'OK', { duration: 3000 }); this.loadAssignments(); },
        error: () => this.snackBar.open('Error al eliminar', 'OK', { duration: 3000 }),
      });
    });
  }

  private loadAssignments(): void {
    this.assignmentsLoading.set(true);
    this.assignmentService.findAllByShift(this.shiftId, { page: this.currentPage, size: this.pageSize }).subscribe({
      next: (p) => { this.assignments.set(p); this.assignmentsLoading.set(false); },
      error: () => {
        this.assignmentsLoading.set(false);
        this.snackBar.open('Error al cargar asignaciones', 'OK', { duration: 3000 });
      },
    });
  }
}
