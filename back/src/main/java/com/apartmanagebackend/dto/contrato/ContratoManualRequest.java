package com.apartmanagebackend.dto.contrato;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratoManualRequest(
        @NotNull LocalDate fechaEntrada,
        @NotNull LocalDate fechaSalida,
        @NotNull @Positive BigDecimal precioBaseAlquiler,

        BigDecimal fianza,
        Boolean notificarInquilino,

        @NotBlank String nombreInquilino,
        @NotBlank String apellidosInquilino,
        @NotBlank String dniInquilino,
        @NotNull LocalDate fechaNacimientoInquilino,
        @NotBlank @Email String emailInquilino,
        String telefonoInquilino
) {}