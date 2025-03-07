package com.niyovi.proyecto.controller;

import com.niyovi.proyecto.model.Estado;
import com.niyovi.proyecto.model.Rol;
import com.niyovi.proyecto.model.TipoDocumento;
import com.niyovi.proyecto.model.Usuario;
import com.niyovi.proyecto.service.TipoDocumentoService;
import com.niyovi.proyecto.service.UsuarioService;
import com.niyovi.proyecto.repository.EstadoRepository;
import com.niyovi.proyecto.repository.RolRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final TipoDocumentoService tipoDocumentoService;
    private final EstadoRepository estadoRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioController(UsuarioService usuarioService,
                             TipoDocumentoService tipoDocumentoService,
                             EstadoRepository estadoRepository,
                             RolRepository rolRepository,
                             PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.tipoDocumentoService = tipoDocumentoService;
        this.estadoRepository = estadoRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/registrarse")
    public String mostrarFormularioRegistroCliente(Model model) {
        Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
        List<TipoDocumento> tiposDocumento = tipoDocumentoService.obtenerTiposDocumentoActivos(estadoActivo);
        Rol rolCliente = rolRepository.findById(2L).orElseThrow(() -> new RuntimeException("Rol cliente no encontrado"));
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("tiposDocumento", tiposDocumento);
        model.addAttribute("rol", rolCliente);
        return "registroCliente";
    }

    @PostMapping("/registrarse")
    public String registrarUsuarioCliente(@ModelAttribute("usuario") Usuario usuario,
                                          @RequestParam(value = "from", required = false) String from,
                                          RedirectAttributes redirectAttributes) {
        Usuario usuarioExistente = usuarioService.buscarPorUsuario(usuario.getUsuarioUsuario());
        if (usuarioExistente != null) {
            redirectAttributes.addFlashAttribute("mensajeError", "Usuario ya existe, intente con otro nombre de usuario.");
            return "redirect:/registrarse" + (from != null ? "?from=" + from : "");
        }
        try {
            Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
            Rol rolCliente = rolRepository.findById(2L).orElseThrow(() -> new RuntimeException("Rol cliente no encontrado"));
            usuario.setEstadoUsuario(estadoActivo);
            usuario.setRolUsuario(rolCliente);
            usuarioService.registrarUsuario(usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario registrado exitosamente.");
            return "redirect:/login" + (from != null ? "?from=" + from : "");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al registrar usuario: " + e.getMessage());
            return "redirect:/registrarse" + (from != null ? "?from=" + from : "");
        }
    }

    @GetMapping("/login")
    public String mostrarFormularioLogin(@RequestParam(value = "from", required = false) String from, Model model) {
        model.addAttribute("from", from);
        return "login";
    }

    @GetMapping("/registrar-empleado")
    public String mostrarFormularioRegistroEmpleado(Model model, Principal principal) {
        Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
        List<TipoDocumento> tiposDocumento = tipoDocumentoService.obtenerTiposDocumentoActivos(estadoActivo);
        List<Rol> roles = rolRepository.findByEstadoRol(estadoActivo);
        Usuario usuario = usuarioService.buscarPorUsuario(principal.getName());
        Rol rolUsuario = usuario.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("tiposDocumento", tiposDocumento);
        model.addAttribute("roles", roles);
        return "registroEmpleado";
    }

    @PostMapping("/registrar-empleado")
    public String registrarUsuarioEmpleado(@ModelAttribute("usuario") Usuario usuario, Model model, Principal principal) {
        Usuario usuarioExistente = usuarioService.buscarPorUsuario(usuario.getUsuarioUsuario());
        if (usuarioExistente != null) {
            model.addAttribute("mensajeError", "Usuario ya existe, intente con otro nombre de usuario.");
            Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
            List<TipoDocumento> tiposDocumento = tipoDocumentoService.obtenerTiposDocumentoActivos(estadoActivo);
            List<Rol> roles = rolRepository.findByEstadoRol(estadoActivo);
            model.addAttribute("tiposDocumento", tiposDocumento);
            model.addAttribute("roles", roles);
            return "registroEmpleado";
        }
        try {
            Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
            usuario.setEstadoUsuario(estadoActivo);
            usuarioService.registrarUsuario(usuario);
            model.addAttribute("mensajeExito", "Usuario registrado exitosamente.");
        } catch (Exception e) {
            model.addAttribute("mensajeError", "Error al registrar usuario: " + e.getMessage());
            Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
            List<TipoDocumento> tiposDocumento = tipoDocumentoService.obtenerTiposDocumentoActivos(estadoActivo);
            List<Rol> roles = rolRepository.findByEstadoRol(estadoActivo);
            model.addAttribute("tiposDocumento", tiposDocumento);
            model.addAttribute("roles", roles);
        }
        Usuario usuariom = usuarioService.buscarPorUsuario(principal.getName());
        Rol rolUsuario = usuariom.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        return "index";
    }

    @GetMapping("/recuperar-contraseña")
    public String mostrarFormularioRecuperarContraseña(Model model) {
        Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
        List<TipoDocumento> tiposDocumento = tipoDocumentoService.obtenerTiposDocumentoActivos(estadoActivo);
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("tiposDocumento", tiposDocumento);
        return "recuperarContraseña";
    }

    @PostMapping("/recuperar-contraseña")
    public String procesarRecuperarContraseña(@ModelAttribute("usuario") Usuario usuario, Model model, HttpSession session) {
        try {
            TipoDocumento tipoDocumento = tipoDocumentoService.obtenerPorId(usuario.getTipoDocUsuario().getIdTipoDoc());
            Usuario usuarioExistente = usuarioService.buscarPorDocumentoYCorreo(tipoDocumento, usuario.getNumeroDocUsuario(), usuario.getCorreoUsuario());
            if (usuarioExistente == null) {
                model.addAttribute("mensajeError", "Los datos no coinciden con ningún usuario registrado.");
                Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
                List<TipoDocumento> tiposDocumento = tipoDocumentoService.obtenerTiposDocumentoActivos(estadoActivo);
                model.addAttribute("tiposDocumento", tiposDocumento);
                return "recuperarContraseña";
            }
            String claveTemporal = usuarioService.generarClaveTemporal();
            usuarioService.actualizarClaveTemporal(usuarioExistente, claveTemporal, session);
            usuarioService.enviarCorreoRecuperacion(usuarioExistente.getCorreoUsuario(), claveTemporal);
            session.setAttribute("usuarioRecuperacion", usuarioExistente);
            model.addAttribute("mensajeExito", "Revisa la bandeja de entrada de tu correo electrónico para asignar la nueva contraseña.");
        } catch (Exception e) {
            model.addAttribute("mensajeError", "Error al procesar la recuperación de contraseña: " + e.getMessage());
            Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
            List<TipoDocumento> tiposDocumento = tipoDocumentoService.obtenerTiposDocumentoActivos(estadoActivo);
            model.addAttribute("tiposDocumento", tiposDocumento);
        }
        return "index";
    }

    @GetMapping("/cambiar-contrasena")
    public String mostrarFormularioCambioContraseña(HttpSession session, Model model, Principal principal) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioRecuperacion");
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("usuario", usuario);
        Usuario usuarioe = usuarioService.buscarPorUsuario(principal.getName());
        Rol rolUsuario = usuarioe.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        return "cambiarContraseña";
    }

    @PostMapping("/cambiar-contrasena")
    public String procesarCambioContraseña(@ModelAttribute("usuario") Usuario usuario, Model model, HttpSession session, Principal principal) {
        if (!usuario.getNuevaClave().equals(usuario.getConfirmarClave())) {
            model.addAttribute("mensajeError", "Las contraseñas no coinciden.");
            return "cambiarContraseña";
        }
        Usuario usuarioExistente = (Usuario) session.getAttribute("usuarioRecuperacion");
        if (usuarioExistente != null) {
            usuarioService.cambiarClave(usuarioExistente, usuario.getNuevaClave());
            session.removeAttribute("usuarioRecuperacion");
            model.addAttribute("mensajeExito", "Contraseña actualizada correctamente.");
        } else {
            model.addAttribute("mensajeError", "Usuario no encontrado.");
        }
        Usuario usuarioe = usuarioService.buscarPorUsuario(principal.getName());
        Rol rolUsuario = usuarioe.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        return "index";
    }

    @GetMapping("/recuperar-usuario")
    public String mostrarFormularioRecuperarUsuario(Model model) {
        Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
        List<TipoDocumento> tiposDocumento = tipoDocumentoService.obtenerTiposDocumentoActivos(estadoActivo);
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("tiposDocumento", tiposDocumento);
        return "recuperarUsuario";
    }

    @PostMapping("/recuperar-usuario")
    public String procesarRecuperarUsuario(@ModelAttribute("usuario") Usuario usuario, Model model, Principal principal) {
        try {
            TipoDocumento tipoDocumento = tipoDocumentoService.obtenerPorId(usuario.getTipoDocUsuario().getIdTipoDoc());
            Usuario usuarioExistente = usuarioService.buscarPorDocumentoYCorreo(tipoDocumento, usuario.getNumeroDocUsuario(), usuario.getCorreoUsuario());
            if (usuarioExistente == null) {
                model.addAttribute("mensajeError", "Los datos no coinciden con ningún usuario registrado.");
                Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
                List<TipoDocumento> tiposDocumento = tipoDocumentoService.obtenerTiposDocumentoActivos(estadoActivo);
                model.addAttribute("tiposDocumento", tiposDocumento);
                return "recuperarUsuario";
            }
            usuarioService.enviarCorreoRecuperacionUsuario(usuarioExistente.getCorreoUsuario(), usuarioExistente.getUsuarioUsuario());
            model.addAttribute("mensajeExito", "El nombre de usuario ha sido enviado a tu correo.");
        } catch (Exception e) {
            model.addAttribute("mensajeError", "Error al procesar la recuperación de usuario: " + e.getMessage());
            Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
            List<TipoDocumento> tiposDocumento = tipoDocumentoService.obtenerTiposDocumentoActivos(estadoActivo);
            model.addAttribute("tiposDocumento", tiposDocumento);
        }
        return "index";
    }

    @GetMapping("/consultar-usuarios")
    public String mostrarUsuariosActivos(@RequestParam(required = false) Long tipoDocUsuario,
                                         @RequestParam(required = false) String numeroDocUsuario,
                                         Model model, Principal principal) {
        Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
        List<TipoDocumento> tiposDocumento = tipoDocumentoService.obtenerTiposDocumentoActivos(estadoActivo);
        Usuario usuario = usuarioService.buscarPorUsuario(principal.getName());
        Rol rolUsuario = usuario.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("tiposDocumento", tiposDocumento);
        try {
            List<Usuario> usuarios;
            if (tipoDocUsuario != null && numeroDocUsuario != null && !numeroDocUsuario.isEmpty()) {
                TipoDocumento tipoDoc = tipoDocumentoService.obtenerPorId(tipoDocUsuario);
                if (tipoDoc != null) {
                    usuarios = usuarioService.buscarPorTipoYNumeroDocumento(tipoDoc, numeroDocUsuario);
                } else {
                    model.addAttribute("mensajeError", "Tipo de documento no encontrado.");
                    usuarios = usuarioService.obtenerUsuariosActivos();
                }
                model.addAttribute("usuarios", usuarios);
            } else {
                usuarios = usuarioService.obtenerUsuariosActivos();
                model.addAttribute("usuarios", usuarios);
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Ocurrió un error al recuperar los usuarios.");
        }
        return "consultarUsuarios";
    }

    @GetMapping("/editar-usuario/{id}")
    public String mostrarFormularioEdicion(@PathVariable("id") Long id, Model model, Principal principal) {
        Usuario usuario = usuarioService.buscarPorId(id);
        Usuario usuariom = usuarioService.buscarPorUsuario(principal.getName());
        Rol rolUsuario = usuariom.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
        List<Rol> roles = rolRepository.findByEstadoRol(estadoActivo);
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", roles);
        return "editarUsuario";
    }

    @PostMapping("/editar-usuario")
    public String procesarEdicion(@ModelAttribute("usuario") Usuario usuario, RedirectAttributes redirectAttributes, Principal principal, Model model) {
        try {
            usuarioService.actualizarUsuario(usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario actualizado correctamente.");
            return "redirect:/consultar-usuarios";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al actualizar el usuario: " + e.getMessage());
            Usuario usuarioe = usuarioService.buscarPorUsuario(principal.getName());
            Rol rolUsuario = usuarioe.getRolUsuario();
            model.addAttribute("rolUsuario", rolUsuario.getIdRol());
            return "redirect:/editar-usuario/" + usuario.getIdUsuario();
        }
    }

    @GetMapping("/eliminar-usuario/{id}")
    public String eliminarUsuario(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioService.buscarPorId(id);
            Estado estadoInactivo = estadoRepository.findById(2L)
                    .orElseThrow(() -> new RuntimeException("Estado inactivo no encontrado"));
            usuario.setEstadoUsuario(estadoInactivo);
            usuarioService.eliminarUsuario(usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar el usuario: " + e.getMessage());
        }
        return "redirect:/consultar-usuarios";
    }

    @GetMapping("/editar-perfil")
    public String mostrarFormularioEdicionPerfil(Model model) {
        Usuario usuario = usuarioService.obtenerUsuarioLogueado();
        if (usuario == null) {
            return "redirect:/login";
        }
        Rol rolUsuario = usuario.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        model.addAttribute("usuario", usuario);
        return "editarPerfil";
    }

    @PostMapping("/editar-perfil")
    public String procesarEdicionPerfil(@ModelAttribute("usuario") Usuario usuario, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuarioExistente = usuarioService.obtenerUsuarioLogueado();
            if (usuarioExistente == null) {
                redirectAttributes.addFlashAttribute("mensajeError", "No se puede actualizar el perfil. Usuario no autenticado.");
                return "redirect:/login";
            }
            usuario.setIdUsuario(usuarioExistente.getIdUsuario());
            usuarioService.editarPerfil(usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario actualizado correctamente.");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al actualizar el usuario: " + e.getMessage());
            return "redirect:/editar-perfil";
        }
    }
}
