import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { catchError, finalize } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
})

export class RegisterComponent {
  registerForm: FormGroup;
  submitted = false;
  errorMessage = '';
  isLoading = false;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.formBuilder.group({
      username: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(20)]],
      confirmPassword: ['', Validators.required]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  // 密码匹配验证器
  passwordMatchValidator(form: FormGroup) {
    return form.get('password')?.value === form.get('confirmPassword')?.value 
      ? null 
      : { passwordMismatch: true };
  }

  onSubmit(): void {
    this.submitted = true;
    this.errorMessage = '';

    // 表单无效时不提交
    if (this.registerForm.invalid) {
      return;
    }

    this.isLoading = true;
    const userData = this.registerForm.value;

    this.authService.register(userData)
      .pipe(
        catchError(error => {
          this.errorMessage = error.message;
          return of(null);
        }),
        finalize(() => this.isLoading = false)
      )
      .subscribe(user => {
        if (user) {
          // 注册成功，跳转到登录页
          this.router.navigate(['/login']);
        }
      });
  }
}
    