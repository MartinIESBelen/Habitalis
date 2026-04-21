package com.apartmanagebackend.service;

import com.apartmanagebackend.domain.*;
import com.apartmanagebackend.domain.enums.EstadoReserva;
import com.apartmanagebackend.dto.reserva.ReservaManualRequest;
import com.apartmanagebackend.dto.reserva.ReservaRequest;
import com.apartmanagebackend.dto.reserva.ReservaResponse;
import com.apartmanagebackend.dto.reserva.VincularRequest;
import com.apartmanagebackend.repository.ApartamentoRepository;
import com.apartmanagebackend.repository.ReservaRepository;
import com.apartmanagebackend.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservaService {
    private final ReservaRepository reservaRepository;
    private final ApartamentoRepository apartamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // EL PROPIETARIO CREA LA RESERVA Y GENERA EL CÓDIGO
    public ReservaResponse crearReserva(Long apartamentoId, ReservaRequest request, String emailPropietario) {

        if (request.fechaSalida().isBefore(request.fechaEntrada()) || request.fechaSalida().isEqual(request.fechaEntrada())) {
            throw new RuntimeException("La fecha de salida debe ser estrictamente posterior a la de entrada.");
        }

        Propietario propietario = (Propietario) usuarioRepository.findByEmail(emailPropietario).orElseThrow();

        Apartamento apartamento = apartamentoRepository.findById(apartamentoId)
                .filter(apt -> apt.getPropietario().getId().equals(propietario.getId()))
                .orElseThrow(() -> new RuntimeException("Apartamento no encontrado o sin permisos"));

        // Generamos un código único de 8 caracteres en mayúsculas
        String codigoGenerado = "APT-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        Reserva nuevaReserva = Reserva.builder()
                .apartamento(apartamento)
                .codigoVinculacion(codigoGenerado)
                .fechaEntrada(request.fechaEntrada())
                .fechaSalida(request.fechaSalida())
                .precioBaseAlquiler(request.precioBaseAlquiler())
                .estado(EstadoReserva.PENDIENTE) // Esperando a que un inquilino use el código
                .build();

        return mapToResponse(reservaRepository.save(nuevaReserva));
    }

    // EL PROPIETARIO CREA UN CONTRATO MANUAL CON UN INQUILINO FANTASMA
    @Transactional
    public ReservaResponse crearReservaManual(Long apartamentoId, ReservaManualRequest request, String emailPropietario) {

        if (request.fechaSalida().isBefore(request.fechaEntrada()) || request.fechaSalida().isEqual(request.fechaEntrada())) {
            throw new RuntimeException("La fecha de salida debe ser estrictamente posterior a la de entrada.");
        }

        Propietario propietario = (Propietario) usuarioRepository.findByEmail(emailPropietario)
                .orElseThrow(() -> new RuntimeException("Propietario no encontrado"));

        Apartamento apartamento = apartamentoRepository.findById(apartamentoId)
                .filter(apt -> apt.getPropietario().getId().equals(propietario.getId()))
                .orElseThrow(() -> new RuntimeException("Apartamento no encontrado o sin permisos"));

        // BUSCAR O CREAR AL INQUILINO FANTASMA
        Usuario inquilino = usuarioRepository.findByEmail(request.emailInquilino()).orElseGet(() -> {
            // Si no existe, creamos el "Shadow Account"
            String contraseñaFantasma = UUID.randomUUID().toString(); // Imposible de adivinar

            Inquilino nuevoInquilino = new Inquilino();
            nuevoInquilino.setNombre(request.nombreInquilino());

            nuevoInquilino.setEmail(request.emailInquilino());
            nuevoInquilino.setPassword(passwordEncoder.encode(contraseñaFantasma));
            nuevoInquilino.setTelefono(request.telefonoInquilino());
            // Nota: Aquí podrías añadir un campo en tu entidad Usuario como 'boolean cuentaActiva = false' si lo deseas.

            return usuarioRepository.save(nuevoInquilino);
        });

        // CREAMOS LA RESERVA YA CONFIRMADA (No necesita código)
        String codigoInterno = "MANUAL-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        Reserva nuevaReserva = Reserva.builder()
                .apartamento(apartamento)
                .inquilino((Inquilino) inquilino)
                .codigoVinculacion(codigoInterno)
                .fechaEntrada(request.fechaEntrada())
                .fechaSalida(request.fechaSalida())
                .precioBaseAlquiler(request.precioBaseAlquiler())
                .estado(EstadoReserva.CONFIRMADA)
                .build();

        return mapToResponse(reservaRepository.save(nuevaReserva));
    }

    //EL INQUILINO INTRODUCE EL CÓDIGO Y SE VINCULA
    public ReservaResponse vincularInquilino(VincularRequest request, String emailInquilino) {
        Inquilino inquilino = (Inquilino) usuarioRepository.findByEmail(emailInquilino)
                .orElseThrow(() -> new RuntimeException("Solo los inquilinos pueden vincularse"));

        // Buscamos la reserva por el código secreto
        Reserva reserva = reservaRepository.findByCodigoVinculacion(request.codigoVinculacion())
                .orElseThrow(() -> new RuntimeException("Código de vinculación inválido o no existe"));

        // Verificamos que la reserva esté libre (PENDIENTE)
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new RuntimeException("Este código ya ha sido usado o la reserva no está disponible");
        }

        reserva.setInquilino(inquilino);
        reserva.setEstado(EstadoReserva.CONFIRMADA);

        return mapToResponse(reservaRepository.save(reserva));
    }

    // OBTENER LA LISTA DE RESERVAS DE UN APARTAMENTO (Para el propietario)
    public List<ReservaResponse> listarReservasPorApartamento(Long apartamentoId, String emailPropietario) {
        // Comprobamos si es realmente el propietario del piso
        Propietario propietario = (Propietario) usuarioRepository.findByEmail(emailPropietario).orElseThrow();

        boolean esSuPiso = apartamentoRepository.findById(apartamentoId)
                .map(apt -> apt.getPropietario().getId().equals(propietario.getId()))
                .orElse(false);

        if (!esSuPiso) {
            throw new RuntimeException("No tienes permisos para ver las reservas de este apartamento");
        }

        return reservaRepository.findByApartamentoId(apartamentoId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ReservaResponse mapToResponse(Reserva reserva) {
        return new ReservaResponse(
                reserva.getId(),
                reserva.getCodigoVinculacion(),
                reserva.getFechaEntrada(),
                reserva.getFechaSalida(),
                reserva.getPrecioBaseAlquiler(),
                reserva.getEstado(),
                reserva.getApartamento().getNombreInterno(),
                reserva.getInquilino() != null ? reserva.getInquilino().getNombre() : "Sin asignar"
        );
    }
}
