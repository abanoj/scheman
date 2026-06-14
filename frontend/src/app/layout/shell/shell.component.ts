import { Component, inject, signal, ViewChild, OnInit, OnDestroy } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MatSidenav, MatSidenavModule } from '@angular/material/sidenav';
import { BreakpointObserver } from '@angular/cdk/layout';
import { Subscription } from 'rxjs';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { HeaderComponent } from '../header/header.component';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, MatSidenavModule, SidebarComponent, HeaderComponent],
  template: `
    <mat-sidenav-container class="shell-container">
      <mat-sidenav
        #sidenav
        [mode]="isMobile() ? 'over' : 'side'"
        [opened]="!isMobile()"
        class="sidenav"
      >
        <app-sidebar (itemClicked)="onNavItemClicked()" />
      </mat-sidenav>

      <mat-sidenav-content class="main-content">
        <app-header
          [showMenuButton]="isMobile()"
          (menuToggle)="sidenav.toggle()"
        />
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
    @media (max-width: 768px) {
      .page-content { padding: 16px; }
    }
  `],
})
export class ShellComponent implements OnInit, OnDestroy {
  @ViewChild('sidenav') sidenav!: MatSidenav;

  private readonly breakpointObserver = inject(BreakpointObserver);
  private sub?: Subscription;

  readonly isMobile = signal(false);

  ngOnInit(): void {
    this.sub = this.breakpointObserver
      .observe('(max-width: 768px)')
      .subscribe((state) => this.isMobile.set(state.matches));
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  onNavItemClicked(): void {
    if (this.isMobile()) {
      this.sidenav.close();
    }
  }
}
