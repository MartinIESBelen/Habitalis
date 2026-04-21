package com.apartmanagebackend.service;

import com.apartmanagebackend.domain.Usuario;
import com.apartmanagebackend.domain.enums.RolUsuario;
import com.apartmanagebackend.dto.user.UsuarioGlobalResponse;
import com.apartmanagebackend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdministradorService {

    private final UsuarioRepository usuarioRepository;

    // SUPERPODER 1: Ver a todos los usuarios de la plataforma (Versión Segura)
    public List<UsuarioGlobalResponse> listarTodosLosUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::mapToResponse) // Transformamos cada Usuario en un DTO
                .collect(Collectors.toList());
    }

    public UsuarioGlobalResponse cambiarEstadoBloqueo(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Regla de oro: No banees a tus compañeros de trabajo (ni a ti mismo)
        if (usuario.getRol() == RolUsuario.ADMIN) {
            throw new RuntimeException("No está permitido bloquear a un Administrador");
        }

        // Hacemos el efecto interruptor (si es true pasa a false, y viceversa)
        usuario.setBloqueado(!usuario.isBloqueado());

        // Guardamos y devolvemos el DTO actualizado
        return mapToResponse(usuarioRepository.save(usuario));
    }

    private UsuarioGlobalResponse mapToResponse(Usuario usuario) {
        return new UsuarioGlobalResponse(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getRol(),
                usuario.isBloqueado()
        );
    }
}
