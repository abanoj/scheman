import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { mustChangePasswordGuard } from './core/guards/must-change-password.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  // ── Rutas públicas ──────────────────────────────────────────────────────────
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },

  // ── Cambio de contraseña (requiere auth pero no depende del shell) ───────────
  {
    path: 'change-password',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/auth/change-password/change-password.component').then(
        (m) => m.ChangePasswordComponent
      ),
  },

  // ── Shell autenticado ────────────────────────────────────────────────────────
  {
    path: '',
    canActivate: [authGuard, mustChangePasswordGuard],
    loadComponent: () =>
      import('./layout/shell/shell.component').then((m) => m.ShellComponent),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },

      // Dashboard
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
      },

      // Managers (solo ADMIN)
      {
        path: 'managers',
        canActivate: [roleGuard(['ADMIN'])],
        children: [
          {
            path: '',
            loadComponent: () =>
              import('./features/managers/manager-list/manager-list.component').then(
                (m) => m.ManagerListComponent
              ),
          },
          {
            path: 'new',
            loadComponent: () =>
              import('./features/managers/manager-form/manager-form.component').then(
                (m) => m.ManagerFormComponent
              ),
          },
          {
            path: ':id/edit',
            loadComponent: () =>
              import('./features/managers/manager-form/manager-form.component').then(
                (m) => m.ManagerFormComponent
              ),
          },
        ],
      },

      // Empleados (ADMIN y MANAGER)
      {
        path: 'employees',
        canActivate: [roleGuard(['ADMIN', 'MANAGER'])],
        children: [
          {
            path: '',
            loadComponent: () =>
              import('./features/employees/employee-list/employee-list.component').then(
                (m) => m.EmployeeListComponent
              ),
          },
          {
            path: 'new',
            loadComponent: () =>
              import('./features/employees/employee-form/employee-form.component').then(
                (m) => m.EmployeeFormComponent
              ),
          },
          {
            path: ':id',
            loadComponent: () =>
              import('./features/employees/employee-detail/employee-detail.component').then(
                (m) => m.EmployeeDetailComponent
              ),
          },
          {
            path: ':id/edit',
            loadComponent: () =>
              import('./features/employees/employee-form/employee-form.component').then(
                (m) => m.EmployeeFormComponent
              ),
          },
        ],
      },

      // Tiendas (ADMIN y MANAGER)
      {
        path: 'stores',
        canActivate: [roleGuard(['ADMIN', 'MANAGER'])],
        children: [
          {
            path: '',
            loadComponent: () =>
              import('./features/stores/store-list/store-list.component').then(
                (m) => m.StoreListComponent
              ),
          },
          {
            path: 'new',
            loadComponent: () =>
              import('./features/stores/store-form/store-form.component').then(
                (m) => m.StoreFormComponent
              ),
          },
          {
            path: ':id/edit',
            loadComponent: () =>
              import('./features/stores/store-form/store-form.component').then(
                (m) => m.StoreFormComponent
              ),
          },
          // Cobertura semanal de turnos de la tienda
          {
            path: ':storeId/coverage',
            loadComponent: () =>
              import('./features/shifts/shift-coverage/shift-coverage.component').then(
                (m) => m.ShiftCoverageComponent
              ),
          },
          // Turnos
          {
            path: ':storeId/shifts/new',
            loadComponent: () =>
              import('./features/shifts/shift-form/shift-form.component').then(
                (m) => m.ShiftFormComponent
              ),
          },
          {
            path: ':storeId/shifts/:shiftId/edit',
            loadComponent: () =>
              import('./features/shifts/shift-form/shift-form.component').then(
                (m) => m.ShiftFormComponent
              ),
          },
          {
            path: ':storeId/shifts/:shiftId/assignments/new',
            loadComponent: () =>
              import(
                './features/shift-assignments/assignment-form/assignment-form.component'
              ).then((m) => m.AssignmentFormComponent),
          },
          {
            path: ':storeId/shifts/:shiftId/assignments/:assignmentId/edit',
            loadComponent: () =>
              import(
                './features/shift-assignments/assignment-form/assignment-form.component'
              ).then((m) => m.AssignmentFormComponent),
          },
          // Detalle de turno (info + asignaciones). Después de las rutas más específicas.
          {
            path: ':storeId/shifts/:shiftId',
            loadComponent: () =>
              import('./features/shifts/shift-detail/shift-detail.component').then(
                (m) => m.ShiftDetailComponent
              ),
          },
          // Detalle de tienda - al final para no capturar rutas anteriores con :id
          {
            path: ':id',
            loadComponent: () =>
              import('./features/stores/store-detail/store-detail.component').then(
                (m) => m.StoreDetailComponent
              ),
          },
        ],
      },

      // Guía de uso (todos los roles)
      {
        path: 'help',
        loadComponent: () =>
          import('./features/help/help.component').then((m) => m.HelpComponent),
      },

      // Mi horario semanal (solo EMPLOYEE)
      {
        path: 'my-schedule',
        canActivate: [roleGuard(['EMPLOYEE'])],
        loadComponent: () =>
          import('./features/my-schedule/my-schedule.component').then((m) => m.MyScheduleComponent),
      },
    ],
  },

  { path: '**', redirectTo: '' },
];
