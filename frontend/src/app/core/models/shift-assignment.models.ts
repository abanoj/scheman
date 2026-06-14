import { ShiftType } from './shift.models';

export interface ShiftAssignmentCreateRequest {
  date: string;
  employeeId: string;
}

export interface ShiftAssignmentUpdateRequest {
  date: string;
  employeeId: string;
}

export interface ShiftAssignmentResponse {
  id: string;
  date: string;
  employeeId: string;
  shiftId: string;
  warning?: string;
}

export interface WeeklyAssignmentResponse {
  id: string;
  date: string;
  shiftId: string;
  shiftName: string;
  startTime: string;
  endTime: string;
  shiftType: ShiftType;
  crossesMidnight: boolean;
  totalHours: number;
  storeId: string;
  storeName: string;
}
