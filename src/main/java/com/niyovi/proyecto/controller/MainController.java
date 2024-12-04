package com.niyovi.proyecto.controller;

import com.niyovi.proyecto.model.Rol;
import com.niyovi.proyecto.model.Usuario;
import com.niyovi.proyecto.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class MainController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/")
    public String home(Model model, Principal principal) {
        if (principal != null) {
            Usuario usuario = usuarioService.buscarPorUsuario(principal.getName());
            Rol rolUsuario = usuario.getRolUsuario();
            model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        } else {
            model.addAttribute("rolUsuario", null);
            model.addAttribute("carritoCount", 0);
        }
        return "redirect:/catalogo-productos";
    }
}