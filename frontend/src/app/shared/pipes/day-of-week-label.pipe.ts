import { Pipe, PipeTransform } from '@angular/core';
import { DayOfWeek } from '../../core/models/shift.models';

const LABELS: Record<DayOfWeek, string> = {
  MONDAY: 'Lunes',
  TUESDAY: 'Martes',
  WEDNESDAY: 'Miércoles',
  THURSDAY: 'Jueves',
  FRIDAY: 'Viernes',
  SATURDAY: 'Sábado',
  SUNDAY: 'Domingo',
};

const ORDER: DayOfWeek[] = [
  'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY',
];

/** Transforms a single DayOfWeek to its Spanish label. */
@Pipe({ name: 'dayOfWeekLabel', standalone: true })
export class DayOfWeekLabelPipe implements PipeTransform {
  transform(value: DayOfWeek | null | undefined): string {
    return value ? (LABELS[value] ?? value) : '';
  }
}

/** Transforms a collection of DayOfWeek into a sorted, comma-separated Spanish string. */
@Pipe({ name: 'daysOfWeekLabel', standalone: true })
export class DaysOfWeekLabelPipe implements PipeTransform {
  transform(value: Iterable<DayOfWeek> | null | undefined): string {
    if (!value) return '';
    const set = new Set(value);
    return ORDER.filter((d) => set.has(d)).map((d) => LABELS[d]).join(', ');
  }
}
