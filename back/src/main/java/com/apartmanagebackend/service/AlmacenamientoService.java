package com.apartmanagebackend.service;

import com.apartmanagebackend.domain.Apartamento;
import com.apartmanagebackend.domain.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import com.apartmanagebackend.repository.UsuarioRepository;
import com.apartmanagebackend.repository.ApartamentoRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlmacenamientoService {

    private final UsuarioRepository usuarioRepository;
    private final ApartamentoRepository apartamentoRepository;

    @Value("${app.almacenamiento.raiz}")
    private String rutaRaiz;


    public String getCarpetaUsuario(Usuario usuario) {
        String nombre = limpiar(usuario.getNombre());
        return rutaRaiz + "Usuarios/" + nombre + "-" + usuario.getId() + "/";
    }

    public String getCarpetaVivienda(Apartamento apto) {
        String propNombre = limpiar(apto.getPropietario().getNombre());
        String aptoNombre = limpiar(apto.getNombreInterno());
        return rutaRaiz + "Usuarios/" + propNombre + "-" + apto.getPropietario().getId()
                + "/Viviendas/" + aptoNombre + "-" + apto.getId() + "/";
    }


    public void inicializarCarpetasUsuario(Usuario usuario) {
        String base = getCarpetaUsuario(usuario);
        crearDirectorios(base + "perfil", base + "Viviendas");
    }

    public void inicializarCarpetasVivienda(Apartamento apartamento) {
        String base = getCarpetaVivienda(apartamento);
        crearDirectorios(base + "img", base + "docs");
    }


    public String guardarImagenPerfil(Usuario usuario, MultipartFile file) throws IOException {
        String carpeta = getCarpetaUsuario(usuario) + "perfil/";
        String nombreUnico = UUID.randomUUID() + "_" + file.getOriginalFilename();
        guardarFisicamente(file, carpeta, nombreUnico);

        return "Usuarios/" + limpiar(usuario.getNombre()) + "-" + usuario.getId()
                + "/perfil/" + nombreUnico;
    }

    public String guardarImagenVivienda(Apartamento apto, MultipartFile file) throws IOException {
        String carpeta = getCarpetaVivienda(apto) + "img/";
        String nombreUnico = UUID.randomUUID() + "_" + file.getOriginalFilename();
        guardarFisicamente(file, carpeta, nombreUnico);

        return buildRutaRelativa(apto) + "/img/" + nombreUnico;
    }

    public String guardarDocumentoVivienda(Apartamento apto, MultipartFile file) throws IOException {
        String carpeta = getCarpetaVivienda(apto) + "docs/";
        String nombreUnico = UUID.randomUUID() + "_" + file.getOriginalFilename();
        guardarFisicamente(file, carpeta, nombreUnico);

        return buildRutaRelativa(apto) + "/docs/" + nombreUnico;
    }

    public void eliminarArchivo(String rutaRelativa) throws IOException {
        Files.deleteIfExists(Paths.get(rutaRaiz).resolve(rutaRelativa));
    }

    public void eliminarCarpetaVivienda(Apartamento apartamento) {
        Path ruta = Paths.get(getCarpetaVivienda(apartamento));
        try {
            FileSystemUtils.deleteRecursively(ruta);
        } catch (IOException e) {
            System.err.println("Aviso: carpeta física no eliminada: " + e.getMessage());
        }
    }


    private void guardarFisicamente(MultipartFile file, String carpeta, String nombre) throws IOException {
        Files.createDirectories(Paths.get(carpeta));
        Files.copy(file.getInputStream(), Paths.get(carpeta + nombre));
    }

    private void crearDirectorios(String... rutas) {
        try {
            for (String ruta : rutas) {
                Files.createDirectories(Paths.get(ruta));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al crear carpetas: " + e.getMessage());
        }
    }

    private String buildRutaRelativa(Apartamento apto) {
        return "Usuarios/" + limpiar(apto.getPropietario().getNombre()) + "-"
                + apto.getPropietario().getId()
                + "/Viviendas/" + limpiar(apto.getNombreInterno()) + "-" + apto.getId();
    }

    private String limpiar(String texto) {
        return texto.replaceAll("[^a-zA-Z0-9]", "_");
    }

    public boolean tieneAccesoAArchivo(String subruta, String emailUsuarioActual) {
        try {
            String[] partes = subruta.split("/");
            Long idPropietarioRuta = Long.parseLong(
                    partes[1].substring(partes[1].lastIndexOf("-") + 1)
            );

            Usuario usuarioActual = usuarioRepository.findByEmail(emailUsuarioActual)
                    .orElseThrow();

            if (usuarioActual.getId().equals(idPropietarioRuta)) return true;

            if (partes.length >= 4 && "Viviendas".equals(partes[2])) {
                Long idApartamento = Long.parseLong(
                        partes[3].substring(partes[3].lastIndexOf("-") + 1)
                );
                return apartamentoRepository.esInquilinoActivo(idApartamento, usuarioActual.getId());
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }
}