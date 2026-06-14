import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page, PageParams } from '../models/page.models';
import { ManagerCreateRequest, ManagerUpdateRequest, ManagerResponse } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class ManagerService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/managers`;
  private readonly authBase = `${environment.apiUrl}/auth`;

  findAll(params?: PageParams): Observable<Page<ManagerResponse>> {
    let p = new HttpParams();
    if (params?.page !== undefined) p = p.set('page', params.page);
    if (params?.size !== undefined) p = p.set('size', params.size);
    return this.http.get<Page<ManagerResponse>>(this.base, { params: p });
  }

  findById(id: string): Observable<ManagerResponse> {
    return this.http.get<ManagerResponse>(`${this.base}/${id}`);
  }

  create(request: ManagerCreateRequest): Observable<ManagerResponse> {
    return this.http.post<ManagerResponse>(`${this.authBase}/signup/manager`, request);
  }

  update(id: string, request: ManagerUpdateRequest): Observable<ManagerResponse> {
    return this.http.patch<ManagerResponse>(`${this.base}/${id}`, request);
  }

  enable(id: string): Observable<void> {
    return this.http.patch<void>(`${this.base}/${id}/enable`, {});
  }

  disable(id: string): Observable<void> {
    return this.http.patch<void>(`${this.base}/${id}/disable`, {});
  }
}
