package com.niyovi.proyecto.controller;

import com.niyovi.proyecto.enums.TipoMovimiento;
import com.niyovi.proyecto.model.MovimientoInventario;
import com.niyovi.proyecto.model.Producto;
import com.niyovi.proyecto.model.Rol;
import com.niyovi.proyecto.model.Usuario;
import com.niyovi.proyecto.repository.MovimientoInventarioRepository;
import com.niyovi.proyecto.repository.ProductoRepository;
import com.niyovi.proyecto.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class MovimientoInventarioController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/inventario/agregar/{idProducto}")
    public String mostrarFormularioAgregarStock(@PathVariable("idProducto") Long idProducto, Model model, Principal principal) {
        Usuario usuariom = usuarioService.buscarPorUsuario(principal.getName());
        Rol rolUsuario = usuariom.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        Producto producto = productoRepository.findById(idProducto).orElse(null);
        if (producto == null) {
            return "redirect:/consultar-productos";
        }
        model.addAttribute("idProducto", producto.getIdProducto());
        model.addAttribute("nombreProducto", producto.getNombreProducto());
        return "agregarStock";
    }

    @PostMapping("/inventario/agregar")
    public String agregarEntradaStock(@RequestParam("idProducto") Long idProducto,
                                      @RequestParam("cantidad") int cantidad,
                                      @RequestParam("observacion") String observacion,
                                      RedirectAttributes redirectAttributes, Model model, Principal principal) {
        Usuario usuariom = usuarioService.buscarPorUsuario(principal.getName());
        Rol rolUsuario = usuariom.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        Optional<Producto> productoOpt = productoRepository.findById(idProducto);
        if (productoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Producto no encontrado");
            return "redirect:/consultar-productos";
        }
        Producto producto = productoOpt.get();
        if (producto == null) {
            redirectAttributes.addFlashAttribute("mensajeError", "Producto no encontrado en la BD");
            return "redirect:/consultar-productos";
        }
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setCantidad(cantidad);
        movimiento.setTipoMovimiento(TipoMovimiento.ENTRADA);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setObservacion(observacion);
        movimientoInventarioRepository.save(movimiento);
        producto.setStockProducto(producto.getStockProducto() + cantidad);
        productoRepository.save(producto);
        redirectAttributes.addFlashAttribute("mensajeExito", "Stock actualizado correctamente");
        return "redirect:/consultar-productos";
    }
}
