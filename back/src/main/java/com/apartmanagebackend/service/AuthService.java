package com.apartmanagebackend.service;

import com.apartmanagebackend.config.JwtService;
import com.apartmanagebackend.domain.Inquilino;
import com.apartmanagebackend.domain.Propietario;
import com.apartmanagebackend.domain.Usuario;
import com.apartmanagebackend.domain.enums.RolUsuario;
import com.apartmanagebackend.dto.auth.AuthResponse;
import com.apartmanagebackend.dto.auth.LoginRequest;
import com.apartmanagebackend.dto.auth.RefreshTokenRequest;
import com.apartmanagebackend.dto.auth.RegisterRequest;
import com.apartmanagebackend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        // Validar si el email ya existe
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        // REGLA DE SEGURIDAD: Nadie puede registrarse como ADMIN desde la API pública
        if (request.rol() == RolUsuario.ADMIN) {
            throw new RuntimeException("No está permitido registrar cuentas de Administrador desde esta vía.");
        }

        Usuario user;

        if (request.rol() == RolUsuario.PROPIETARIO) {
            user = Propietario.builder()
                    .nombre(request.nombre())
                    .email(request.email())
                    .password(passwordEncoder.encode(request.password()))
                    .rol(RolUsuario.PROPIETARIO)
                    .build();
        } else {
            // Asumimos Inquilino por defecto
            user = Inquilino.builder()
                    .nombre(request.nombre())
                    .email(request.email())
                    .password(passwordEncoder.encode(request.password()))
                    .rol(RolUsuario.INQUILINO)
                    .build();
        }

        // Guardar en BD
        usuarioRepository.save(user);

        // Generar Token
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(
                jwtToken,
                refreshToken,
                "Bearer",
                jwtService.getJwtExpiration() / 1000
        );
    }

    public AuthResponse login(LoginRequest request) {
        // Autenticar (Esto verifica usuario y contraseña automáticamente)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // Buscamos al usuario.
        var user = usuarioRepository.findByEmail(request.email())
                .orElseThrow();

        // Generamos Token
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(
                jwtToken,
                refreshToken,
                "Bearer",
                jwtService.getJwtExpiration() / 1000
        );
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();
        String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            Usuario user = usuarioRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Si el refresh token es válido, generamos un nuevo access token
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);

                return new AuthResponse(
                        accessToken,
                        refreshToken, // Devolvemos el mismo refresh token
                        "Bearer",
                        jwtService.getJwtExpiration() / 1000
                );
            }
        }
        throw new RuntimeException("Refresh token inválido o caducado");
    }
}
