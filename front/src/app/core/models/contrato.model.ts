export interface ContratoLista {
  id: number;
  codigoVinculacion: string;
  fechaEntrada: string;
  fechaSalida: string;
  precioBaseAlquiler: number;
  estado: string;
  nombreApartamento: string;
  nombreInquilino: string;
}

export interface InquilinoPublico {
  id: number;
  nombre: string;
  apellidos: string;
  email: string;
  telefono?: string;
}

export interface ContratoDetalle {
  id: number;
  codigoVinculacion: string;
  nombreApartamento: string;
  fechaEntrada: string;
  fechaSalida: string;
  precioBaseAlquiler: number;
  fianza: number;
  estado: string;
  creadoEn: string;
  contratoPdf?: string | null;
  inquilino: InquilinoPublico | null;
}
