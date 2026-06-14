export type ShiftType = 'MORNING' | 'AFTERNOON' | 'NIGHT';
export type DayOfWeek = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';

export interface ShiftCreateRequest {
  name: string;
  startTime: string;
  endTime: string;
  effectiveFrom: string;
  shiftType: ShiftType;
  availableDays: DayOfWeek[];
}

export interface ShiftUpdateRequest {
  name: string;
  startTime: string;
  endTime: string;
  shiftType: ShiftType;
  availableDays: DayOfWeek[];
}

export interface ShiftResponse {
  id: string;
  name: string;
  storeId: string;
  startTime: string;
  endTime: string;
  shiftType: ShiftType;
  availableDays: DayOfWeek[];
  crossesMidnight: boolean;
}

export interface UnassignedShiftResponse {
  shiftId: string;
  shiftName: string;
  unassignedDays: DayOfWeek[];
}

export interface ShiftCoverageSlot {
  date: string;
  dayOfWeek: DayOfWeek;
  shiftId: string;
  shiftName: string;
  shiftType: ShiftType;
  startTime: string;
  endTime: string;
  crossesMidnight: boolean;
  assigned: boolean;
  assignmentId: string | null;
  employeeId: string | null;
  employeeName: string | null;
}
