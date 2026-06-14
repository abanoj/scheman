import { Component, inject, Input, Output, EventEmitter } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { RouterLink } from '@angular/router';
import { TokenService } from '../../core/auth/token.service';
import { AuthService } from '../../core/auth/auth.service';

const ROLE_LABELS: Record<string, string> = {
  ADMIN: 'Administrador',
  MANAGER: 'Manager',
  EMPLOYEE: 'Empleado',
};

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDividerModule,
    RouterLink,
  ],
  template: `
    <mat-toolbar class="app-header">
      @if (showMenuButton) {
        <button mat-icon-button class="menu-btn" (click)="menuToggle.emit()">
          <mat-icon>menu</mat-icon>
        </button>
      }

      <span class="spacer"></span>

      <div class="user-trigger" [matMenuTriggerFor]="userMenu">
        <div class="avatar">{{ initials() }}</div>
        <div class="user-info hide-mobile">
          <span class="user-email">{{ user()?.email }}</span>
          <span class="user-role">{{ roleLabel() }}</span>
        </div>
        <mat-icon class="chevron hide-mobile">expand_more</mat-icon>
      </div>

      <mat-menu #userMenu="matMenu" xPosition="before">
        <div class="menu-header">
          <div class="menu-avatar">{{ initials() }}</div>
          <div>
            <div class="menu-email">{{ user()?.email }}</div>
            <div class="menu-role">{{ roleLabel() }}</div>
          </div>
        </div>
        <mat-divider />
        <a mat-menu-item [routerLink]="['/change-password']">
          <mat-icon>lock_outline</mat-icon>
          <span>Cambiar contraseña</span>
        </a>
        <mat-divider />
        <button mat-menu-item (click)="logout()" class="logout-item">
          <mat-icon>logout</mat-icon>
          <span>Cerrar sesión</span>
        </button>
      </mat-menu>
    </mat-toolbar>
  `,
  styles: [`
    .app-header {
      background: #ffffff;
      border-bottom: 1px solid #e2e8f0;
      height: 64px;
      padding: 0 24px;
      box-shadow: 0 1px 4px rgba(0,0,0,0.06);
      color: #1e293b;
    }

    .menu-btn { color: #64748b; margin-right: 4px; }

    .spacer { flex: 1; }

    .user-trigger {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 6px 10px;
      border-radius: 10px;
      cursor: pointer;
      transition: background 0.15s;
    }

    .user-trigger:hover { background: #f1f5f9; }

    .avatar {
      width: 34px; height: 34px;
      background: #dbeafe; color: #1d4ed8;
      border-radius: 50%;
      display: flex; align-items: center; justify-content: center;
      font-weight: 700; font-size: 0.85rem; flex-shrink: 0;
    }

    .user-info { display: flex; flex-direction: column; align-items: flex-start; line-height: 1.2; }
    .user-email { font-size: 0.8rem; font-weight: 500; color: #1e293b; }
    .user-role  { font-size: 0.72rem; color: #64748b; }
    .chevron    { color: #94a3b8; font-size: 1.1rem; width: 1.1rem; height: 1.1rem; }

    @media (max-width: 768px) {
      .app-header { padding: 0 12px; }
      .hide-mobile { display: none; }
    }

    .menu-header {
      display: flex; align-items: center; gap: 12px;
      padding: 14px 16px; pointer-events: none;
    }
    .menu-avatar {
      width: 38px; height: 38px; background: #dbeafe; color: #1d4ed8;
      border-radius: 50%; display: flex; align-items: center; justify-content: center;
      font-weight: 700; font-size: 0.9rem;
    }
    .menu-email { font-size: 0.85rem; font-weight: 600; color: #1e293b; }
    .menu-role  { font-size: 0.75rem; color: #64748b; }
    .logout-item { color: #dc2626 !important; }
    .logout-item mat-icon { color: #dc2626 !important; }
  `],
})
export class HeaderComponent {
  @Input() showMenuButton = false;
  @Output() menuToggle = new EventEmitter<void>();

  private readonly authService = inject(AuthService);
  readonly user = inject(TokenService).currentUser;

  initials(): string {
    const email = this.user()?.email ?? '';
    return email.substring(0, 2).toUpperCase();
  }

  roleLabel(): string {
    const role = this.user()?.role;
    return role ? (ROLE_LABELS[role] ?? role) : '';
  }

  logout(): void {
    this.authService.logout();
  }
}
