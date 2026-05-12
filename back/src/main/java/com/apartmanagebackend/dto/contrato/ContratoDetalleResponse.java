package com.apartmanagebackend.dto.contrato;

import com.apartmanagebackend.domain.enums.EstadoContrato;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ContratoDetalleResponse(
        Long id,
        String codigoVinculacion,
        String nombreApartamento,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        BigDecimal precioBaseAlquiler,
        BigDecimal fianza,
        EstadoContrato estado,
        LocalDateTime creadoEn,
        String contratoPdf,
        InquilinoPublico inquilino
) {
    public record InquilinoPublico(
            Long id,
            String nombre,
            String apellidos,
            String email,
            String telefono
    ) {}
}