package com.niyovi.proyecto.config;

import com.niyovi.proyecto.model.Usuario;
import com.niyovi.proyecto.service.UsuarioService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthentication implements AuthenticationSuccessHandler {

    private final UsuarioService usuarioService;

    public CustomAuthentication(@Lazy UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        HttpSession session = request.getSession();
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        Usuario usuario = usuarioService.buscarPorUsuario(username);
        Boolean claveTemporal = (Boolean) session.getAttribute("claveTemporal");
        if (claveTemporal != null && claveTemporal) {
            response.sendRedirect("/cambiar-contrasena");
            session.removeAttribute("claveTemporal");
        } else {
            String from = request.getParameter("from");
            if ("finalizarCompra".equals(from)) {
                response.sendRedirect("/confirmar-datos-compra");
            } else {
                response.sendRedirect("/");
            }
        }
    }
}
