import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';
import { TokenService } from '../../core/auth/token.service';
import { EmployeeService } from '../../core/services/employee.service';
import { StoreService } from '../../core/services/store.service';
import { ShiftAssignmentService } from '../../core/services/shift-assignment.service';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [MatCardModule, MatIconModule, MatButtonModule, RouterLink, PageHeaderComponent],
  template: `
    <app-page-header
      [title]="'Hola, ' + (firstName() ?? 'usuario')"
      subtitle="Aquí tienes un resumen de la actividad"
    />

    <!-- ── ADMIN ─────────────────────────────────────────────────────────── -->
    @if (role() === 'ADMIN') {
      <div class="cards-grid">
        <mat-card class="stat-card">
          <mat-card-content class="stat-content">
            <div class="stat-icon-wrap blue"><mat-icon>people</mat-icon></div>
            <div class="stat-info">
              <span class="stat-label">Empleados</span>
              <span class="stat-value">{{ totalEmployees() ?? '—' }}</span>
            </div>
          </mat-card-content>
          <mat-card-actions>
            <a mat-button color="primary" routerLink="/employees">Ver todos</a>
            <a mat-button routerLink="/employees/new">+ Nuevo empleado</a>
          </mat-card-actions>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content class="stat-content">
            <div class="stat-icon-wrap green"><mat-icon>store</mat-icon></div>
            <div class="stat-info">
              <span class="stat-label">Tiendas</span>
              <span class="stat-value">{{ totalStores() ?? '—' }}</span>
            </div>
          </mat-card-content>
          <mat-card-actions>
            <a mat-button color="primary" routerLink="/stores">Ver todas</a>
            <a mat-button routerLink="/stores/new">+ Nueva tienda</a>
          </mat-card-actions>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content class="stat-content">
            <div class="stat-icon-wrap purple"><mat-icon>manage_accounts</mat-icon></div>
            <div class="stat-info">
              <span class="stat-label">Managers</span>
              <span class="stat-sublabel">Cuentas con acceso de manager</span>
            </div>
          </mat-card-content>
          <mat-card-actions>
            <a mat-button color="primary" routerLink="/managers">Ver todos</a>
            <a mat-button routerLink="/managers/new">+ Nuevo manager</a>
          </mat-card-actions>
        </mat-card>
      </div>
    }

    <!-- ── MANAGER ────────────────────────────────────────────────────────── -->
    @if (role() === 'MANAGER') {
      <div class="cards-grid">
        <mat-card class="stat-card">
          <mat-card-content class="stat-content">
            <div class="stat-icon-wrap blue"><mat-icon>people</mat-icon></div>
            <div class="stat-info">
              <span class="stat-label">Empleados</span>
              <span class="stat-value">{{ totalEmployees() ?? '—' }}</span>
            </div>
          </mat-card-content>
          <mat-card-actions>
            <a mat-button color="primary" routerLink="/employees">Ver todos</a>
            <a mat-button routerLink="/employees/new">+ Nuevo empleado</a>
          </mat-card-actions>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content class="stat-content">
            <div class="stat-icon-wrap green"><mat-icon>store</mat-icon></div>
            <div class="stat-info">
              <span class="stat-label">Tiendas</span>
              <span class="stat-value">{{ totalStores() ?? '—' }}</span>
            </div>
          </mat-card-content>
          <mat-card-actions>
            <a mat-button color="primary" routerLink="/stores">Ver todas</a>
            <a mat-button routerLink="/stores/new">+ Nueva tienda</a>
          </mat-card-actions>
        </mat-card>
      </div>
    }

    <!-- ── EMPLOYEE ───────────────────────────────────────────────────────── -->
    @if (role() === 'EMPLOYEE') {
      <div class="cards-grid">
        <mat-card class="stat-card">
          <mat-card-content class="stat-content">
            <div class="stat-icon-wrap blue"><mat-icon>calendar_today</mat-icon></div>
            <div class="stat-info">
              <span class="stat-label">Mi horario esta semana</span>
              <span class="stat-value">{{ weeklyAssignments() ?? '—' }}</span>
              <span class="stat-unit">turnos asignados</span>
            </div>
          </mat-card-content>
          <mat-card-actions>
            <a mat-button color="primary" routerLink="/my-schedule">Ver horario</a>
          </mat-card-actions>
        </mat-card>
      </div>
    }
  `,
  styles: [`
    .cards-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
      gap: 20px;
      max-width: 960px;
    }

    .stat-card { background: #ffffff !important; }

    .stat-content {
      display: flex;
      align-items: center;
      gap: 18px;
      padding: 20px 16px 8px !important;
    }

    .stat-icon-wrap {
      width: 52px; height: 52px; border-radius: 12px;
      display: flex; align-items: center; justify-content: center; flex-shrink: 0;
    }
    .stat-icon-wrap.blue   { background: #dbeafe; color: #1d4ed8; }
    .stat-icon-wrap.green  { background: #d1fae5; color: #059669; }
    .stat-icon-wrap.purple { background: #ede9fe; color: #7c3aed; }

    .stat-icon-wrap mat-icon { font-size: 1.6rem; width: 1.6rem; height: 1.6rem; }

    .stat-info { display: flex; flex-direction: column; }
    .stat-label  { font-size: 0.78rem; font-weight: 600; color: #64748b; text-transform: uppercase; letter-spacing: 0.06em; }
    .stat-value  { font-size: 2rem; font-weight: 700; color: #0f172a; line-height: 1.1; }
    .stat-sublabel { font-size: 0.85rem; color: #64748b; margin-top: 2px; }
    .stat-unit   { font-size: 0.8rem; color: #94a3b8; }
  `],
})
export class DashboardComponent implements OnInit {
  private readonly tokenService = inject(TokenService);
  private readonly employeeService = inject(EmployeeService);
  private readonly storeService = inject(StoreService);
  private readonly assignmentService = inject(ShiftAssignmentService);

  readonly totalEmployees = signal<number | null>(null);
  readonly totalStores = signal<number | null>(null);
  readonly weeklyAssignments = signal<number | null>(null);

  readonly user = this.tokenService.currentUser;
  readonly role = computed(() => this.user()?.role);
  readonly firstName = computed(() => this.user()?.email?.split('@')[0]);

  ngOnInit(): void {
    const r = this.role();
    if (r === 'ADMIN' || r === 'MANAGER') {
      this.employeeService.findAll({ size: 1 }).subscribe((p) => this.totalEmployees.set(p.totalElements));
      this.storeService.findAll({ size: 1 }).subscribe((p) => this.totalStores.set(p.totalElements));
    }
    if (r === 'EMPLOYEE') {
      const userId = this.user()!.id;
      const today = new Date().toISOString().split('T')[0];
      this.assignmentService.findWeeklyByEmployee(userId, today).subscribe((list) =>
        this.weeklyAssignments.set(list.length)
      );
    }
  }
}
