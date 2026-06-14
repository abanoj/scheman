import { Component, input } from '@angular/core';

@Component({
  selector: 'app-page-header',
  standalone: true,
  template: `
    <div class="page-header">
      <h1 class="title">{{ title() }}</h1>
      @if (subtitle()) {
        <p class="subtitle">{{ subtitle() }}</p>
      }
    </div>
  `,
  styles: [`
    .page-header {
      margin-bottom: 24px;
    }
    .title {
      margin: 0;
      font-size: 1.6rem;
      font-weight: 700;
      color: #0f172a;
      line-height: 1.2;
    }
    .subtitle {
      margin: 6px 0 0;
      color: #64748b;
      font-size: 0.9rem;
    }
  `],
})
export class PageHeaderComponent {
  readonly title = input.required<string>();
  readonly subtitle = input<string>();
}
