import { ShiftType } from './shift.models';
import { StoreSummaryResponse } from './store.models';

export interface EmployeeCreateRequest {
  dni: string;
  firstName: string;
  lastName: string;
  email: string;
  preferredShift: ShiftType;
  preferredStoresIDs: string[];
  weeklyContractedHours: number;
}

export interface EmployeeUpdateRequest {
  dni?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  preferredShift?: ShiftType;
  preferredStoresIds?: string[];
  weeklyContractedHours?: number;
}

export interface EmployeeResponse {
  id: string;
  dni: string;
  firstName: string;
  lastName: string;
  email: string;
  userId: string;
  preferredShift: ShiftType;
  preferredStores: StoreSummaryResponse[];
  weeklyContractedHours: number;
}

export interface EmployeeSummaryResponse {
  id: string;
  name: string;
}
