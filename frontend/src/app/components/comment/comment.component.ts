import { Component, Input, Output, EventEmitter } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { PostComment } from '../../models/post.model';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-comment',
    templateUrl: './comment.component.html',
    styleUrls: ['./comment.component.scss'],
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterLink]
})
export class CommentComponent {
    @Input() comments: PostComment[] = [];
    @Input() postId!: number;
    @Output() onAddComment = new EventEmitter<{postId: number, content: string, parentId?: number}>();

    commentContent = new FormControl('');
    replyingTo: number | null = null;

    constructor(private authService: AuthService) {
        this.commentContent = new FormControl('');
    }

    get isLoggedIn(): boolean {
        return this.authService.isLoggedIn();
    }

    submitComment(parentId?: number) {
        if (this.commentContent.value?.trim() && this.isLoggedIn) {
            this.onAddComment.emit({
                postId: this.postId,
                content: this.commentContent.value,
                parentId: parentId !== undefined ? parentId : this.replyingTo || undefined
            });
            this.commentContent.reset();
            this.replyingTo = null;
        }
    }

    startReply(commentId: number) {
        this.replyingTo = commentId;
    }

    cancelReply() {
        this.replyingTo = null;
        this.commentContent.reset();
    }
}
