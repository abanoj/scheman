import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page, PageParams } from '../models/page.models';
import {
  ShiftAssignmentCreateRequest,
  ShiftAssignmentUpdateRequest,
  ShiftAssignmentResponse,
  WeeklyAssignmentResponse,
} from '../models/shift-assignment.models';
import { EmployeeWeeklyAvailability } from '../models/employee.models';

@Injectable({ providedIn: 'root' })
export class ShiftAssignmentService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  private shiftAssignmentsUrl(shiftId: string): string {
    return `${this.apiUrl}/shifts/${shiftId}/shift-assignments`;
  }

  findAllByShift(shiftId: string, params?: PageParams): Observable<Page<ShiftAssignmentResponse>> {
    let p = new HttpParams();
    if (params?.page !== undefined) p = p.set('page', params.page);
    if (params?.size !== undefined) p = p.set('size', params.size);
    return this.http.get<Page<ShiftAssignmentResponse>>(this.shiftAssignmentsUrl(shiftId), {
      params: p,
    });
  }

  findById(shiftId: string, id: string): Observable<ShiftAssignmentResponse> {
    return this.http.get<ShiftAssignmentResponse>(`${this.shiftAssignmentsUrl(shiftId)}/${id}`);
  }

  findByEmployee(
    shiftId: string,
    employeeId: string,
    params?: PageParams
  ): Observable<Page<ShiftAssignmentResponse>> {
    let p = new HttpParams();
    if (params?.page !== undefined) p = p.set('page', params.page);
    return this.http.get<Page<ShiftAssignmentResponse>>(
      `${this.shiftAssignmentsUrl(shiftId)}/employee/${employeeId}`,
      { params: p }
    );
  }

  findWeeklyByEmployee(employeeId: string, date: string): Observable<WeeklyAssignmentResponse[]> {
    const params = new HttpParams().set('date', date);
    return this.http.get<WeeklyAssignmentResponse[]>(
      `${this.apiUrl}/shift-assignments/employees/${employeeId}/weekly`,
      { params }
    );
  }

  findWeeklyAvailability(date: string): Observable<EmployeeWeeklyAvailability[]> {
    const params = new HttpParams().set('date', date);
    return this.http.get<EmployeeWeeklyAvailability[]>(
      `${this.apiUrl}/shift-assignments/employees/weekly-availability`,
      { params }
    );
  }

  create(
    shiftId: string,
    request: ShiftAssignmentCreateRequest
  ): Observable<ShiftAssignmentResponse> {
    return this.http.post<ShiftAssignmentResponse>(this.shiftAssignmentsUrl(shiftId), request);
  }

  update(
    shiftId: string,
    id: string,
    request: ShiftAssignmentUpdateRequest
  ): Observable<ShiftAssignmentResponse> {
    return this.http.put<ShiftAssignmentResponse>(
      `${this.shiftAssignmentsUrl(shiftId)}/${id}`,
      request
    );
  }

  delete(shiftId: string, id: string): Observable<void> {
    return this.http.delete<void>(`${this.shiftAssignmentsUrl(shiftId)}/${id}`);
  }
}
