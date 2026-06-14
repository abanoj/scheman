import { Pipe, PipeTransform } from '@angular/core';
import { ShiftType } from '../../core/models/shift.models';

const LABELS: Record<ShiftType, string> = {
  MORNING: 'Mañana',
  AFTERNOON: 'Tarde',
  NIGHT: 'Noche',
};

@Pipe({ name: 'shiftTypeLabel', standalone: true })
export class ShiftTypeLabelPipe implements PipeTransform {
  transform(value: ShiftType | null | undefined): string {
    return value ? (LABELS[value] ?? value) : '-';
  }
}
