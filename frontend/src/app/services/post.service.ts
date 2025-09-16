import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Post, PostComment, PageResponse } from '../models/post.model';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';

export interface CreatePostRequest {
    title: string;
    content: string;
    authorId: number;
}

@Injectable({
    providedIn: 'root'
})
export class PostService {
    private baseUrl = 'http://localhost:8080/api';

    constructor(
        private http: HttpClient,
        private authService: AuthService,
        private router: Router
    ) { }

    private handleError(error: HttpErrorResponse) {
        if (error.status === 401) {
            this.router.navigate(['/login']);
            return throwError(() => new Error('Please login to perform this action.'));
        }
        return throwError(() => new Error('Something went wrong. Please try again later.'));
    }

    private getAuthHeaders(): HttpHeaders {
        const currentUserStr = sessionStorage.getItem('currentUser');
        if (!currentUserStr) {
            return new HttpHeaders({
                'Content-Type': 'application/json'
            });
        }

        try {
            const currentUser = JSON.parse(currentUserStr);
            return new HttpHeaders({
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentUser.token}`
            });
        } catch (error) {
            console.error('Error parsing currentUser:', error);
            return new HttpHeaders({
                'Content-Type': 'application/json'
            });
        }
    }

    getPosts(page: number = 0, size: number = 3): Observable<PageResponse<Post>> {
        return this.http.get<PageResponse<Post>>(`${this.baseUrl}/posts?page=${page}&size=${size}`, {
            withCredentials: true
        });
    }

    getComments(postId: number): Observable<PostComment[]> {
        return this.http.get<PostComment[]>(`${this.baseUrl}/posts/${postId}/comments`, {
            withCredentials: true
        });
    }

    addComment(postId: number, content: string, parentId?: number): Observable<PostComment> {
        if (!this.authService.isLoggedIn()) {
            this.router.navigate(['/login']);
            return throwError(() => new Error('Please login to comment.'));
        }

        const requestBody = parentId ? { content, parentId } : { content };

        return this.http.post<PostComment>(
            `${this.baseUrl}/posts/${postId}/comments`, 
            requestBody,
            { 
                headers: this.getAuthHeaders(),
                withCredentials: true 
            }
        ).pipe(
            catchError(this.handleError.bind(this))
        );
    }

    createPost(post: CreatePostRequest): Observable<Post> {
        if (!this.authService.isLoggedIn()) {
            this.router.navigate(['/login']);
            return throwError(() => new Error('Please login to create a post.'));
        }

        return this.http.post<Post>(
            `${this.baseUrl}/posts`,
            post,
            { 
                headers: this.getAuthHeaders(),
                withCredentials: true 
            }
        ).pipe(
            catchError(this.handleError.bind(this))
        );
    }
}
