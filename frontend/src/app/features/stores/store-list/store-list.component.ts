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
import { StoreService } from '../../../core/services/store.service';
import { StoreListResponse } from '../../../core/models/store.models';
import { Page } from '../../../core/models/page.models';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-store-list',
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
    <app-page-header title="Tiendas" subtitle="Gestiona las tiendas y sus turnos" />

    <div class="toolbar">
      <a mat-raised-button color="primary" routerLink="new">
        <mat-icon>add_business</mat-icon> Nueva tienda
      </a>
    </div>

    @if (loading()) {
      <div class="spinner-center"><mat-spinner /></div>
    } @else {
      <div class="table-wrapper mat-elevation-z2">
        <table mat-table [dataSource]="page()?.content ?? []">
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Nombre</th>
            <td mat-cell *matCellDef="let s">
              <a [routerLink]="s.id" class="link">{{ s.name }}</a>
            </td>
          </ng-container>

          <ng-container matColumnDef="address">
            <th mat-header-cell *matHeaderCellDef>Dirección</th>
            <td mat-cell *matCellDef="let s">{{ s.address ?? '-' }}</td>
          </ng-container>

          <ng-container matColumnDef="phone">
            <th mat-header-cell *matHeaderCellDef>Teléfono</th>
            <td mat-cell *matCellDef="let s">{{ s.phone ?? '-' }}</td>
          </ng-container>

          <ng-container matColumnDef="is24h">
            <th mat-header-cell *matHeaderCellDef>24h</th>
            <td mat-cell *matCellDef="let s">{{ s.is24h ? 'Sí' : 'No' }}</td>
          </ng-container>

          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let s">
              <a mat-icon-button [routerLink]="s.id" matTooltip="Ver detalle">
                <mat-icon>visibility</mat-icon>
              </a>
              <a mat-icon-button [routerLink]="[s.id, 'edit']" matTooltip="Editar">
                <mat-icon>edit</mat-icon>
              </a>
              <button mat-icon-button color="warn" (click)="confirmDelete(s)" matTooltip="Eliminar">
                <mat-icon>delete</mat-icon>
              </button>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns;"></tr>
        </table>

        <mat-paginator
          [length]="page()?.totalElements ?? 0"
          [pageSize]="pageSize"
          [pageSizeOptions]="[10, 25, 50]"
          (page)="onPageChange($event)"
        />
      </div>
    }
  `,
  styles: [`
    .toolbar { display: flex; justify-content: flex-end; margin-bottom: 16px; }
    .table-wrapper { border-radius: 8px; overflow: hidden; }
    table { width: 100%; }
    .spinner-center { display: flex; justify-content: center; padding: 48px; }
    .link { color: inherit; text-decoration: none; font-weight: 500; }
    .link:hover { text-decoration: underline; }
  `],
})
export class StoreListComponent implements OnInit {
  private readonly storeService = inject(StoreService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly loading = signal(true);
  readonly page = signal<Page<StoreListResponse> | null>(null);
  readonly columns = ['name', 'address', 'phone', 'is24h', 'actions'];
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

  confirmDelete(store: StoreListResponse): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Eliminar tienda',
        message: `¿Eliminar "${store.name}"? Esta acción no se puede deshacer.`,
        confirmLabel: 'Eliminar',
      },
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) this.delete(store.id);
    });
  }

  private load(): void {
    this.loading.set(true);
    this.storeService.findAll({ page: this.currentPage, size: this.pageSize }).subscribe({
      next: (p) => { this.page.set(p); this.loading.set(false); },
      error: () => { this.loading.set(false); this.snackBar.open('Error al cargar tiendas', 'OK', { duration: 3000 }); },
    });
  }

  private delete(id: string): void {
    this.storeService.delete(id).subscribe({
      next: () => { this.snackBar.open('Tienda eliminada', 'OK', { duration: 3000 }); this.load(); },
      error: (err) => this.snackBar.open(err.error?.message ?? 'Error al eliminar', 'OK', { duration: 3000 }),
    });
  }
}
