import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { EmployeeService } from '../../../core/services/employee.service';
import { EmployeeResponse } from '../../../core/models/employee.models';
import { Page } from '../../../core/models/page.models';
import { ShiftTypeLabelPipe } from '../../../shared/pipes/shift-type-label.pipe';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatChipsModule,
    ShiftTypeLabelPipe,
    PageHeaderComponent,
  ],
  template: `
    <app-page-header title="Empleados" subtitle="Gestiona el equipo" />

    <div class="toolbar">
      <a mat-flat-button color="primary" routerLink="new">
        <mat-icon>person_add</mat-icon> Nuevo empleado
      </a>
    </div>

    @if (loading()) {
      <div class="spinner-center"><mat-spinner /></div>
    } @else {
      <div class="table-wrapper mat-elevation-z1">
        <table mat-table [dataSource]="page()?.content ?? []">

          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Nombre</th>
            <td mat-cell *matCellDef="let e">
              <a class="name-link" [routerLink]="e.id">{{ e.firstName }} {{ e.lastName }}</a>
            </td>
          </ng-container>

          <ng-container matColumnDef="dni">
            <th mat-header-cell *matHeaderCellDef>DNI</th>
            <td mat-cell *matCellDef="let e">{{ e.dni }}</td>
          </ng-container>

          <ng-container matColumnDef="email">
            <th mat-header-cell *matHeaderCellDef>Email</th>
            <td mat-cell *matCellDef="let e">{{ e.email }}</td>
          </ng-container>

          <ng-container matColumnDef="shift">
            <th mat-header-cell *matHeaderCellDef>Turno</th>
            <td mat-cell *matCellDef="let e">{{ e.preferredShift | shiftTypeLabel }}</td>
          </ng-container>

          <ng-container matColumnDef="hours">
            <th mat-header-cell *matHeaderCellDef>Horas/sem</th>
            <td mat-cell *matCellDef="let e">{{ e.weeklyContractedHours }}h</td>
          </ng-container>

          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let e">
              <a mat-icon-button [routerLink]="e.id" matTooltip="Ver detalle">
                <mat-icon>visibility</mat-icon>
              </a>
              <a mat-icon-button [routerLink]="[e.id, 'edit']" matTooltip="Editar">
                <mat-icon>edit</mat-icon>
              </a>
              <button
                mat-icon-button
                color="warn"
                (click)="confirmDisable(e)"
                matTooltip="Deshabilitar"
              >
                <mat-icon>person_off</mat-icon>
              </button>
              <button
                mat-icon-button
                color="primary"
                (click)="confirmEnable(e)"
                matTooltip="Habilitar"
              >
                <mat-icon>how_to_reg</mat-icon>
              </button>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns;"></tr>

          @if (!page()?.content?.length) {
            <tr class="mat-mdc-no-data-row">
              <td [attr.colspan]="columns.length" class="no-data">No hay empleados registrados</td>
            </tr>
          }
        </table>

        <mat-paginator
          [length]="page()?.totalElements ?? 0"
          [pageSize]="pageSize"
          [pageSizeOptions]="[10, 25, 50]"
          (page)="onPageChange($event)"
          showFirstLastButtons
        />
      </div>
    }
  `,
  styles: [`
    .toolbar { display: flex; justify-content: flex-end; margin-bottom: 16px; }
    .table-wrapper { border-radius: 10px; overflow: hidden; background: #fff; }
    table { width: 100%; }
    .spinner-center { display: flex; justify-content: center; padding: 48px; }
    .name-link { font-weight: 500; color: #2563eb; text-decoration: none; }
    .name-link:hover { text-decoration: underline; }
    .no-data { padding: 32px; text-align: center; color: #94a3b8; }
  `],
})
export class EmployeeListComponent implements OnInit {
  private readonly employeeService = inject(EmployeeService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly loading = signal(true);
  readonly page = signal<Page<EmployeeResponse> | null>(null);
  readonly columns = ['name', 'dni', 'email', 'shift', 'hours', 'actions'];
  pageSize = 10;
  currentPage = 0;

  ngOnInit(): void {
    this.load();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.load();
  }

  confirmDisable(employee: EmployeeResponse): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Deshabilitar empleado',
        message: `¿Deshabilitar a ${employee.firstName} ${employee.lastName}? No podrá acceder al sistema.`,
        confirmLabel: 'Deshabilitar',
      },
    });
    ref.afterClosed().subscribe((ok) => { if (ok) this.disable(employee.id); });
  }

  confirmEnable(employee: EmployeeResponse): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Habilitar empleado',
        message: `¿Habilitar a ${employee.firstName} ${employee.lastName}? Recuperará acceso al sistema.`,
        confirmLabel: 'Habilitar',
      },
    });
    ref.afterClosed().subscribe((ok) => { if (ok) this.enable(employee.id); });
  }

  private load(): void {
    this.loading.set(true);
    this.employeeService.findAll({ page: this.currentPage, size: this.pageSize }).subscribe({
      next: (p) => { this.page.set(p); this.loading.set(false); },
      error: () => { this.loading.set(false); this.snackBar.open('Error al cargar empleados', 'OK', { duration: 3000 }); },
    });
  }

  private disable(id: string): void {
    this.employeeService.disable(id).subscribe({
      next: () => { this.snackBar.open('Empleado deshabilitado', 'OK', { duration: 3000 }); this.load(); },
      error: () => this.snackBar.open('Error al deshabilitar', 'OK', { duration: 3000 }),
    });
  }

  private enable(id: string): void {
    this.employeeService.enable(id).subscribe({
      next: () => { this.snackBar.open('Empleado habilitado', 'OK', { duration: 3000 }); this.load(); },
      error: () => this.snackBar.open('Error al habilitar', 'OK', { duration: 3000 }),
    });
  }
}
