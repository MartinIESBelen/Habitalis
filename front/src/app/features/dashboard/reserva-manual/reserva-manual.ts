import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms'; // Añadido NgForm
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ReservaService } from '../../../core/services/reserva/reserva';

@Component({
  selector: 'app-reserva-manual',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './reserva-manual.html'
})
export class ReservaManualComponent implements OnInit {
  apartamentoId!: number;
  cargando = false;

  // Modelo actualizado: Añadidos apellidos, DNI y Fecha de Nacimiento
  form = {
    fechaEntrada: '',
    fechaSalida: '',
    precioBaseAlquiler: 0,
    nombreInquilino: '',
    apellidosInquilino: '',
    dniInquilino: '',
    fechaNacimientoInquilino: '',
    emailInquilino: '',
    telefonoInquilino: ''
  };

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private reservaService = inject(ReservaService);

  ngOnInit() {
    this.apartamentoId = Number(this.route.snapshot.paramMap.get('id'));
  }

  // Ahora recibimos el formulario para comprobar si es válido
  guardar(reservaForm: NgForm) {
    if (reservaForm.invalid) return;

    this.cargando = true;
    this.reservaService.crearReservaManual(this.apartamentoId, this.form).subscribe({
      next: () => {
        alert('¡Contrato e Inquilino Fantasma registrados correctamente!');
        // Solucionado el warning de la promesa con "void"
        void this.router.navigate(['/apartamento', this.apartamentoId]);
      },
      error: (err) => {
        this.cargando = false;
        alert('Error: ' + (err.error?.message || 'No se pudo crear el registro'));
      }
    });
  }
}
