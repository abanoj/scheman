import { ShiftResponse } from './shift.models';
import { EmployeeSummaryResponse } from './employee.models';

export interface StoreCreateRequest {
  name: string;
  address?: string;
  phone?: string;
  is24h?: boolean;
}

export interface StoreUpdateRequest {
  name?: string;
  address?: string;
  phone?: string;
  is24h?: boolean;
}

export interface StoreListResponse {
  id: string;
  name: string;
  address: string;
  phone: string;
  is24h: boolean;
}

export interface StoreResponse {
  id: string;
  name: string;
  address: string;
  phone: string;
  is24h: boolean;
  shifts: ShiftResponse[];
  preferredEmployees: EmployeeSummaryResponse[];
}

export interface StoreSummaryResponse {
  id: string;
  name: string;
}
