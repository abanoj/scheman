import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { HeaderComponent } from '../header/header.component';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, MatSidenavModule, SidebarComponent, HeaderComponent],
  template: `
    <mat-sidenav-container class="shell-container">
      <mat-sidenav mode="side" opened class="sidenav">
        <app-sidebar />
      </mat-sidenav>
      <mat-sidenav-content class="main-content">
        <app-header />
        <main class="page-content">
          <router-outlet />
        </main>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    .shell-container {
      height: 100vh;
      background: #f1f5f9;
    }
    .sidenav {
      width: 248px;
      background: #1e293b;
      border-right: none;
      box-shadow: 2px 0 12px rgba(0,0,0,0.15);
    }
    .main-content {
      display: flex;
      flex-direction: column;
      height: 100%;
      background: #f1f5f9;
    }
    .page-content {
      flex: 1;
      padding: 28px 32px;
      overflow-y: auto;
    }
  `],
})
export class ShellComponent {}
