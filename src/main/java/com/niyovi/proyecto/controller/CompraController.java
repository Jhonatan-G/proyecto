package com.niyovi.proyecto.controller;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;

import com.niyovi.proyecto.enums.TipoMovimiento;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URL;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

@Controller
public class CompraController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EstadoRepository estadoRepository;

    @Autowired
    private MetodoEntregaRepository metodoEntregaRepository;

    @Autowired
    private FormaPagoRepository formaPagoRepository;

    @Autowired
    private CompraService compraService;

    @Autowired
    private EstadoService estadoService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private DetalleCompraRepository detalleCompraRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Value("${aws.access.key}")
    private String accessKey;

    @Value("${aws.secret.key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @GetMapping("/finalizar-compra")
    public String finalizarCompra(Model model, Principal principal) {
        if (principal != null) {
            return "redirect:/confirmarDatosCompra";
        } else {
            return "redirect:/login?from=finalizarCompra";
        }
    }

    @GetMapping("/confirmar-datos-compra")
    public String mostrarConfirmarDatosCompra(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        Usuario usuario = usuarioService.obtenerUsuarioLogueado();
        if (usuario == null) {
            return "redirect:/login";
        }
        Rol rolUsuario = usuario.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        model.addAttribute("usuario", usuario);
        return "confirmarDatosCompra";
    }

    @GetMapping("/confirmar-metodo-entrega")
    public String mostrarMetodosEntrega(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        Usuario usuario = usuarioService.obtenerUsuarioLogueado();
        if (usuario == null) {
            return "redirect:/login";
        }
        Rol rolUsuario = usuario.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        model.addAttribute("usuario", usuario);
        Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
        List<MetodoEntrega> metodosEntrega = metodoEntregaRepository.findByEstadoMetodoEntrega(estadoActivo);
        model.addAttribute("metodosEntrega", metodosEntrega);
        return "confirmarMetodoEntrega";
    }

    @PostMapping("/actualizar-datos")
    public String actualizarDatos(@ModelAttribute("usuario") Usuario usuarioForm, RedirectAttributes redirectAttributes) {
        Usuario usuarioOriginal = usuarioService.buscarPorId(usuarioForm.getIdUsuario());
        if (usuarioOriginal != null) {
            usuarioOriginal.setNombresUsuario(usuarioForm.getNombresUsuario());
            usuarioOriginal.setApellidosUsuario(usuarioForm.getApellidosUsuario());
            usuarioOriginal.setCelularUsuario(usuarioForm.getCelularUsuario());
            usuarioOriginal.setDireccionUsuario(usuarioForm.getDireccionUsuario());
            usuarioOriginal.setCorreoUsuario(usuarioForm.getCorreoUsuario());
            usuarioService.actualizarUsuarioCompra(usuarioOriginal);
        }
        return "redirect:/confirmar-metodo-entrega";
    }

    @PostMapping("/guardar-metodo-entrega")
    public String guardarMetodoEntrega(@RequestParam("metodoEntregaSeleccionado") Long idMetodoEntrega,
                                       Model model,
                                       HttpSession session) {
        MetodoEntrega metodoEntregaSeleccionado = metodoEntregaRepository.findById(idMetodoEntrega)
                .orElseThrow(() -> new IllegalArgumentException("Método de entrega no válido"));
        session.setAttribute("metodoEntregaSeleccionado", metodoEntregaSeleccionado);
        model.addAttribute("mensajeExito", "Método de entrega seleccionado correctamente.");
        return "redirect:/confirmar-forma-pago";
    }

    @GetMapping("/confirmar-forma-pago")
    public String mostrarFormasPago(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        Usuario usuario = usuarioService.obtenerUsuarioLogueado();
        if (usuario == null) {
            return "redirect:/login";
        }
        Rol rolUsuario = usuario.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        model.addAttribute("usuario", usuario);
        Estado estadoActivo = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
        List<FormaPago> formaPagos = formaPagoRepository.findByEstadoFormaPago(estadoActivo);
        model.addAttribute("formaPagos", formaPagos);
        return "confirmarFormaPago";
    }

    @PostMapping("/guardar-compra")
    public String guardarCompra(@RequestParam("formaPagoSeleccionado") Long idFormaPago,
                                @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
                                Principal principal, RedirectAttributes redirectAttributes, Compra compra,
                                @RequestParam("carritoOculto") String carritoJSON,
                                HttpSession session, Model model) {
        if (principal == null || usuarioService.obtenerUsuarioLogueado() == null) {
            return "redirect:/login";
        }
        Usuario usuario = usuarioService.obtenerUsuarioLogueado();
        MetodoEntrega metodoEntregaSeleccionado = (MetodoEntrega) session.getAttribute("metodoEntregaSeleccionado");
        if (metodoEntregaSeleccionado == null) {
            model.addAttribute("mensajeError", "No se ha seleccionado un método de entrega.");
            return "confirmarFormaPago";
        }
        if (carritoJSON == null || carritoJSON.isEmpty()) {
            model.addAttribute("mensajeError", "El carrito está vacío o no tiene productos.");
            return "confirmarFormaPago";
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Producto> carrito = objectMapper.readValue(carritoJSON, new TypeReference<List<Producto>>() {
            });
            if (!imagenFile.isEmpty()) {
                File archivoTemporal = File.createTempFile("compresion", ".jpg");
                Thumbnails.of(imagenFile.getInputStream())
                        .size(800, 800)
                        .outputFormat("jpg")
                        .toFile(archivoTemporal);
                AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                        .withRegion(region)
                        .withCredentials(new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(accessKey, secretKey)))
                        .build();
                String nombreArchivo = "Comprobantes/" + UUID.randomUUID() + ".jpg";
                s3Client.putObject(new PutObjectRequest(bucketName, nombreArchivo, archivoTemporal));
                compra.setImagenComprobanteCompra(nombreArchivo);
            }
            FormaPago formaPago = formaPagoRepository.findById(idFormaPago)
                    .orElseThrow(() -> new RuntimeException("Forma de pago no válida"));
            if ((formaPago.getNombreFormaPago().equalsIgnoreCase("Nequi") ||
                    formaPago.getNombreFormaPago().equalsIgnoreCase("Daviplata"))
                    && (imagenFile == null || imagenFile.isEmpty())) {
                redirectAttributes.addFlashAttribute("mensajeError", "Debe adjuntar un comprobante de pago para Nequi o Daviplata.");
                return "redirect:/confirmar-forma-pago";
            }
            Estado estadoCompra;
            if (formaPago.getNombreFormaPago().equalsIgnoreCase("Nequi") ||
                    formaPago.getNombreFormaPago().equalsIgnoreCase("Daviplata")) {
                estadoCompra = estadoRepository.findById(7L)
                        .orElseThrow(() -> new RuntimeException("Estado 'Pendiente' no encontrado"));
                compra.setObservacionCompra("Revisando comprobante de pago.");
            } else {
                estadoCompra = estadoRepository.findById(3L)
                        .orElseThrow(() -> new RuntimeException("Estado 'Recibido' no encontrado"));
            }
            compra.setUsuarioCompra(usuario);
            compra.setMetodoEntregaCompra(metodoEntregaSeleccionado);
            compra.setFormaPagoCompra(formaPago);
            compra.setEstadoCompra(estadoCompra);
            List<DetalleCompra> detalles = new ArrayList<>();
            double totalCompra = 0.0;
            for (Producto productoCarrito : carrito) {
                Producto productoExistente = productoRepository.findById(productoCarrito.getIdProducto())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productoCarrito.getIdProducto()));
                if (productoExistente.getStockProducto() < productoCarrito.getCantidadCarrito()) {
                    redirectAttributes.addFlashAttribute("mensajeError", "Stock insuficiente para el producto: " + productoExistente.getNombreProducto());
                    return "redirect:/carrito";
                }
            }
            List<Producto> productosBajoStock = new ArrayList<>();
            for (Producto productoCarrito : carrito) {
                Producto productoExistente = productoRepository.findById(productoCarrito.getIdProducto())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productoCarrito.getIdProducto()));
                DetalleCompra detalle = new DetalleCompra();
                detalle.setProductoDetalle(productoExistente);
                detalle.setCantidadDetalle(productoCarrito.getCantidadCarrito());
                detalle.setPrecioDetalle(productoExistente.getPrecioProducto());
                detalle.setSubtotalDetalle(productoCarrito.getCantidadCarrito() * productoExistente.getPrecioProducto());
                detalle.setCompraDetalle(compra);
                detalles.add(detalle);
                totalCompra += detalle.getSubtotalDetalle();
                productoRepository.save(productoExistente);
                int nuevoStock = productoExistente.getStockProducto() - productoCarrito.getCantidadCarrito();
                productoExistente.setStockProducto(nuevoStock);
                productoRepository.save(productoExistente);
                if (nuevoStock < 5) {
                    productosBajoStock.add(productoExistente);
                }
                MovimientoInventario movimiento = new MovimientoInventario();
                movimiento.setProducto(productoExistente);
                movimiento.setCantidad(productoCarrito.getCantidadCarrito());
                movimiento.setTipoMovimiento(TipoMovimiento.SALIDA);
                movimiento.setFechaMovimiento(LocalDateTime.now());
                movimiento.setObservacion("Venta realizada");
                movimientoInventarioRepository.save(movimiento);
            }
            compra.setPrecioTotalCompra(totalCompra);
            compraService.crearCompra(compra, detalles);
            String correoAdministrador = usuarioService.obtenerCorreoAdministrador();
            if (correoAdministrador != null) {
                compraService.enviarCorreoNuevoPedido(correoAdministrador, compra, detalles);
            }
            if (!productosBajoStock.isEmpty() && correoAdministrador != null) {
                compraService.enviarCorreoBajoStock(correoAdministrador, productosBajoStock);
            }
            redirectAttributes.addFlashAttribute("mensajeExito", "Tu pedido ha sido creado.");
            redirectAttributes.addFlashAttribute("compraExitosa", true);
            return "redirect:/mis-compras";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al realizar la compra: " + e.getMessage());
            e.printStackTrace();
            return "confirmarFormaPago";
        }
    }

    @GetMapping("/mis-compras")
    public String mostrarMisCompras(@RequestParam(value = "estadoCompra", required = false) Long estadoCompraId,
                                    @RequestParam(defaultValue = "0") int page,
                                    Model model, Principal principal) {
        Usuario usuario = usuarioService.buscarPorUsuario(principal.getName());
        Rol rolUsuario = usuario.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        Pageable pageable = PageRequest.of(page, 5);
        Page<Compra> comprasUsuario;
        if (estadoCompraId != null) {
            Estado estado = estadoService.obtenerPorId(estadoCompraId);
            comprasUsuario = compraRepository.findByUsuarioCompraAndEstadoCompra(usuario, estado, pageable);
        } else {
            comprasUsuario = compraRepository.findByUsuarioCompra(usuario, pageable);
        }
        model.addAttribute("compras", comprasUsuario.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", comprasUsuario.getTotalPages());
        List<Long> idsEstadosPedidos = List.of(3L, 4L, 5L, 6L, 7L, 8L);
        List<Estado> estados = estadoService.listarEstadosParaPedidos(idsEstadosPedidos);
        model.addAttribute("estados", estados);
        model.addAttribute("estadoSeleccionado", estadoCompraId);
        return "misCompras";
    }

    @GetMapping("/mis-compras/detalle/{idCompra}")
    public String verDetalleCompra(@PathVariable Long idCompra, Model model, Principal principal) {
        Usuario usuario = usuarioService.buscarPorUsuario(principal.getName());
        model.addAttribute("usuario", new Usuario());
        Compra compra = compraRepository.findById(idCompra)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));
        if (!compra.getUsuarioCompra().equals(usuario)) {
            throw new RuntimeException("No tienes permiso para ver los detalles de esta compra");
        }
        List<DetalleCompra> detalles = detalleCompraRepository.findByCompraDetalle(compra);
        model.addAttribute("compra", compra);
        model.addAttribute("detalles", detalles);
        return "fragmentos/detalleCompraModal";
    }

    @GetMapping("/pedidos")
    public String mostrarPedidos(@RequestParam(value = "estadoCompra", required = false) Long estadoCompraId,
                                 @RequestParam(defaultValue = "0") int page,
                                 Model model, Principal principal) {
        Usuario usuario = usuarioService.buscarPorUsuario(principal.getName());
        Rol rolUsuario = usuario.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        Pageable pageable = PageRequest.of(page, 5);
        Page<Compra> compras;
        if (estadoCompraId != null) {
            Estado estado = estadoService.obtenerPorId(estadoCompraId);
            compras = compraRepository.findByEstadoCompra(estado, pageable);
        } else {
            compras = compraRepository.findAll(pageable);
        }
        model.addAttribute("compras", compras.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", compras.getTotalPages());
        List<Long> idsEstadosPedidos = List.of(3L, 4L, 5L, 6L, 7L, 8L);
        List<Estado> estados = estadoService.listarEstadosParaPedidos(idsEstadosPedidos);
        model.addAttribute("estados", estados);
        model.addAttribute("estadoSeleccionado", estadoCompraId);
        return "pedidos";
    }

    @GetMapping("/pedidos/detalle/{idCompra}")
    public String verDetallePedidos(@PathVariable Long idCompra, Model model) {
        Compra compra = compraRepository.findById(idCompra)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));
        List<DetalleCompra> detalles = detalleCompraRepository.findByCompraDetalle(compra);
        model.addAttribute("compra", compra);
        model.addAttribute("detalles", detalles);
        return "fragmentos/detalleCompraModal";
    }

    @GetMapping("/editar-pedido/{id}")
    public String mostrarFormularioEdicionPedido(@PathVariable Long id, Model model, Principal principal) {
        Compra compra = compraService.obtenerCompraPorId(id);
        List<Long> idsEstadosPedidos = List.of(3L, 4L, 5L, 6L, 7L, 8L);
        List<Estado> estados = estadoService.listarEstadosParaPedidos(idsEstadosPedidos);
        Usuario usuariom = usuarioService.buscarPorUsuario(principal.getName());
        Rol rolUsuario = usuariom.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        model.addAttribute("pedido", compra);
        model.addAttribute("estados", estados);
        return "editarPedido";
    }

    @PostMapping("/editar-pedido")
    public String actualizarPedido(@ModelAttribute("pedido") Compra compra, RedirectAttributes redirectAttributes) {
        try {
            Compra compraExistente = compraService.obtenerCompraPorId(compra.getIdCompra());
            Usuario usuarioCompra = compraExistente.getUsuarioCompra();
            String correoUsuario = usuarioCompra.getCorreoUsuario();
            String nombreUsuario = usuarioCompra.getUsuarioUsuario();
            Estado nuevoEstado = estadoService.obtenerPorId(compra.getEstadoCompra().getIdEstado());
            String estadoPedido = (nuevoEstado != null) ? nuevoEstado.getNombreEstado() : "Desconocido";
            String observacion = compra.getObservacionCompra();
            compraExistente.setEstadoCompra(nuevoEstado);
            compraExistente.setObservacionCompra(compra.getObservacionCompra());
            compraService.actualizarCompra(compraExistente);
            compraService.enviarCorreoCambioEstado(correoUsuario, nombreUsuario, estadoPedido, observacion);
            redirectAttributes.addFlashAttribute("mensajeExito", "Pedido actualizado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al actualizar el pedido.");
        }
        return "redirect:/pedidos";
    }

    @GetMapping("/ver-comprobante/{idCompra}")
    @ResponseBody
    public ResponseEntity<String> verComprobante(@PathVariable Long idCompra) {
        Compra compra = compraRepository.findById(idCompra)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));
        if (compra.getImagenComprobanteCompra() == null) {
            return ResponseEntity.badRequest().body("El comprobante no está disponible para esta compra.");
        }
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(accessKey, secretKey)))
                .build();
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(
                bucketName, compra.getImagenComprobanteCompra())
                .withMethod(HttpMethod.GET)
                .withExpiration(new Date(System.currentTimeMillis() + 3600 * 1000));
        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
        return ResponseEntity.ok(url.toString());
    }

    @GetMapping("/descargar-comprobante/{idCompra}")
    public ResponseEntity<byte[]> descargarComprobante(@PathVariable Long idCompra) throws IOException {
        Compra compra = compraRepository.findById(idCompra)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));
        if (compra.getImagenComprobanteCompra() == null) {
            throw new RuntimeException("El comprobante no está disponible para esta compra.");
        }
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(accessKey, secretKey)))
                .build();
        S3Object s3Object = s3Client.getObject(bucketName, compra.getImagenComprobanteCompra());
        byte[] contenido = IOUtils.toByteArray(s3Object.getObjectContent());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentDispositionFormData("attachment", "Comprobante.jpg");
        return new ResponseEntity<>(contenido, headers, HttpStatus.OK);
    }

    @GetMapping("/reseña-pedido/{id}")
    public String mostrarFormularioReseñaPedido(@PathVariable Long id, Model model, Principal principal) {
        Compra compra = compraService.obtenerCompraPorId(id);
        Usuario usuariom = usuarioService.buscarPorUsuario(principal.getName());
        Rol rolUsuario = usuariom.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        model.addAttribute("pedido", compra);
        return "reseñaPedido";
    }

    @PostMapping("/reseña-pedido")
    public String reseñaPedido(@ModelAttribute("pedido") Compra compra, RedirectAttributes redirectAttributes) {
        try {
            Compra compraExistente = compraService.obtenerCompraPorId(compra.getIdCompra());
            compraExistente.setCalificacionCompra(compra.getCalificacionCompra());
            compraExistente.setReseñaCompra(compra.getReseñaCompra());
            compraService.actualizarCompra(compraExistente);
            String correoAdministrador = usuarioService.obtenerCorreoAdministrador();
            if (correoAdministrador != null) {
                compraService.enviarCorreoReseñaPedido(correoAdministrador, compraExistente.getIdCompra(), compraExistente.getCalificacionCompra(), compraExistente.getReseñaCompra());
            }
            redirectAttributes.addFlashAttribute("mensajeExito", "Reseña actualizada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al actualizar la reseña.");
        }
        return "redirect:/mis-compras";
    }

    @GetMapping("/pedidos/cliente/{idCompra}")
    public String verDatosCliente(@PathVariable Long idCompra, Model model) {
        Compra compra = compraRepository.findById(idCompra)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));
        Usuario usuario = compra.getUsuarioCompra();
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado para la compra ID: " + idCompra);
        }
        model.addAttribute("usuario", usuario);
        return "fragmentos/datosClienteModal";
    }
}