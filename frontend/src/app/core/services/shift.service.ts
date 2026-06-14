import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page, PageParams } from '../models/page.models';
import {
  ShiftCreateRequest,
  ShiftUpdateRequest,
  ShiftResponse,
  UnassignedShiftResponse,
  ShiftCoverageSlot,
} from '../models/shift.models';

@Injectable({ providedIn: 'root' })
export class ShiftService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  private storeShiftsUrl(storeId: string): string {
    return `${this.apiUrl}/stores/${storeId}/shifts`;
  }

  findAllByStore(storeId: string, params?: PageParams): Observable<Page<ShiftResponse>> {
    let p = new HttpParams();
    if (params?.page !== undefined) p = p.set('page', params.page);
    if (params?.size !== undefined) p = p.set('size', params.size);
    return this.http.get<Page<ShiftResponse>>(this.storeShiftsUrl(storeId), { params: p });
  }

  findById(storeId: string, shiftId: string): Observable<ShiftResponse> {
    return this.http.get<ShiftResponse>(`${this.storeShiftsUrl(storeId)}/${shiftId}`);
  }

  findUnassigned(storeId: string, date: string): Observable<UnassignedShiftResponse[]> {
    const params = new HttpParams().set('date', date);
    return this.http.get<UnassignedShiftResponse[]>(
      `${this.storeShiftsUrl(storeId)}/unassigned`,
      { params }
    );
  }

  findWeeklyCoverage(storeId: string, date: string): Observable<ShiftCoverageSlot[]> {
    const params = new HttpParams().set('date', date);
    return this.http.get<ShiftCoverageSlot[]>(
      `${this.storeShiftsUrl(storeId)}/coverage`,
      { params }
    );
  }

  create(storeId: string, request: ShiftCreateRequest): Observable<ShiftResponse> {
    return this.http.post<ShiftResponse>(this.storeShiftsUrl(storeId), request);
  }

  update(storeId: string, shiftId: string, request: ShiftUpdateRequest): Observable<ShiftResponse> {
    return this.http.put<ShiftResponse>(`${this.storeShiftsUrl(storeId)}/${shiftId}`, request);
  }

  delete(storeId: string, shiftId: string): Observable<void> {
    return this.http.delete<void>(`${this.storeShiftsUrl(storeId)}/${shiftId}`);
  }
}
