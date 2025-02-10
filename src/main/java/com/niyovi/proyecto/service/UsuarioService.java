package com.niyovi.proyecto.service;

import com.niyovi.proyecto.model.Estado;
import com.niyovi.proyecto.model.TipoDocumento;
import com.niyovi.proyecto.model.Usuario;
import com.niyovi.proyecto.repository.EstadoRepository;
import com.niyovi.proyecto.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final EstadoRepository estadoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, EstadoRepository estadoRepository, PasswordEncoder passwordEncoder, JavaMailSender javaMailSender) {
        this.usuarioRepository = usuarioRepository;
        this.estadoRepository = estadoRepository;
        this.passwordEncoder = passwordEncoder;
        this.javaMailSender = javaMailSender;
    }

    private boolean esClaveValida(String clave) {
        return clave.length() >= 8 &&
                clave.matches(".*[A-Z].*") &&
                clave.matches(".*\\d.*") &&
                clave.matches(".*[^A-Za-z0-9].*");
    }

    public Usuario registrarUsuario(Usuario usuario) {
        if (!esClaveValida(usuario.getClaveUsuario())) {
            throw new IllegalArgumentException("La clave no cumple con los requisitos de seguridad.");
        }
        String claveEncriptada = passwordEncoder.encode(usuario.getClaveUsuario());
        usuario.setClaveUsuario(claveEncriptada);
        return usuarioRepository.save(usuario);
    }

    public Usuario buscarPorUsuario(String usuario) {
        return usuarioRepository.findByUsuarioUsuario(usuario);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsuarioUsuario(username);
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }
        return User.builder()
                .username(usuario.getUsuarioUsuario())
                .password(usuario.getClaveUsuario())
                .roles(usuario.getRolUsuario().getNombreRol())
                .build();
    }

    public Usuario buscarPorDocumentoYCorreo(TipoDocumento tipoDocumento, String numeroDocumento, String correo) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByTipoDocUsuarioAndNumeroDocUsuarioAndCorreoUsuario(tipoDocumento, numeroDocumento, correo);
        return usuarioOpt.orElse(null);
    }

    public String generarClaveTemporal() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&";
        StringBuilder claveTemporal = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            claveTemporal.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }
        return claveTemporal.toString();
    }

    public void actualizarClaveTemporal(Usuario usuario, String claveTemporal, HttpSession session) {
        usuario.setClaveUsuario(passwordEncoder.encode(claveTemporal));
        usuarioRepository.save(usuario);
        session.setAttribute("claveTemporal", true);
    }

    public void enviarCorreoRecuperacion(String correoUsuario, String claveTemporal) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(correoUsuario);
            mensaje.setSubject("Recuperación de contraseña");
            mensaje.setText("Tu nueva contraseña temporal es: " + claveTemporal);
            javaMailSender.send(mensaje);
        } catch (Exception e) {
            System.out.println("Error al enviar el correo: " + e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo de recuperación.");
        }
    }

    public void cambiarClave(Usuario usuario, String nuevaClave) {
        usuario.setClaveUsuario(passwordEncoder.encode(nuevaClave));
        usuarioRepository.save(usuario);
    }

    public void enviarCorreoRecuperacionUsuario(String correoUsuario, String nombreUsuario) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(correoUsuario);
            mensaje.setSubject("Recuperación de nombre de usuario");
            mensaje.setText("Tu nombre de usuario es: " + nombreUsuario);
            javaMailSender.send(mensaje);
        } catch (Exception e) {
            System.out.println("Error al enviar el correo: " + e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo de recuperación.");
        }
    }

    public List<Usuario> obtenerUsuariosActivos() {
        Estado estadoActivo = estadoRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
        return usuarioRepository.findByEstadoUsuario(estadoActivo);
    }

    public void actualizarUsuario(Usuario usuario) {
        Usuario usuarioExistente = usuarioRepository.findById(usuario.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuarioExistente.setNombresUsuario(usuario.getNombresUsuario());
        usuarioExistente.setApellidosUsuario(usuario.getApellidosUsuario());
        usuarioExistente.setCelularUsuario(usuario.getCelularUsuario());
        usuarioExistente.setDireccionUsuario(usuario.getDireccionUsuario());
        usuarioExistente.setCorreoUsuario(usuario.getCorreoUsuario());
        usuarioExistente.setUsuarioUsuario(usuario.getUsuarioUsuario());
        usuarioExistente.setRolUsuario(usuario.getRolUsuario());
        if (usuario.getClaveUsuario() != null && !usuario.getClaveUsuario().isEmpty()) {
            String nuevaClaveEncriptada = passwordEncoder.encode(usuario.getClaveUsuario());
            usuarioExistente.setClaveUsuario(nuevaClaveEncriptada);
        }
        usuarioRepository.save(usuarioExistente);
    }

    public void eliminarUsuario(Usuario usuario) {
        Usuario usuarioExistente = usuarioRepository.findById(usuario.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuarioExistente.setEstadoUsuario(usuario.getEstadoUsuario());
        usuarioRepository.save(usuarioExistente);
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public List<Usuario> buscarPorTipoYNumeroDocumento(TipoDocumento tipoDocumento, String numeroDocumento) {
        if (tipoDocumento == null || numeroDocumento == null || numeroDocumento.isEmpty()) {
            return Collections.emptyList();
        }
        return usuarioRepository.findByTipoDocUsuarioAndNumeroDocUsuario(tipoDocumento, numeroDocumento);
    }

    public void editarPerfil(Usuario usuario) {
        Usuario usuarioExistente = usuarioRepository.findById(usuario.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuarioExistente.setNombresUsuario(usuario.getNombresUsuario());
        usuarioExistente.setApellidosUsuario(usuario.getApellidosUsuario());
        usuarioExistente.setCelularUsuario(usuario.getCelularUsuario());
        usuarioExistente.setDireccionUsuario(usuario.getDireccionUsuario());
        usuarioExistente.setCorreoUsuario(usuario.getCorreoUsuario());
        usuarioExistente.setUsuarioUsuario(usuario.getUsuarioUsuario());
        if (usuario.getClaveUsuario() != null && !usuario.getClaveUsuario().isEmpty()) {
            String nuevaClaveEncriptada = passwordEncoder.encode(usuario.getClaveUsuario());
            usuarioExistente.setClaveUsuario(nuevaClaveEncriptada);
        }
        usuarioRepository.save(usuarioExistente);
    }

    public Usuario obtenerUsuarioLogueado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            return usuarioRepository.findByUsuarioUsuario(username);
        }
        return null;
    }

    public void actualizarUsuarioCompra(Usuario usuario) {
        usuarioRepository.save(usuario);
    }

    public String obtenerCorreoAdministrador() {
        Usuario administrador = usuarioRepository.findByRolUsuarioIdRol(1L);
        return (administrador != null) ? administrador.getCorreoUsuario() : null;
    }
}