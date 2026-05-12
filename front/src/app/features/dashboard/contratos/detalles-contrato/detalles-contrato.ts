import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ContratoService } from '../../../../core/services/contrato/contrato.service';
import { ContratoDetalle } from '../../../../core/models/contrato.model';

@Component({
  selector: 'app-detalles-contrato',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './detalles-contrato.html'
})
export class DetallesContrato implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private contratoService = inject(ContratoService);

  // Estados
  contrato = signal<ContratoDetalle | null>(null);
  cargando = signal<boolean>(true);
  error = signal<string>('');
  subiendo = signal<boolean>(false);

  ngOnInit() {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.cargarDetalle(+idParam);
    } else {
      void this.router.navigate(['/contratos']);
    }
  }

  cargarDetalle(id: number) {
    this.cargando.set(true);
    this.contratoService.getDetalleContrato(id).subscribe({
      next: (data) => {
        this.contrato.set(data);
        this.cargando.set(false);
      },
      error: (err) => {
        console.error(err);
        this.error.set('No se pudo cargar la información del contrato.');
        this.cargando.set(false);
      }
    });
  }

  onFileSelected(event: Event) {
    const element = event.currentTarget as HTMLInputElement;
    const file = element.files?.[0];
    const currentContrato = this.contrato();

    if (file && currentContrato) {
      this.subiendo.set(true);

      this.contratoService.subirContratoPdf(currentContrato.id, file).subscribe({
        next: () => {
          alert('¡Contrato subido con éxito!');
          this.subiendo.set(false);
          this.cargarDetalle(currentContrato.id); // Recargamos para ver el archivo
        },
        error: (err) => {
          alert('Error al subir el archivo: ' + (err.error || 'Inténtalo de nuevo'));
          this.subiendo.set(false);
        }
      });
    }
  }

  romperContrato() {
    const currentContrato = this.contrato();
    if (!currentContrato) return;

    const confirmacion = window.confirm(
      '⚠️ ¿Estás totalmente seguro de que quieres romper este contrato?\n\n' +
      'Esta acción eliminará el contrato, desvinculará al inquilino de la vivienda y le bloqueará el acceso a la plataforma. No se puede deshacer.'
    );

    if (confirmacion) {
      this.contratoService.borrarContrato(currentContrato.id).subscribe({
        next: () => {
          alert('El contrato ha sido eliminado y el inquilino desvinculado.');
          this.router.navigate(['/contratos']);
        },
        error: (err) => {
          alert('Hubo un problema al romper el contrato.');
        }
      });
    }
  }
  borrarDocumento() {
    const currentContrato = this.contrato();
    if (!currentContrato) return;

    if (window.confirm('¿Seguro que quieres borrar este documento?')) {
      this.cargando.set(true);

      this.contratoService.borrarContratoPdf(currentContrato.id).subscribe({
        next: () => {
          alert('Documento borrado con éxito.');
          this.cargarDetalle(currentContrato.id);
        },
        error: (err) => {
          alert('No se pudo borrar el documento.');
          this.cargando.set(false);
        }
      });
    }
  }
}
