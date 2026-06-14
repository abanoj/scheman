export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  access_token: string;
  refresh_token: string;
  mustChangePassword: boolean;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
  repeatNewPassword: string;
}

export interface ManagerCreateRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export interface ManagerUpdateRequest {
  firstName: string;
  lastName: string;
}

export interface ManagerResponse {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  enabled: boolean;
}

export type Role = 'ADMIN' | 'MANAGER' | 'EMPLOYEE';

export interface JwtPayload {
  sub: string;
  id: string;
  role: Role;
  exp: number;
  iat: number;
}

export interface CurrentUser {
  id: string;
  email: string;
  role: Role;
  mustChangePassword: boolean;
}
