export interface User {
  id: string;
  username: string;
  email: string;
  password?: string;
  token?: string;
}

export interface LoginResponse {
  token: string;
  id: string;
  username: string;
  email: string;
  type: string;
  success: boolean;
  message: string;
}

export interface LoginRequest {
  loginId: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
}
