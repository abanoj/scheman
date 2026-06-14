import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ManagerService } from '../../../core/services/manager.service';
import { ManagerResponse } from '../../../core/models/auth.models';
import { Page } from '../../../core/models/page.models';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-manager-list',
  standalone: true,
  imports: [
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatSnackBarModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    PageHeaderComponent,
  ],
  template: `
    <app-page-header title="Managers" subtitle="Gestiona las cuentas de manager" />

    <div class="toolbar">
      <a mat-flat-button color="primary" routerLink="new">
        <mat-icon>person_add</mat-icon> Nuevo manager
      </a>
    </div>

    @if (loading()) {
      <div class="spinner-center"><mat-spinner /></div>
    } @else {
      <div class="table-wrapper mat-elevation-z1">
        <table mat-table [dataSource]="page()?.content ?? []">

          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Nombre</th>
            <td mat-cell *matCellDef="let m">{{ m.firstName }} {{ m.lastName }}</td>
          </ng-container>

          <ng-container matColumnDef="email">
            <th mat-header-cell *matHeaderCellDef>Email</th>
            <td mat-cell *matCellDef="let m">{{ m.email }}</td>
          </ng-container>

          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Estado</th>
            <td mat-cell *matCellDef="let m">
              <mat-chip [class]="m.enabled ? 'chip-active' : 'chip-inactive'">
                {{ m.enabled ? 'Activo' : 'Inactivo' }}
              </mat-chip>
            </td>
          </ng-container>

          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let m">
              <a mat-icon-button [routerLink]="[m.id, 'edit']" matTooltip="Editar">
                <mat-icon>edit</mat-icon>
              </a>
              <button
                mat-icon-button
                color="warn"
                (click)="confirmDisable(m)"
                matTooltip="Deshabilitar"
                [disabled]="!m.enabled"
              >
                <mat-icon>person_off</mat-icon>
              </button>
              <button
                mat-icon-button
                color="primary"
                (click)="confirmEnable(m)"
                matTooltip="Habilitar"
                [disabled]="m.enabled"
              >
                <mat-icon>how_to_reg</mat-icon>
              </button>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns;"></tr>

          @if (!page()?.content?.length) {
            <tr class="mat-mdc-no-data-row">
              <td [attr.colspan]="columns.length" class="no-data">No hay managers registrados</td>
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
    .no-data { padding: 32px; text-align: center; color: #94a3b8; }
    .chip-active  { --mdc-chip-label-text-color: #059669; background: #d1fae5 !important; }
    .chip-inactive { --mdc-chip-label-text-color: #9ca3af; background: #f3f4f6 !important; }
  `],
})
export class ManagerListComponent implements OnInit {
  private readonly managerService = inject(ManagerService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly loading = signal(true);
  readonly page = signal<Page<ManagerResponse> | null>(null);
  readonly columns = ['name', 'email', 'status', 'actions'];
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

  confirmDisable(manager: ManagerResponse): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Deshabilitar manager',
        message: `¿Deshabilitar a ${manager.firstName} ${manager.lastName}? No podrá acceder al sistema.`,
        confirmLabel: 'Deshabilitar',
      },
    });
    ref.afterClosed().subscribe((ok) => { if (ok) this.disable(manager.id); });
  }

  confirmEnable(manager: ManagerResponse): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Habilitar manager',
        message: `¿Habilitar a ${manager.firstName} ${manager.lastName}? Recuperará acceso al sistema.`,
        confirmLabel: 'Habilitar',
      },
    });
    ref.afterClosed().subscribe((ok) => { if (ok) this.enable(manager.id); });
  }

  private load(): void {
    this.loading.set(true);
    this.managerService.findAll({ page: this.currentPage, size: this.pageSize }).subscribe({
      next: (p) => { this.page.set(p); this.loading.set(false); },
      error: () => { this.loading.set(false); this.snackBar.open('Error al cargar managers', 'OK', { duration: 3000 }); },
    });
  }

  private enable(id: string): void {
    this.managerService.enable(id).subscribe({
      next: () => { this.snackBar.open('Manager habilitado', 'OK', { duration: 3000 }); this.load(); },
      error: () => this.snackBar.open('Error al habilitar', 'OK', { duration: 3000 }),
    });
  }

  private disable(id: string): void {
    this.managerService.disable(id).subscribe({
      next: () => { this.snackBar.open('Manager deshabilitado', 'OK', { duration: 3000 }); this.load(); },
      error: () => this.snackBar.open('Error al deshabilitar', 'OK', { duration: 3000 }),
    });
  }
}
