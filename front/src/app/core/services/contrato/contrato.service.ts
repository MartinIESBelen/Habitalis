import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ContratoLista, ContratoDetalle, InquilinoPublico } from '../../models/contrato.model';
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class ContratoService {
  private http = inject(HttpClient);

  private apiUrl = 'http://localhost:8080/api/v1/contratos';

  getMisContratos(): Observable<ContratoLista[]> {
    return this.http.get<ContratoLista[]>(this.apiUrl);
  }

  getDetalleContrato(id: number): Observable<ContratoDetalle> {
    return this.http.get<ContratoDetalle>(`${this.apiUrl}/${id}`);
  }

  vincularCodigo(codigo: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/vincular`, { codigoVinculacion: codigo });
  }

  crearContrato(apartamentoId: number, contratoData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/apartamentos/${apartamentoId}`, contratoData);
  }

  crearContratoManual(apartamentoId: number, contratoManualData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/apartamentos/${apartamentoId}/manual`, contratoManualData);
  }

  subirContratoPdf(contratoId: number, archivo: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', archivo);
    return this.http.post(`${this.apiUrl}/${contratoId}/contrato`, formData, { responseType: 'text' });
  }

  borrarContrato(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  borrarContratoPdf(contratoId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${contratoId}/contrato`);
  }
}
