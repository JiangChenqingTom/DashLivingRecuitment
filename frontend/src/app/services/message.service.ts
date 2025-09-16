import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { Message } from '../models/message.model';
import { map, catchError } from 'rxjs/operators';
// 导入HttpHeaders用于配置请求头
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class MessageService {
  private apiUrl = 'http://localhost:8080/api/messages';

  constructor(private http: HttpClient) {}

  getMessages(): Observable<Message[]> {
    return this.http.get<{ success: boolean; data: Message[] }>(this.apiUrl).pipe(
      map(response => response.data),
      catchError(error => {
        const errorMessage = error.error?.message || '获取留言失败，请重试';
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  /**
   * 提交回复（修复Authorization头问题）
   */
  createReply(replyData: { content: string; parentId: number }): Observable<any> {
    const currentUser = localStorage.getItem('currentUser') || sessionStorage.getItem('currentUser');
    if (!currentUser) {
      return throwError(() => new Error('用户未登录'));
    }

    const token = JSON.parse(currentUser)?.token || '{}'; // 获取存储的token
    if (!token) {
      return throwError(() => new Error('用户未登录'));
    }

    // 2. 正确创建请求头（使用链式调用或重新赋值）
    const headers = new HttpHeaders()
      .set('Content-Type', 'application/json') // 添加必要的Content-Type头
      .set('Authorization', `Bearer ${token}`);

    return this.http.post(this.apiUrl, replyData, { headers });
  }
}
    