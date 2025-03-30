package com.niyovi.proyecto.controller;

import com.niyovi.proyecto.model.Compra;
import com.niyovi.proyecto.model.Rol;
import com.niyovi.proyecto.model.Usuario;
import com.niyovi.proyecto.service.CompraService;
import com.niyovi.proyecto.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
public class InformacionController {

    @Autowired
    private CompraService compraService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/nosotros")
    public String mostrarNosotros(Model model, Principal principal) {
        List<Compra> reseñas = compraService.obtenerReseñasClientes();
        double promedioCalificacion = reseñas.stream()
                .mapToInt(Compra::getCalificacionCompra)
                .average()
                .orElse(0.0);
        model.addAttribute("reseñas", reseñas);
        model.addAttribute("promedioCalificacion", promedioCalificacion);
        if (principal != null) {
            Usuario usuario = usuarioService.buscarPorUsuario(principal.getName());
            Rol rolUsuario = usuario.getRolUsuario();
            model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        }
        return "nosotros";
    }
}