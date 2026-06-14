import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page, PageParams } from '../models/page.models';
import {
  EmployeeCreateRequest,
  EmployeeUpdateRequest,
  EmployeeResponse,
} from '../models/employee.models';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/employees`;

  findAll(params?: PageParams): Observable<Page<EmployeeResponse>> {
    const httpParams = toHttpParams(params);
    return this.http.get<Page<EmployeeResponse>>(this.base, { params: httpParams });
  }

  findById(id: string): Observable<EmployeeResponse> {
    return this.http.get<EmployeeResponse>(`${this.base}/${id}`);
  }

  getMyProfile(): Observable<EmployeeResponse> {
    return this.http.get<EmployeeResponse>(`${this.base}/me`);
  }

  create(request: EmployeeCreateRequest): Observable<EmployeeResponse> {
    return this.http.post<EmployeeResponse>(this.base, request);
  }

  update(id: string, request: EmployeeUpdateRequest): Observable<EmployeeResponse> {
    return this.http.patch<EmployeeResponse>(`${this.base}/${id}`, request);
  }

  enable(id: string): Observable<void> {
    return this.http.patch<void>(`${this.base}/${id}/enable`, {});
  }

  disable(id: string): Observable<void> {
    return this.http.patch<void>(`${this.base}/${id}/disable`, {});
  }
}

function toHttpParams(params?: PageParams): HttpParams {
  let p = new HttpParams();
  if (!params) return p;
  if (params.page !== undefined) p = p.set('page', params.page);
  if (params.size !== undefined) p = p.set('size', params.size);
  if (params.sort) p = p.set('sort', params.sort);
  return p;
}
