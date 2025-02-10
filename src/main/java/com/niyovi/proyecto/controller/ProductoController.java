package com.niyovi.proyecto.controller;

import com.niyovi.proyecto.model.*;
import com.niyovi.proyecto.repository.*;
import com.niyovi.proyecto.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private EstadoRepository estadoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping("/registrar-producto")
    public String mostrarFormularioNuevoProducto(Model model, Principal principal) {
        Usuario usuariom = usuarioService.buscarPorUsuario(principal.getName());
        Rol rolUsuario = usuariom.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
        List<Categoria> categorias = categoriaService.obtenerCategoriasActivas(estadoActivo);
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categorias);
        return "registroProducto";
    }

    @PostMapping("/registrar-producto")
    public String guardarProducto(@ModelAttribute("producto") Producto producto,
                                  @RequestParam("imagenFile") MultipartFile imagenFile,
                                  Model model, Principal principal) {
        try {
            String rutaImagen = "C:/Users/Jhona/OneDrive/Imagenes Proyecto/";
            Path directorioPath = Paths.get(rutaImagen);
            if (!Files.exists(directorioPath)) {
                Files.createDirectories(directorioPath);
            }
            if (!imagenFile.isEmpty()) {
                String nombreArchivo = imagenFile.getOriginalFilename();
                Path rutaCompleta = Paths.get(rutaImagen + nombreArchivo);
                Files.copy(imagenFile.getInputStream(), rutaCompleta, StandardCopyOption.REPLACE_EXISTING);
                producto.setImagenProducto(nombreArchivo);
            }
            Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
            producto.setEstadoProducto(estadoActivo);
            productoService.guardarProducto(producto);
            model.addAttribute("mensajeExito", "Producto guardado correctamente.");
            Usuario usuario = usuarioService.buscarPorUsuario(principal.getName());
            Rol rolUsuario = usuario.getRolUsuario();
            model.addAttribute("rolUsuario", rolUsuario.getIdRol());
            return "index";
        } catch (IOException e) {
            model.addAttribute("mensajeError", "Error al guardar la imagen: " + e.getMessage());
            Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
            List<Categoria> categorias = categoriaService.obtenerCategoriasActivas(estadoActivo);
            Usuario usuario = usuarioService.buscarPorUsuario(principal.getName());
            Rol rolUsuario = usuario.getRolUsuario();
            model.addAttribute("rolUsuario", rolUsuario.getIdRol());
            return "registroProducto";
        } catch (Exception e) {
            model.addAttribute("mensajeError", "Error al guardar el producto: " + e.getMessage());
            Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
            List<Categoria> categorias = categoriaService.obtenerCategoriasActivas(estadoActivo);
            Usuario usuario = usuarioService.buscarPorUsuario(principal.getName());
            Rol rolUsuario = usuario.getRolUsuario();
            model.addAttribute("rolUsuario", rolUsuario.getIdRol());
            return "registroProducto";
        }
    }

    @GetMapping("/consultar-productos")
    public String mostrarProductosActivos(@RequestParam(value = "categoriaProducto", required = false) Long categoriaId,
                                          @RequestParam(value = "nombreProducto", required = false) String nombreProducto,
                                          Model model, Principal principal) {
        Usuario usuariom = usuarioService.buscarPorUsuario(principal.getName());
        Rol rolUsuario = usuariom.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
        List<Categoria> categorias = categoriaService.obtenerCategoriasActivas(estadoActivo);
        model.addAttribute("categorias", categorias);
        List<Producto> productosActivos = productoService.obtenerProductosActivos();
        model.addAttribute("productos", productosActivos);
        List<Producto> productos = productoService.filtrarProductos(categoriaId, nombreProducto);
        model.addAttribute("productos", productos);
        return "consultarProductos";
    }

    @GetMapping("/editar-producto/{id}")
    public String mostrarFormularioEdicion(@PathVariable("id") Long id, Model model, Principal principal) {
        Producto producto = productoService.obtenerProductoPorId(id);
        Usuario usuariom = usuarioService.buscarPorUsuario(principal.getName());
        Rol rolUsuario = usuariom.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
        List<Categoria> categorias = categoriaService.obtenerCategoriasActivas(estadoActivo);
        model.addAttribute("producto", producto);
        model.addAttribute("categorias", categorias);
        return "editarProducto";
    }

    @PostMapping("/editar-producto")
    public String procesarEdicion(@ModelAttribute("producto") Producto producto,
                                  RedirectAttributes redirectAttributes,
                                  @RequestParam("imagenFile") MultipartFile imagenFile,
                                  Model model, Principal principal) {
        try {
            String rutaImagen = "C:/Users/Jhona/OneDrive/Imagenes Proyecto/";
            Path directorioPath = Paths.get(rutaImagen);
            if (!Files.exists(directorioPath)) {
                Files.createDirectories(directorioPath);
            }
            if (!imagenFile.isEmpty()) {
                String nombreArchivo = imagenFile.getOriginalFilename();
                Path rutaCompleta = Paths.get(rutaImagen + nombreArchivo);
                Files.copy(imagenFile.getInputStream(), rutaCompleta, StandardCopyOption.REPLACE_EXISTING);
                producto.setImagenProducto(nombreArchivo);
            } else {
                Producto productoExistente = productoService.obtenerProductoPorId(producto.getIdProducto());
                producto.setImagenProducto(productoExistente.getImagenProducto());
            }
            Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
            producto.setEstadoProducto(estadoActivo);
            productoService.actualizarProducto(producto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Producto actualizado correctamente.");
            return "redirect:/consultar-productos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al actualizar el producto: " + e.getMessage());
            Usuario usuario = usuarioService.buscarPorUsuario(principal.getName());
            Rol rolUsuario = usuario.getRolUsuario();
            model.addAttribute("rolUsuario", rolUsuario.getIdRol());
            return "redirect:/editar-producto/" + producto.getIdProducto();
        }
    }

    @GetMapping("/eliminar-producto/{id}")
    public String eliminarProducto(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Producto producto = productoService.obtenerProductoPorId(id);
            Estado estadoInactivo = estadoRepository.findById(2L)
                    .orElseThrow(() -> new RuntimeException("Estado inactivo no encontrado"));
            producto.setEstadoProducto(estadoInactivo);
            productoService.eliminarProducto(producto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Producto eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar el producto: " + e.getMessage());
        }
        return "redirect:/consultar-productos";
    }

    public String formatoPrecio(Double precio) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);
        String formattedPrice = format.format(precio);
        return formattedPrice;
    }

    @GetMapping("/catalogo-productos")
    public String mostrarCatalogo(
            @RequestParam(required = false) Long categoriaProducto,
            @RequestParam(required = false) String nombreProducto,
            @RequestParam(required = false) Integer precioMinimo,
            @RequestParam(required = false) Integer precioMaximo,
            @RequestParam(required = false, defaultValue = "menor") String ordenarPor,
            Model model, Principal principal) {
        Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
        List<Categoria> categorias = categoriaService.obtenerCategoriasActivas(estadoActivo);
        model.addAttribute("categorias", categorias);
        List<Producto> productosActivos = productoService.obtenerProductosActivos();
        model.addAttribute("productos", productosActivos);
        List<Producto> productos = productoService.filtrarProductosCatalogo(categoriaProducto, nombreProducto, precioMinimo, precioMaximo, ordenarPor);
        model.addAttribute("productos", productos);
        if ("mayor".equals(ordenarPor)) {
            productos.sort(Comparator.comparingDouble(Producto::getPrecioProducto).reversed());
        } else {
            productos.sort(Comparator.comparingDouble(Producto::getPrecioProducto));
        }
        if (principal != null) {
            Usuario usuario = usuarioService.buscarPorUsuario(principal.getName());
            Rol rolUsuario = usuario.getRolUsuario();
            model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        }
        return "catalogo";
    }

    @GetMapping("/carrito")
    public String mostrarCarrito(Model model, HttpSession session, Principal principal) {
        List<Producto> productosCarrito = new ArrayList<>();
        if (principal != null) {
            Usuario usuario = usuarioService.buscarPorUsuario(principal.getName());
            model.addAttribute("usuario", usuario);
            Rol rolUsuario = usuario.getRolUsuario();
            model.addAttribute("rolUsuario", rolUsuario.getIdRol());
            List<Long> carrito = (List<Long>) session.getAttribute("carrito");
            if (carrito != null && !carrito.isEmpty()) {
                productosCarrito = carrito.stream()
                        .map(idProducto -> productoService.obtenerProductoPorId(idProducto))
                        .collect(Collectors.toList());
            }
        } else {
            model.addAttribute("usuario", null);
        }
        model.addAttribute("productosCarrito", productosCarrito);
        return "carrito";
    }
}