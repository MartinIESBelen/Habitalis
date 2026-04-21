package com.apartmanagebackend.util;

import com.apartmanagebackend.domain.*;
import com.apartmanagebackend.domain.enums.*;
import com.apartmanagebackend.repository.*;
import com.apartmanagebackend.domain.*;
import com.apartmanagebackend.domain.enums.*;
import com.apartmanagebackend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final PropietarioRepository propietarioRepository;
    private final InquilinoRepository inquilinoRepository;
    private final ApartamentoRepository apartamentoRepository;
    private final ElementoInventarioRepository inventarioRepository;
    private final ReservaRepository reservaRepository;
    private final ReciboRepository reciboRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Iniciando la carga de datos de prueba para ApartManage...");

        // Evitar duplicados: si ya hay propietarios, no hacemos nada
        if (propietarioRepository.count() > 0) {
            log.info("La base de datos ya contiene información. Saltando DataLoader.");
            return;
        }

        // Crear PROPIETARIO
        Propietario propietario = Propietario.builder()
                .nombre("Martin")
                .apellidos("Sierra")
                .email("propietario@email.com")
                .password(passwordEncoder.encode("123456")) // Contraseña encriptada
                .telefono("600123456")
                .dniPasaporte("12345678A")
                .rol(RolUsuario.PROPIETARIO) // Suponiendo que tienes este valor en tu enum RolUsuario
                .iban("ES1234567890123456789012")
                .direccionFiscal("Calle Falsa 123, Madrid")
                .build();
        propietarioRepository.save(propietario);

        Propietario propietario2 = Propietario.builder()
                .nombre("Martin2")
                .apellidos("Sierra")
                .email("propietario2@email.com")
                .password(passwordEncoder.encode("1234567")) // Contraseña encriptada
                .telefono("600143455")
                .dniPasaporte("12345678F")
                .rol(RolUsuario.PROPIETARIO) // Suponiendo que tienes este valor en tu enum RolUsuario
                .iban("ES1234567890123456789013")
                .direccionFiscal("Calle Falsa 123, Malaga")
                .build();
        propietarioRepository.save(propietario2);

        // Crear INQUILINO
        Inquilino inquilino = Inquilino.builder()
                .nombre("Martin")
                .apellidos("Godinez")
                .email("inquilino@email.com")
                .password(passwordEncoder.encode("123456"))
                .telefono("600654321")
                .dniPasaporte("87654321B")
                .rol(RolUsuario.INQUILINO) // Suponiendo que tienes este valor en tu enum RolUsuario
                .fechaNacimiento(LocalDate.of(1990, 5, 15))
                .direccionHabitual("Av. Siempre Viva 742, Barcelona")
                .build();
        inquilinoRepository.save(inquilino);

        // Crear APARTAMENTO
        Apartamento apartamento = Apartamento.builder()
                .propietario(propietario)
                .nombreInterno("Ático Sol")
                .direccion("Plaza Mayor 1, 5º Derecha")
                .ciudad("Madrid")
                .descripcion("Precioso ático muy luminoso en el centro.")
                .estado(EstadoApartamento.ACTIVO) // Viene por defecto en tu Builder, pero lo ponemos por claridad
                .build();
        apartamentoRepository.save(apartamento);

        // Crear ELEMENTOS DE INVENTARIO
        ElementoInventario sofa = ElementoInventario.builder()
                .apartamento(apartamento)
                .nombre("Sofá de 3 plazas (Ikea)")
                .categoria(CategoriaItem.MUEBLE) // Suponiendo valores de tu Enum
                .estado(EstadoItem.BUENO)
                .precioCompra(new BigDecimal("350.00"))
                .fechaCompra(LocalDate.of(2023, 1, 10))
                .build();
        inventarioRepository.save(sofa);

        ElementoInventario tv = ElementoInventario.builder()
                .apartamento(apartamento)
                .nombre("Smart TV Samsung 55'")
                .categoria(CategoriaItem.ELECTRODOMESTICO) // Suponiendo valores de tu Enum
                .estado(EstadoItem.NUEVO)
                .precioCompra(new BigDecimal("499.99"))
                .fechaCompra(LocalDate.of(2024, 2, 20))
                .build();
        inventarioRepository.save(tv);

        // Crear RESERVA
        Reserva reserva = Reserva.builder()
                .apartamento(apartamento)
                .inquilino(inquilino)
                .codigoVinculacion("RES-2026-001")
                .fechaEntrada(LocalDate.now().plusDays(5))
                .fechaSalida(LocalDate.now().plusMonths(6))
                .precioBaseAlquiler(new BigDecimal("1200.00"))
                .estado(EstadoReserva.CONFIRMADA) // Suponiendo valores de tu Enum
                .build();
        reservaRepository.save(reserva);

        // Crear RECIBO para esa reserva
        Recibo recibo = Recibo.builder()
                .reserva(reserva)
                .mes(3)
                .anio(2026)
                .montoAlquiler(new BigDecimal("1200.00"))
                .montoLuz(new BigDecimal("45.50"))
                .montoAgua(new BigDecimal("20.00"))
                .totalPagar(new BigDecimal("1265.50"))
                .estado(EstadoRecibo.PENDIENTE) // Viene por defecto, pero lo indicamos
                .build();
        reciboRepository.save(recibo);

        log.info("¡Datos de prueba cargados con éxito! Ya puedes iniciar sesión en Angular con 'propietario@email.com' o 'inquilino@email.com' y contraseña '123456'.");
    }
}