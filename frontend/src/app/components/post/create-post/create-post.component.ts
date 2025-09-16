import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { PostService } from '../../../services/post.service';
import { AuthService } from '../../../services/auth.service';

@Component({
    selector: 'app-create-post',
    templateUrl: './create-post.component.html',
    styleUrls: ['./create-post.component.scss'],
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterLink]
})
export class CreatePostComponent implements OnInit {
    postForm: FormGroup;
    isLoading = false;
    errorMessage = '';
    
    ngOnInit(): void {
        if (!this.authService.isLoggedIn()) {
            this.router.navigate(['/login']);
        }
    }

    constructor(
        private formBuilder: FormBuilder,
        private postService: PostService,
        private router: Router,
        private authService: AuthService
    ) {
        this.postForm = this.formBuilder.group({
            title: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(100)]],
            content: ['', [Validators.required, Validators.minLength(10)]]
        });
    }

    onSubmit(): void {
        if (this.postForm.invalid) {
            return;
        }

        this.isLoading = true;
        this.errorMessage = '';

        const currentUser = this.authService.getCurrentUser();
        if (!currentUser) {
            this.router.navigate(['/login']);
            return;
        }

        const postData = {
            ...this.postForm.value,
            authorId: currentUser.id
        };
        
        this.postService.createPost(postData)
            .subscribe({
                next: () => {
                    this.router.navigate(['/']);
                },
                error: (error) => {
                    this.errorMessage = error.message;
                    this.isLoading = false;
                }
            });
    }
}
