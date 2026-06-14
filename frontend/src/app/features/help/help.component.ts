import { Component, computed, inject } from '@angular/core';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { TokenService } from '../../core/auth/token.service';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-help',
  standalone: true,
  imports: [MatExpansionModule, MatIconModule, RouterLink, PageHeaderComponent],
  template: `
    <app-page-header
      title="Guía de uso"
      subtitle="Todo lo que necesitas saber para gestionar Scheman"
    />

    <mat-accordion class="help-accordion" multi>

      <!-- ── Roles y permisos ─────────────────────────── ADMIN · MANAGER ── -->
      @if (is('ADMIN') || is('MANAGER')) {
        <mat-expansion-panel [expanded]="true">
          <mat-expansion-panel-header>
            <mat-panel-title class="panel-title">
              <mat-icon>shield</mat-icon> Roles y permisos
            </mat-panel-title>
          </mat-expansion-panel-header>

          <p class="section-intro">
            Scheman tiene tres roles con acceso progresivo. Cada usuario solo ve y puede hacer
            lo que le corresponde a su nivel.
          </p>

          <div class="role-table-wrap">
            <table class="role-table">
              <thead>
                <tr>
                  <th>Función</th>
                  <th><span class="badge admin">ADMIN</span></th>
                  <th><span class="badge manager">MANAGER</span></th>
                  <th><span class="badge employee">EMPLEADO</span></th>
                </tr>
              </thead>
              <tbody>
                <tr><td>Gestionar managers</td>        <td class="yes">✓</td><td class="no">—</td><td class="no">—</td></tr>
                <tr><td>Gestionar empleados</td>       <td class="yes">✓</td><td class="yes">✓</td><td class="no">—</td></tr>
                <tr><td>Gestionar tiendas</td>         <td class="yes">✓</td><td class="yes">✓</td><td class="no">—</td></tr>
                <tr><td>Gestionar turnos</td>          <td class="yes">✓</td><td class="yes">✓</td><td class="no">—</td></tr>
                <tr><td>Asignar turnos</td>            <td class="yes">✓</td><td class="yes">✓</td><td class="no">—</td></tr>
                <tr><td>Ver cobertura semanal</td>     <td class="yes">✓</td><td class="yes">✓</td><td class="no">—</td></tr>
                <tr><td>Ver su propio horario</td>     <td class="no">—</td><td class="no">—</td><td class="yes">✓</td></tr>
              </tbody>
            </table>
          </div>

          <div class="info-box">
            <mat-icon>info</mat-icon>
            <span>Las contraseñas iniciales se asignan al crear la cuenta. El usuario debe cambiarlas
            en su primer acceso.</span>
          </div>
        </mat-expansion-panel>
      }

      <!-- ── Managers ─────────────────────────────────────────────── ADMIN ── -->
      @if (is('ADMIN')) {
        <mat-expansion-panel>
          <mat-expansion-panel-header>
            <mat-panel-title class="panel-title">
              <mat-icon>manage_accounts</mat-icon> Managers
            </mat-panel-title>
          </mat-expansion-panel-header>

          <p class="section-intro">
            Los managers pueden gestionar empleados, tiendas, turnos y asignaciones,
            pero no pueden crear otros managers ni acceder a la gestión de cuentas de administración.
          </p>

          <div class="step-list">
            <div class="step">
              <span class="step-num">1</span>
              <div>
                <strong>Crear un manager</strong> — Ve a
                <em>Managers → Nuevo manager</em>. Introduce nombre, apellidos, email
                y una contraseña inicial segura (mín. 8 caracteres, mayúscula, minúscula,
                número y símbolo).
              </div>
            </div>
            <div class="step">
              <span class="step-num">2</span>
              <div>
                <strong>Editar datos</strong> — Solo se puede modificar el nombre y apellidos.
                El email es el identificador de la cuenta y no cambia.
              </div>
            </div>
            <div class="step">
              <span class="step-num">3</span>
              <div>
                <strong>Habilitar / deshabilitar</strong> — Deshabilitar una cuenta impide
                el acceso al sistema sin eliminar ningún dato. Se puede volver a habilitar
                en cualquier momento.
              </div>
            </div>
          </div>
        </mat-expansion-panel>
      }

      <!-- ── Empleados ──────────────────────────────── ADMIN · MANAGER ── -->
      @if (is('ADMIN') || is('MANAGER')) {
        <mat-expansion-panel>
          <mat-expansion-panel-header>
            <mat-panel-title class="panel-title">
              <mat-icon>people</mat-icon> Empleados
            </mat-panel-title>
          </mat-expansion-panel-header>

          <p class="section-intro">
            Cada empleado tiene una cuenta de usuario asociada. Al crear el empleado
            se crea automáticamente su acceso al sistema.
          </p>

          <div class="step-list">
            <div class="step">
              <span class="step-num">1</span>
              <div>
                <strong>Crear empleado</strong> — El DNI actúa como contraseña inicial.
                El empleado deberá cambiarla en su primer acceso. Puedes indicar su turno
                preferente, horas contratadas semanales y tiendas de preferencia.
              </div>
            </div>
            <div class="step">
              <span class="step-num">2</span>
              <div>
                <strong>Ver detalle</strong> — Desde la lista, haz clic en el nombre para
                ver el perfil completo. Incluye una vista de horario semanal con la
                relación <em>horas asignadas / horas contratadas</em>.
              </div>
            </div>
            <div class="step">
              <span class="step-num">3</span>
              <div>
                <strong>Habilitar / deshabilitar</strong> — Igual que con los managers,
                el acceso se puede suspender y restaurar sin perder datos históricos.
              </div>
            </div>
          </div>

          <div class="info-box">
            <mat-icon>info</mat-icon>
            <span>Las <strong>horas contratadas semanales</strong> del empleado son el límite
            de referencia para el aviso de horas en las asignaciones.</span>
          </div>
        </mat-expansion-panel>
      }

      <!-- ── Tiendas ─────────────────────────────────── ADMIN · MANAGER ── -->
      @if (is('ADMIN') || is('MANAGER')) {
        <mat-expansion-panel>
          <mat-expansion-panel-header>
            <mat-panel-title class="panel-title">
              <mat-icon>store</mat-icon> Tiendas
            </mat-panel-title>
          </mat-expansion-panel-header>

          <p class="section-intro">
            Las tiendas son el contenedor principal. Cada tienda tiene sus propios turnos,
            y cada turno tiene sus propias asignaciones de empleados.
          </p>

          <div class="step-list">
            <div class="step">
              <span class="step-num">1</span>
              <div>
                <strong>Crear tienda</strong> — Nombre, dirección y teléfono (opcional).
              </div>
            </div>
            <div class="step">
              <span class="step-num">2</span>
              <div>
                <strong>Ver detalle</strong> — Desde la lista de tiendas, haz clic en el
                nombre para ver sus turnos activos y acceder a la cobertura semanal.
              </div>
            </div>
            <div class="step">
              <span class="step-num">3</span>
              <div>
                <strong>Eliminar tienda</strong> — Es un borrado lógico: la tienda desaparece
                de la lista pero los datos históricos se conservan.
              </div>
            </div>
          </div>
        </mat-expansion-panel>
      }

      <!-- ── Turnos ──────────────────────────────────── ADMIN · MANAGER ── -->
      @if (is('ADMIN') || is('MANAGER')) {
        <mat-expansion-panel>
          <mat-expansion-panel-header>
            <mat-panel-title class="panel-title">
              <mat-icon>schedule</mat-icon> Turnos
            </mat-panel-title>
          </mat-expansion-panel-header>

          <p class="section-intro">
            Los turnos se crean dentro de una tienda y definen el horario, el tipo y los
            días en los que ese hueco debe cubrirse.
          </p>

          <div class="step-list">
            <div class="step">
              <span class="step-num">1</span>
              <div>
                <strong>Crear turno</strong> — Desde el detalle de una tienda.
                Indica nombre, hora de inicio y fin, tipo (Mañana / Tarde / Noche)
                y los días de la semana en que aplica.
              </div>
            </div>
            <div class="step">
              <span class="step-num">2</span>
              <div>
                <strong>Turnos nocturnos</strong> — Si la hora de fin es anterior a la de
                inicio (p. ej. 22:00–06:00), el sistema detecta automáticamente que cruza
                la medianoche y lo marca con <em>+1</em>.
              </div>
            </div>
            <div class="step">
              <span class="step-num">3</span>
              <div>
                <strong>Fecha de inicio</strong> — Define desde cuándo el turno está activo.
                Los turnos sin fecha de fin no caducan.
              </div>
            </div>
          </div>
        </mat-expansion-panel>
      }

      <!-- ── Cobertura semanal ───────────────────────── ADMIN · MANAGER ── -->
      @if (is('ADMIN') || is('MANAGER')) {
        <mat-expansion-panel>
          <mat-expansion-panel-header>
            <mat-panel-title class="panel-title">
              <mat-icon>calendar_view_week</mat-icon> Cobertura semanal
            </mat-panel-title>
          </mat-expansion-panel-header>

          <p class="section-intro">
            La vista de cobertura muestra todos los turnos activos de una tienda durante
            una semana, indicando si cada hueco está cubierto o no.
          </p>

          <div class="legend-row">
            <span class="legend-chip assigned">Verde — Asignado</span>
            <span class="legend-chip unassigned">Amarillo — Sin asignar</span>
          </div>

          <div class="step-list">
            <div class="step">
              <span class="step-num">1</span>
              <div>
                <strong>Acceder</strong> — Desde el detalle de una tienda, pulsa
                <em>Ver cobertura semanal</em>.
              </div>
            </div>
            <div class="step">
              <span class="step-num">2</span>
              <div>
                <strong>Navegar semanas</strong> — Usa las flechas para ir hacia atrás
                o adelante. El botón <em>Semana actual</em> vuelve a hoy.
              </div>
            </div>
            <div class="step">
              <span class="step-num">3</span>
              <div>
                <strong>Asignar directamente</strong> — En los huecos sin cubrir aparece
                el botón <em>Asignar</em>. Al pulsarlo abre el formulario con la fecha
                ya preseleccionada. Al guardar vuelves a esta vista en la misma semana.
              </div>
            </div>
            <div class="step">
              <span class="step-num">4</span>
              <div>
                <strong>Editar asignación</strong> — Haz clic sobre el nombre del empleado
                en cualquier hueco cubierto para editar esa asignación.
              </div>
            </div>
          </div>
        </mat-expansion-panel>
      }

      <!-- ── Asignaciones ──────────────────────────────────────── ALL ── -->
      <mat-expansion-panel>
        <mat-expansion-panel-header>
          <mat-panel-title class="panel-title">
            <mat-icon>assignment_ind</mat-icon> Asignaciones
          </mat-panel-title>
        </mat-expansion-panel-header>

        @if (is('ADMIN') || is('MANAGER')) {
          <p class="section-intro">
            Una asignación vincula a un empleado con un turno en una fecha concreta.
            El sistema aplica varias reglas automáticamente para evitar conflictos.
          </p>
        } @else {
          <p class="section-intro">
            Estas son las normas que determinan cuándo y cómo se te puede asignar un turno.
          </p>
        }

        <div class="rules-grid">
          <div class="rule-card">
            <div class="rule-icon blue"><mat-icon>event_busy</mat-icon></div>
            <div>
              <strong>Un turno por día</strong>
              <p>Un empleado no puede tener dos turnos asignados el mismo día. El sistema
              bloquea la asignación si ya existe una ese día.</p>
            </div>
          </div>

          <div class="rule-card">
            <div class="rule-icon purple"><mat-icon>bedtime</mat-icon></div>
            <div>
              <strong>12 horas de descanso</strong>
              <p>Entre el fin de un turno y el inicio del siguiente deben transcurrir al menos
              12 horas. Esto aplica también a turnos que cruzan la medianoche.</p>
            </div>
          </div>

          <div class="rule-card">
            <div class="rule-icon orange"><mat-icon>warning</mat-icon></div>
            <div>
              <strong>Aviso de horas semanales</strong>
              <p>Si una asignación hace que el empleado supere sus horas contratadas en esa
              semana, aparece un aviso antes de confirmar y otro al guardar. No es un bloqueo:
              puedes continuar si es necesario.</p>
            </div>
          </div>
        </div>
      </mat-expansion-panel>

      <!-- ── Mi horario ─────────────────────────────────── EMPLOYEE ── -->
      @if (is('EMPLOYEE')) {
        <mat-expansion-panel [expanded]="true">
          <mat-expansion-panel-header>
            <mat-panel-title class="panel-title">
              <mat-icon>calendar_today</mat-icon> Mi horario
            </mat-panel-title>
          </mat-expansion-panel-header>

          <p class="section-intro">
            En <em>Mi Horario</em> puedes consultar todos los turnos que tienes asignados,
            semana a semana.
          </p>

          <div class="step-list">
            <div class="step">
              <span class="step-num">1</span>
              <div>
                <strong>Semana actual</strong> — Al entrar siempre ves la semana en curso,
                con el día de hoy resaltado.
              </div>
            </div>
            <div class="step">
              <span class="step-num">2</span>
              <div>
                <strong>Navegar semanas</strong> — Usa las flechas para consultar semanas
                pasadas o futuras. El botón <em>Semana actual</em> vuelve a hoy.
              </div>
            </div>
            <div class="step">
              <span class="step-num">3</span>
              <div>
                <strong>Resumen de horas</strong> — Encima del calendario se muestra
                el número de turnos y las horas totales de la semana.
              </div>
            </div>
          </div>

          <div class="info-box">
            <mat-icon>info</mat-icon>
            <span>Si ves un día marcado como <em>Libre</em>, no tienes ningún turno
            asignado ese día.</span>
          </div>
        </mat-expansion-panel>
      }

    </mat-accordion>
  `,
  styles: [`
    .help-accordion { display: block; max-width: 860px; }

    .panel-title {
      display: flex; align-items: center; gap: 10px;
      font-size: 1rem; font-weight: 600; color: #0f172a;
    }
    .panel-title mat-icon { color: #3b82f6; }

    .section-intro { color: #475569; font-size: 0.9rem; line-height: 1.6; margin: 0 0 20px; }

    /* Role table */
    .role-table-wrap { overflow-x: auto; margin-bottom: 20px; }
    .role-table { width: 100%; border-collapse: collapse; font-size: 0.875rem; }
    .role-table th, .role-table td { padding: 10px 14px; text-align: center; border-bottom: 1px solid #f1f5f9; }
    .role-table th:first-child, .role-table td:first-child { text-align: left; color: #334155; font-weight: 500; }
    .role-table th { background: #f8fafc; font-weight: 600; color: #64748b; font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.04em; }
    .yes { color: #059669; font-weight: 700; font-size: 1rem; }
    .no  { color: #cbd5e1; font-weight: 400; }

    .badge { padding: 3px 10px; border-radius: 20px; font-size: 0.72rem; font-weight: 700; letter-spacing: 0.04em; }
    .badge.admin    { background: #dbeafe; color: #1d4ed8; }
    .badge.manager  { background: #d1fae5; color: #059669; }
    .badge.employee { background: #ede9fe; color: #7c3aed; }

    /* Steps */
    .step-list { display: flex; flex-direction: column; gap: 14px; margin-bottom: 20px; }
    .step { display: flex; gap: 14px; align-items: flex-start; }
    .step-num {
      width: 26px; height: 26px; border-radius: 50%; background: #eff6ff; color: #2563eb;
      display: flex; align-items: center; justify-content: center;
      font-size: 0.8rem; font-weight: 700; flex-shrink: 0; margin-top: 1px;
    }
    .step div { font-size: 0.875rem; color: #334155; line-height: 1.55; }
    .step strong { color: #0f172a; }

    /* Rules grid */
    .rules-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 16px; margin-bottom: 8px; }
    .rule-card { display: flex; gap: 14px; align-items: flex-start; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 16px; }
    .rule-card div:last-child { display: flex; flex-direction: column; gap: 4px; }
    .rule-card strong { font-size: 0.9rem; color: #0f172a; }
    .rule-card p { margin: 0; font-size: 0.82rem; color: #64748b; line-height: 1.5; }
    .rule-icon { width: 38px; height: 38px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
    .rule-icon mat-icon { font-size: 1.2rem; width: 1.2rem; height: 1.2rem; }
    .rule-icon.blue   { background: #dbeafe; color: #1d4ed8; }
    .rule-icon.purple { background: #ede9fe; color: #7c3aed; }
    .rule-icon.orange { background: #fff7ed; color: #ea580c; }

    /* Legend */
    .legend-row { display: flex; gap: 12px; margin-bottom: 20px; flex-wrap: wrap; }
    .legend-chip { padding: 4px 12px; border-radius: 20px; font-size: 0.8rem; font-weight: 500; }
    .legend-chip.assigned   { background: #d1fae5; color: #065f46; }
    .legend-chip.unassigned { background: #fef3c7; color: #92400e; }

    /* Info box */
    .info-box {
      display: flex; align-items: flex-start; gap: 10px;
      background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 8px;
      padding: 12px 14px; color: #1e40af; font-size: 0.875rem; line-height: 1.5;
    }
    .info-box mat-icon { font-size: 1.1rem; width: 1.1rem; height: 1.1rem; flex-shrink: 0; margin-top: 1px; }
  `],
})
export class HelpComponent {
  private readonly tokenService = inject(TokenService);
  private readonly role = computed(() => this.tokenService.currentUser()?.role);

  is(role: string): boolean {
    return this.role() === role;
  }
}
