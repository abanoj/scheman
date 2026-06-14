import { Component, computed, inject, Output, EventEmitter } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { TokenService } from '../../core/auth/token.service';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles: string[];
  dividerBefore?: boolean;
}

const NAV_ITEMS: NavItem[] = [
  { label: 'Dashboard',      icon: 'dashboard',     route: '/dashboard',    roles: ['ADMIN', 'MANAGER', 'EMPLOYEE'] },
  { label: 'Empleados',      icon: 'people',        route: '/employees',    roles: ['ADMIN', 'MANAGER'] },
  { label: 'Tiendas',        icon: 'store',         route: '/stores',       roles: ['ADMIN', 'MANAGER'] },
  { label: 'Mi Horario',     icon: 'calendar_today',route: '/my-schedule',  roles: ['EMPLOYEE'] },
  { label: 'Managers',       icon: 'manage_accounts',route: '/managers',    roles: ['ADMIN'], dividerBefore: true },
  { label: 'Ayuda',          icon: 'help_outline',   route: '/help',        roles: ['ADMIN', 'MANAGER', 'EMPLOYEE'], dividerBefore: true },
];

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, MatIconModule, MatDividerModule],
  template: `
    <div class="sidebar-brand">
      <div class="brand-icon">S</div>
      <span class="brand-name">Scheman</span>
    </div>

    <nav class="nav-list">
      @for (item of visibleItems(); track item.route) {
        @if (item.dividerBefore) {
          <mat-divider class="nav-divider" />
        }
        <a
          [routerLink]="item.route"
          routerLinkActive="active"
          [routerLinkActiveOptions]="{ exact: item.route === '/dashboard' }"
          class="nav-item"
          (click)="itemClicked.emit()"
        >
          <mat-icon class="nav-icon">{{ item.icon }}</mat-icon>
          <span class="nav-label">{{ item.label }}</span>
        </a>
      }
    </nav>
  `,
  styles: [`
    :host { display: flex; flex-direction: column; height: 100%; }

    .sidebar-brand {
      display: flex; align-items: center; gap: 12px;
      padding: 24px 20px 20px;
      border-bottom: 1px solid rgba(255,255,255,0.08);
      margin-bottom: 8px;
    }
    .brand-icon {
      width: 36px; height: 36px; background: #3b82f6; border-radius: 9px;
      display: flex; align-items: center; justify-content: center;
      font-size: 1.1rem; font-weight: 800; color: #fff; flex-shrink: 0;
    }
    .brand-name { font-size: 1.2rem; font-weight: 700; color: #f8fafc; letter-spacing: 0.02em; }

    .nav-list { display: flex; flex-direction: column; padding: 4px 12px; gap: 2px; }

    .nav-divider { border-color: rgba(255,255,255,0.08) !important; margin: 8px 0 6px; }

    .nav-item {
      display: flex; align-items: center; gap: 12px;
      padding: 11px 14px; border-radius: 10px;
      text-decoration: none; color: #94a3b8;
      font-size: 0.9rem; font-weight: 500;
      transition: background 0.15s, color 0.15s;
    }
    .nav-item:hover { background: rgba(255,255,255,0.07); color: #e2e8f0; }
    .nav-item.active { background: rgba(59,130,246,0.2); color: #93c5fd; }
    .nav-item.active .nav-icon { color: #60a5fa; }
    .nav-icon { font-size: 1.25rem; width: 1.25rem; height: 1.25rem; flex-shrink: 0; }
  `],
})
export class SidebarComponent {
  @Output() itemClicked = new EventEmitter<void>();

  private readonly tokenService = inject(TokenService);

  readonly visibleItems = computed(() => {
    const role = this.tokenService.currentUser()?.role;
    if (!role) return [];
    return NAV_ITEMS.filter((item) => item.roles.includes(role));
  });
}
