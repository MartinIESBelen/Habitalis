import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ContratoService } from '../../../core/services/contrato/contrato.service';

@Component({
  selector: 'app-reserva-manual',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './reserva-manual.html'
})
export class ReservaManualComponent implements OnInit {
  apartamentoId!: number;
  cargando = false;
  mensajeError = '';

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private contratoService = inject(ContratoService);
  private fb = inject(FormBuilder);

  // Formulario Reactivo
  formContrato: FormGroup = this.fb.group({
    fechaEntrada: ['', Validators.required],
    fechaSalida: ['', Validators.required],
    precioBaseAlquiler: [null, [Validators.required, Validators.min(1)]],
    fianza: [null, [Validators.required, Validators.min(0)]],
    nombreInquilino: ['', [Validators.required, Validators.minLength(2), Validators.pattern('^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$')]],
    apellidosInquilino: ['', [Validators.required, Validators.minLength(2), Validators.pattern('^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$')]],
    dniInquilino: ['', [Validators.required, Validators.pattern('^[A-Za-z0-9]{5,20}$')]],
    fechaNacimientoInquilino: ['', Validators.required],
    emailInquilino: ['', [Validators.required, Validators.email]],
    telefonoInquilino: [''],
    // Opción para avisar al inquilino (puedes gestionarlo en el backend o dejarlo visual por ahora)
    notificarInquilino: [true]
  });

  ngOnInit() {
    this.apartamentoId = Number(this.route.snapshot.paramMap.get('id'));
  }

  // Helper para errores
  isInvalid(controlName: string) {
    const control = this.formContrato.get(controlName);
    return control?.invalid && control?.touched;
  }

  guardar() {
    if (this.formContrato.invalid) {
      this.formContrato.markAllAsTouched();
      return;
    }

    this.cargando = true;
    this.mensajeError = '';

    const requestData = this.formContrato.value;

    this.contratoService.crearContratoManual(this.apartamentoId, requestData).subscribe({
      next: () => {
        alert('¡Contrato registrado y cuenta de inquilino creada correctamente!');
        this.router.navigate(['/apartamento', this.apartamentoId]);
      },
      error: (err) => {
        this.cargando = false;
        this.mensajeError = err.error?.message || 'No se pudo crear el registro. Verifica los datos.';
        console.error(err);
      }
    });
  }
}
