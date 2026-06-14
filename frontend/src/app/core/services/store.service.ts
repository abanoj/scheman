import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page, PageParams } from '../models/page.models';
import {
  StoreCreateRequest,
  StoreUpdateRequest,
  StoreListResponse,
  StoreResponse,
} from '../models/store.models';

@Injectable({ providedIn: 'root' })
export class StoreService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/stores`;

  findAll(params?: PageParams): Observable<Page<StoreListResponse>> {
    let p = new HttpParams();
    if (params?.page !== undefined) p = p.set('page', params.page);
    if (params?.size !== undefined) p = p.set('size', params.size);
    return this.http.get<Page<StoreListResponse>>(this.base, { params: p });
  }

  findById(id: string): Observable<StoreResponse> {
    return this.http.get<StoreResponse>(`${this.base}/${id}`);
  }

  create(request: StoreCreateRequest): Observable<StoreResponse> {
    return this.http.post<StoreResponse>(this.base, request);
  }

  update(id: string, request: StoreUpdateRequest): Observable<StoreResponse> {
    return this.http.patch<StoreResponse>(`${this.base}/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
