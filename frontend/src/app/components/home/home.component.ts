import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { PostService } from '../../services/post.service';
import { Post, PostComment } from '../../models/post.model';
import { User } from '../../models/user.model';
import { CommentComponent } from '../comment/comment.component';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  standalone: true,
  imports: [CommonModule, CommentComponent, RouterLink, RouterModule]
})
export class HomeComponent implements OnInit {
  posts: Post[] = [];
  isLoading = true;
  currentUser: User | null = null;
  currentPage = 0;
  totalPages = 0;
  pageSize = 3;
  postsWithComments = new Map<number, PostComment[]>();

  constructor(
    private postService: PostService,
    private authService: AuthService,
    private router: Router
  ) {}

  get isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  ngOnInit(): void {
    this.loadPosts();
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
  }

  loadPosts(): void {
    this.isLoading = true;
    this.postService.getPosts(this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        this.posts = response.content;
        this.totalPages = response.totalPages;
        this.isLoading = false;
        this.posts.forEach(post => this.loadComments(post.id));
      },
      error: (error) => {
        console.error('Failed to load posts:', error);
        this.isLoading = false;
      }
    });
  }

  loadComments(postId: number): void {
    this.postService.getComments(postId).subscribe({
      next: (comments) => {
        this.postsWithComments.set(postId, comments);
      },
      error: (error) => {
        console.error(`Failed to load comments for post ${postId}:`, error);
      }
    });
  }

  getCommentsForPost(postId: number): PostComment[] {
    return this.postsWithComments.get(postId) ?? [];
  }

  handleAddComment(event: {postId: number; content: string; parentId?: number}): void {
    if (!this.currentUser) {
      this.router.navigate(['/login']);
      return;
    }

    this.postService.addComment(event.postId, event.content, event.parentId).subscribe({
      next: () => {
        this.loadComments(event.postId);
      },
      error: (error) => {
        console.error('Failed to add comment:', error);
        alert('Failed to add comment. Please try again.');
      }
    });
  }

  loadNextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadPosts();
    }
  }

  loadPreviousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadPosts();
    }
  }
}
