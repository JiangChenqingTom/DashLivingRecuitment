import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import {
  User,
  LoginRequest,
  RegisterRequest,
  LoginResponse,
} from '../models/user.model';
import { catchError, map } from 'rxjs/operators';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    this.initializeUserFromStorage();
  }

  private initializeUserFromStorage(): void {
    const storedUser = sessionStorage.getItem('currentUser');
    if (storedUser && storedUser !== 'undefined' && storedUser !== 'null') {
      const user = JSON.parse(storedUser);
      this.currentUserSubject.next(user);
    }
  }

  login(credentials: LoginRequest): Observable<User> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });

    const requestBody = {
      username: credentials.loginId,
      password: credentials.password,
    };

    return this.http
      .post<LoginResponse>(
        'http://localhost:8080/api/auth/login',
        requestBody,
        {
          headers: headers,
          withCredentials: true,
        }
      )
      .pipe(
        map((response) => {
          const userToStore = {
            id: response.id,
            username: response.username,
            email: response.email,
            token: response.token  // 确保token也保存在用户对象中
          };
          // 保存用户信息（包含token）
          sessionStorage.setItem('currentUser', JSON.stringify(userToStore));
          this.currentUserSubject.next(userToStore);
          return userToStore;
        }),
        catchError((error) => {
          const statusCode = error.status;
          const errorDetails =
            error.error?.message || error.message || '登录失败';
          const errorMessage = `[${statusCode}] ${errorDetails}`;
          return throwError(() => new Error(errorMessage));
        })
      );
  }

  register(userData: RegisterRequest): Observable<User> {
    // 1. 登录ID验证
    if (!userData.username.trim()) {
      return throwError(() => new Error('登录ID不能为空'));
    }
    if (!/^[a-zA-Z0-9]{5,20}$/.test(userData.username)) {
      return throwError(
        () => new Error('登录ID只能包含字母和数字，长度5-20个字符')
      );
    }

    // 2. 密码验证
    if (!userData.password) {
      return throwError(() => new Error('密码不能为空'));
    }
    if (
      !/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,20}$/.test(
        userData.password
      )
    ) {
      return throwError(
        () => new Error('密码需8-20位，包含大小写字母、数字和特殊符号(@$!%*?&)')
      );
    }
    if (userData.password !== userData.confirmPassword) {
      return throwError(() => new Error('两次输入的密码不一致'));
    }

    // 3. 邮箱验证
    if (!userData.email.trim()) {
      return throwError(() => new Error('邮箱不能为空'));
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(userData.email)) {
      return throwError(() => new Error('邮箱格式不正确'));
    }

    // 配置请求头（与登录保持一致）
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });

    // 构造注册请求体
    const requestBody = {
      username: userData.username,
      password: userData.password,
      email: userData.email,
    };

    return this.http
      .post<User>('http://localhost:8080/api/auth/register', requestBody, {
        headers: headers, // 添加请求头
        withCredentials: true, // 跨域请求携带凭据（修复注册403问题）
      })
      .pipe(
        map((user) => {
          const userToStore = { ...user };
          delete userToStore.password;
          return userToStore;
        }),
        catchError((error) => {
          const statusCode = error.status;
          const errorDetails =
            error.error?.message || error.message || '注册失败';
          const errorMessage = `[${statusCode}] ${errorDetails}`;
          return throwError(() => new Error(errorMessage));
        })
      );
  }

  logout(): void {
    // 发送登出请求到后端
    this.http.post(
      'http://localhost:8080/api/auth/logout',
      {},
      { withCredentials: true }
    ).subscribe({
      next: () => {
        sessionStorage.removeItem('currentUser');
        this.currentUserSubject.next(null);
      },
      error: (error) => {
        console.error('Logout failed:', error);
        // 即使请求失败也清除本地状态
        sessionStorage.removeItem('currentUser');
        this.currentUserSubject.next(null);
      }
    });
  }

  isLoggedIn(): boolean {
    return !!sessionStorage.getItem('currentUser');
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }
}
