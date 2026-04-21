import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms'; // Añadido NgForm
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth/auth.service';
import { RegisterRequest } from '../../../core/models/auth.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class RegisterComponent {

  userData: RegisterRequest = {
    nombre: '',
    apellidos: '',
    email: '',
    password: '',
    dniPasaporte: '',
    fechaNacimiento: '',
    rol: 'INQUILINO'
  };

  // Variable separada para la confirmación
  confirmarPassword: string = '';
  errorMessage: string = '';

  private authService = inject(AuthService);
  private router = inject(Router);

  // Comprueba si ambas contraseñas son idénticas
  contrasenasCoinciden(): boolean {
    return this.userData.password === this.confirmarPassword;
  }

  onSubmit(form: NgForm) {
    // Doble validación de seguridad antes de enviar
    if (form.invalid || !this.contrasenasCoinciden()) return;

    this.authService.register(this.userData).subscribe({
      next: (response) => {
        this.authService.guardarToken(response.token);
        this.errorMessage = '';
        void this.router.navigate(['/home']);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error al crear la cuenta. Revisa los datos.';
      }
    });
  }
}
