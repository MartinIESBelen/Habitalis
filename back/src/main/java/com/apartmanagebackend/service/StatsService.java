package com.apartmanagebackend.service;

import com.apartmanagebackend.domain.Transaccion;
import com.apartmanagebackend.domain.Usuario;
import com.apartmanagebackend.domain.enums.EstadoTransaccion;
import com.apartmanagebackend.domain.enums.EstadoIncidencia;
import com.apartmanagebackend.domain.enums.EstadoContrato;
import com.apartmanagebackend.domain.enums.TipoTransaccion;
import com.apartmanagebackend.dto.stats.DashboardStatsResponse;
import com.apartmanagebackend.dto.stats.FinanzasMesResponse;
import com.apartmanagebackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final ApartamentoRepository apartamentoRepository;
    private final TransaccionRepository transaccionRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ContratoRepository contratoRepository;

    public DashboardStatsResponse obtenerResumen(String emailPropietario) {
        Usuario prop = usuarioRepository.findByEmail(emailPropietario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Long idPropietario = prop.getId();
        LocalDate hoy = LocalDate.now();

        // Total de apartamentos del propietario
        long totalAptos = apartamentoRepository.findByPropietarioId(idPropietario).size();

        // Cálculo de apartamentos OCUPADOS actualmente
        long ocupados = contratoRepository.findAll().stream()
                .filter(r -> r.getApartamento().getPropietario().getId().equals(idPropietario))
                .filter(r -> r.getEstado() == EstadoContrato.CONFIRMADA)
                .filter(r -> !hoy.isBefore(r.getFechaEntrada()) && !hoy.isAfter(r.getFechaSalida()))
                .map(r -> r.getApartamento().getId())
                .distinct()
                .count();

        // Ingresos del mes actual (Solo sumamos transacciones tipo INGRESO y estado PAGADO)
        BigDecimal ingresos = transaccionRepository.findAll().stream()
                .filter(t -> t.getApartamento().getPropietario().getId().equals(idPropietario))
                .filter(t -> t.getTipo() == TipoTransaccion.INGRESO)
                .filter(t -> t.getEstado() == EstadoTransaccion.PAGADO)
                .filter(t -> t.getFechaEmision().getMonthValue() == hoy.getMonthValue() && t.getFechaEmision().getYear() == hoy.getYear())
                .map(Transaccion::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Deuda pendiente total (Ingresos que no se han pagado, PENDIENTE o VENCIDO)
        BigDecimal deuda = transaccionRepository.findAll().stream()
                .filter(t -> t.getApartamento().getPropietario().getId().equals(idPropietario))
                .filter(t -> t.getTipo() == TipoTransaccion.INGRESO)
                .filter(t -> t.getEstado() == EstadoTransaccion.PENDIENTE || t.getEstado() == EstadoTransaccion.VENCIDO)
                .map(Transaccion::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Incidencias abiertas
        long incidencias = incidenciaRepository.findAll().stream()
                .filter(i -> i.getApartamento().getPropietario().getId().equals(idPropietario))
                .filter(i -> i.getEstado() != EstadoIncidencia.SOLUCIONADA)
                .count();

        return new DashboardStatsResponse(totalAptos, ocupados, ingresos, deuda, incidencias);
    }

    public List<FinanzasMesResponse> obtenerBalanceConsolidado(String emailPropietario, Integer anio) {
        Usuario prop = usuarioRepository.findByEmail(emailPropietario).orElseThrow();

        // 1. Obtenemos de golpe TODAS las transacciones del propietario de ese año
        List<Transaccion> transaccionesAnio = transaccionRepository.findAll().stream()
                .filter(t -> t.getApartamento().getPropietario().getId().equals(prop.getId()))
                .filter(t -> t.getFechaEmision().getYear() == anio)
                .toList();

        // Agrupar por mes (Enero a Diciembre) y devolver el DTO
        String[] nombresMeses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        List<FinanzasMesResponse> balanceAnual = new java.util.ArrayList<>();

        // Recorremos los 12 meses del año
        for (int i = 1; i <= 12; i++) {
            final int mesActual = i;

            // Sumamos los INGRESOS PAGADOS de este mes
            BigDecimal ingresosMes = transaccionesAnio.stream()
                    .filter(t -> t.getTipo() == TipoTransaccion.INGRESO)
                    .filter(t -> t.getEstado() == EstadoTransaccion.PAGADO)
                    .filter(t -> t.getFechaEmision().getMonthValue() == mesActual)
                    .map(Transaccion::getImporte)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Sumamos los GASTOS de este mes (Aquí podemos sumar todos, pagados o no, para saber el gasto incurrido)
            BigDecimal gastosMes = transaccionesAnio.stream()
                    .filter(t -> t.getTipo() == TipoTransaccion.GASTO)
                    .filter(t -> t.getFechaEmision().getMonthValue() == mesActual)
                    .map(Transaccion::getImporte)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Creamos el objeto del mes y lo metemos en la lista
            balanceAnual.add(new FinanzasMesResponse(nombresMeses[i - 1], ingresosMes, gastosMes));
        }

        return balanceAnual;
    }
}