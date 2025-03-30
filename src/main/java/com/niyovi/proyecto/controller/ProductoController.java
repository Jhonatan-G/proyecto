package com.niyovi.proyecto.controller;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.niyovi.proyecto.model.*;
import com.niyovi.proyecto.repository.*;
import com.niyovi.proyecto.service.*;
import jakarta.servlet.http.HttpSession;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
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

    @Value("${aws.access.key}")
    private String accessKey;

    @Value("${aws.secret.key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.s3.bucket}")
    private String bucketName;

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
                                  RedirectAttributes redirectAttributes,
                                  Model model, Principal principal) {
        try {
            if (producto.getStockProducto() == null) {
                producto.setStockProducto(0);
            }
            if (!imagenFile.isEmpty()) {
                File archivoTemporal = File.createTempFile("producto", ".jpg");
                Thumbnails.of(imagenFile.getInputStream())
                        .size(800, 800)
                        .outputFormat("jpg")
                        .toFile(archivoTemporal);
                AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                        .withRegion(region)
                        .withCredentials(new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(accessKey, secretKey)))
                        .build();
                String nombreArchivo = "Productos/" + UUID.randomUUID() + ".jpg";
                s3Client.putObject(new PutObjectRequest(bucketName, nombreArchivo, archivoTemporal));
                producto.setImagenProducto(nombreArchivo);
            }
            Estado estadoActivo = estadoRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
            producto.setEstadoProducto(estadoActivo);
            productoService.guardarProducto(producto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Producto guardado correctamente.");
            Usuario usuario = usuarioService.buscarPorUsuario(principal.getName());
            Rol rolUsuario = usuario.getRolUsuario();
            model.addAttribute("rolUsuario", rolUsuario.getIdRol());
            return "redirect:/consultar-productos";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al guardar la imagen: " + e.getMessage());
            Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
            List<Categoria> categorias = categoriaService.obtenerCategoriasActivas(estadoActivo);
            Usuario usuario = usuarioService.buscarPorUsuario(principal.getName());
            Rol rolUsuario = usuario.getRolUsuario();
            model.addAttribute("rolUsuario", rolUsuario.getIdRol());
            return "registroProducto";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al guardar el producto: " + e.getMessage());
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
                                          @RequestParam(defaultValue = "0") int page,
                                          Model model, Principal principal) {
        Usuario usuariom = usuarioService.buscarPorUsuario(principal.getName());
        Rol rolUsuario = usuariom.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
        List<Categoria> categorias = categoriaService.obtenerCategoriasActivas(estadoActivo);
        model.addAttribute("categorias", categorias);
        Pageable pageable = PageRequest.of(page, 5);
        Page<Producto> productosActivos = productoService.obtenerProductosActivos(pageable);
        model.addAttribute("productos", productosActivos);
        Page<Producto> productos = productoService.filtrarProductos(categoriaId, nombreProducto, pageable);
        model.addAttribute("productos", productos);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productos.getTotalPages());
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
            Producto productoExistente = productoService.obtenerProductoPorId(producto.getIdProducto());
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(region)
                    .withCredentials(new AWSStaticCredentialsProvider(
                            new BasicAWSCredentials(accessKey, secretKey)))
                    .build();
            if (!imagenFile.isEmpty()) {
                File archivoTemporal = File.createTempFile("producto", ".jpg");
                Thumbnails.of(imagenFile.getInputStream())
                        .size(800, 800)
                        .outputFormat("jpg")
                        .toFile(archivoTemporal);
                String nombreArchivo = "Productos/" + UUID.randomUUID() + ".jpg";
                s3Client.putObject(new PutObjectRequest(bucketName, nombreArchivo, archivoTemporal));
                if (productoExistente.getImagenProducto() != null) {
                    s3Client.deleteObject(bucketName, productoExistente.getImagenProducto());
                }
                productoExistente.setImagenProducto(nombreArchivo);
            }
            productoExistente.setNombreProducto(producto.getNombreProducto());
            productoExistente.setDescripcionProducto(producto.getDescripcionProducto());
            productoExistente.setPrecioProducto(producto.getPrecioProducto());
            productoExistente.setCategoriaProducto(producto.getCategoriaProducto());
            productoService.actualizarProducto(productoExistente);
            redirectAttributes.addFlashAttribute("mensajeExito", "Producto actualizado correctamente.");
            return "redirect:/consultar-productos";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al actualizar la imagen: " + e.getMessage());
            return "redirect:/editar-producto/" + producto.getIdProducto();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al actualizar el producto: " + e.getMessage());
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