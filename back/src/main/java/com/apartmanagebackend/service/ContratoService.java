package com.apartmanagebackend.service;

import com.apartmanagebackend.domain.*;
import com.apartmanagebackend.domain.enums.EstadoContrato;
import com.apartmanagebackend.dto.contrato.*;
import com.apartmanagebackend.repository.ApartamentoRepository;
import com.apartmanagebackend.repository.ContratoRepository;
import com.apartmanagebackend.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final ApartamentoRepository apartamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RecuperacionPasswordService recuperacionService;
    private final AlmacenamientoService almacenamientoService;

    @Transactional
    public ContratoResponse crearContrato(Long apartamentoId, ContratoRequest request, String emailPropietario) {
        validarFechas(request.fechaEntrada(), request.fechaSalida());

        Apartamento apartamento = obtenerApartamentoVerificandoPropietario(apartamentoId, emailPropietario);
        String codigoGenerado = "APT-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        Contrato nuevoContrato = Contrato.builder()
                .apartamento(apartamento)
                .codigoVinculacion(codigoGenerado)
                .fechaEntrada(request.fechaEntrada())
                .fechaSalida(request.fechaSalida())
                .precioBaseAlquiler(request.precioBaseAlquiler())
                .fianza(request.fianza())
                .estado(EstadoContrato.PENDIENTE)
                .build();

        return mapToResponse(contratoRepository.save(nuevoContrato));
    }

    @Transactional
    public ContratoResponse crearContratoManual(Long apartamentoId, ContratoManualRequest request, String emailPropietario) {
        validarFechas(request.fechaEntrada(), request.fechaSalida());

        Apartamento apartamento = obtenerApartamentoVerificandoPropietario(apartamentoId, emailPropietario);

        Usuario inquilino = obtenerOCrearInquilinoFantasma(request);

        if (Boolean.TRUE.equals(request.notificarInquilino())) {
            try {
                recuperacionService.solicitarRecuperacion(inquilino.getEmail());
            } catch (Exception e) {
                System.err.println("Aviso: No se pudo notificar al inquilino por email: " + e.getMessage());
            }
        }

        String codigoInterno = "MANUAL-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        Contrato nuevoContrato = Contrato.builder()
                .apartamento(apartamento)
                .inquilino(inquilino)
                .codigoVinculacion(codigoInterno)
                .fechaEntrada(request.fechaEntrada())
                .fechaSalida(request.fechaSalida())
                .precioBaseAlquiler(request.precioBaseAlquiler())
                .fianza(request.fianza())
                .estado(EstadoContrato.CONFIRMADA)
                .build();

        return mapToResponse(contratoRepository.save(nuevoContrato));
    }

    @Transactional
    public ContratoResponse vincularInquilino(VincularRequest request, String emailInquilino) {
        Usuario inquilino = usuarioRepository.findByEmail(emailInquilino)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Contrato contrato = contratoRepository.findByCodigoVinculacion(request.codigoVinculacion())
                .orElseThrow(() -> new RuntimeException("Código de vinculación inválido o no existe"));

        if (contrato.getEstado() != EstadoContrato.PENDIENTE) {
            throw new RuntimeException("Este código ya ha sido usado o el contrato no está disponible");
        }

        contrato.setInquilino(inquilino);
        contrato.setEstado(EstadoContrato.CONFIRMADA);

        return mapToResponse(contratoRepository.save(contrato));
    }

    public List<ContratoResponse> listarContratosPorApartamento(Long apartamentoId, String emailPropietario) {
        obtenerApartamentoVerificandoPropietario(apartamentoId, emailPropietario);
        return contratoRepository.findByApartamentoId(apartamentoId).stream().map(this::mapToResponse).toList();
    }

    public List<ContratoResponse> obtenerMisContratosPropietario(String emailPropietario) {
        return contratoRepository.findMisContratosComoPropietario(emailPropietario).stream().map(this::mapToResponse).toList();
    }

    public ContratoDetalleResponse obtenerDetalleContrato(Long contratoId, String emailPropietario) {
        Contrato r = contratoRepository.findByIdAndPropietarioEmail(contratoId, emailPropietario)
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado o no tienes permisos"));
        return mapToDetalleResponse(r);
    }

    @Transactional
    public void subirContratoPdf(Long contratoId, MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("El archivo PDF está vacío.");

        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));

        String rutaRelativa = almacenamientoService.guardarDocumentoVivienda(contrato.getApartamento(), file);

        contrato.setContratoPdf(rutaRelativa);
        contratoRepository.save(contrato);
    }

    @Transactional
    public void eliminarContratoPdf(Long contratoId, String emailPropietario) {
        Contrato contrato = contratoRepository.findByIdAndPropietarioEmail(contratoId, emailPropietario)
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado o no tienes permisos"));

        if (contrato.getContratoPdf() != null && !contrato.getContratoPdf().isEmpty()) {
            try {
                almacenamientoService.eliminarArchivo(contrato.getContratoPdf());
            } catch (IOException e) {
                throw new RuntimeException("No se pudo borrar el archivo físico: " + e.getMessage());
            }
            contrato.setContratoPdf(null);
            contratoRepository.save(contrato);
        }
    }

    @Transactional
    public void eliminarContrato(Long contratoId, String emailPropietario) {
        Contrato contrato = contratoRepository.findByIdAndPropietarioEmail(contratoId, emailPropietario)
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado o no tienes permisos"));

        if (contrato.getContratoPdf() != null && !contrato.getContratoPdf().isEmpty()) {
            try {
                almacenamientoService.eliminarArchivo(contrato.getContratoPdf());
            } catch (IOException e) {
                System.err.println("Aviso: No se pudo borrar el archivo PDF físico: " + e.getMessage());
            }
        }
        contratoRepository.delete(contrato);
    }


    private void validarFechas(java.time.LocalDate entrada, java.time.LocalDate salida) {
        if (salida.isBefore(entrada) || salida.isEqual(entrada)) {
            throw new RuntimeException("La fecha de salida debe ser estrictamente posterior a la de entrada.");
        }
    }

    private Apartamento obtenerApartamentoVerificandoPropietario(Long apartamentoId, String emailPropietario) {
        Usuario propietario = usuarioRepository.findByEmail(emailPropietario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return apartamentoRepository.findById(apartamentoId)
                .filter(apt -> apt.getPropietario().getId().equals(propietario.getId()))
                .orElseThrow(() -> new RuntimeException("Apartamento no encontrado o sin permisos"));
    }

    private Usuario obtenerOCrearInquilinoFantasma(ContratoManualRequest request) {
        return usuarioRepository.findByEmail(request.emailInquilino()).orElseGet(() -> {
            Usuario nuevoInquilino = new Usuario();
            nuevoInquilino.setNombre(request.nombreInquilino());
            nuevoInquilino.setApellidos(request.apellidosInquilino());
            nuevoInquilino.setEmail(request.emailInquilino());
            nuevoInquilino.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            nuevoInquilino.setTelefono(request.telefonoInquilino());
            nuevoInquilino.setDniPasaporte(request.dniInquilino());
            nuevoInquilino.setFechaNacimiento(request.fechaNacimientoInquilino());
            nuevoInquilino.setRol(com.apartmanagebackend.domain.enums.RolUsuario.INQUILINO);
            return usuarioRepository.save(nuevoInquilino);
        });
    }

    private ContratoResponse mapToResponse(Contrato contrato) {
        return new ContratoResponse(
                contrato.getId(),
                contrato.getCodigoVinculacion(),
                contrato.getFechaEntrada(),
                contrato.getFechaSalida(),
                contrato.getPrecioBaseAlquiler(),
                contrato.getEstado(),
                contrato.getApartamento().getNombreInterno(),
                contrato.getInquilino() != null ? contrato.getInquilino().getNombre() + " " + contrato.getInquilino().getApellidos() : "Sin asignar"
        );
    }

    private ContratoDetalleResponse mapToDetalleResponse(Contrato r) {
        ContratoDetalleResponse.InquilinoPublico inquilinoData = null;
        if (r.getInquilino() != null) {
            inquilinoData = new ContratoDetalleResponse.InquilinoPublico(
                    r.getInquilino().getId(), r.getInquilino().getNombre(), r.getInquilino().getApellidos(),
                    r.getInquilino().getEmail(), r.getInquilino().getTelefono()
            );
        }
        return new ContratoDetalleResponse(
                r.getId(),
                r.getCodigoVinculacion(),
                r.getApartamento().getNombreInterno(),
                r.getFechaEntrada(),
                r.getFechaSalida(),
                r.getPrecioBaseAlquiler(),
                r.getFianza(),
                r.getEstado(),
                r.getCreadoEn(),
                r.getContratoPdf(),
                inquilinoData
        );
    }
}