package com.apartmanagebackend.dto.reserva;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservaManualRequest(
        @NotNull LocalDate fechaEntrada,
        @NotNull LocalDate fechaSalida,
        @NotNull @Positive BigDecimal precioBaseAlquiler,

        // Datos del Inquilino Fantasma
        @NotBlank String nombreInquilino,
        @NotBlank String apellidoInquilino,
        @NotBlank String DNIInquilino,
        @NotBlank @Email String emailInquilino,
        String telefonoInquilino
) {
}
