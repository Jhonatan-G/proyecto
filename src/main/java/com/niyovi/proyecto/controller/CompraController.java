package com.niyovi.proyecto.controller;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;
import com.niyovi.proyecto.model.*;
import com.niyovi.proyecto.repository.*;
import com.niyovi.proyecto.service.*;
import jakarta.servlet.http.HttpSession;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import java.io.File;
import java.net.URL;
import java.security.Principal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
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
    private DetalleCompraService detalleCompraService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private DetalleCompraRepository detalleCompraRepository;

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
            Estado estadoPendiente = estadoRepository.findById(3L)
                    .orElseThrow(() -> new RuntimeException("Estado 'recibido' no encontrado"));
            compra.setUsuarioCompra(usuario);
            compra.setMetodoEntregaCompra(metodoEntregaSeleccionado);
            compra.setFormaPagoCompra(formaPago);
            compra.setEstadoCompra(estadoPendiente);
            List<DetalleCompra> detalles = new ArrayList<>();
            double totalCompra = 0.0;
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
            }
            compra.setPrecioTotalCompra(totalCompra);
            compraService.crearCompra(compra, detalles);
            String correoAdministrador = usuarioService.obtenerCorreoAdministrador();
            if (correoAdministrador != null) {
                compraService.enviarCorreoNuevoPedido(correoAdministrador, compra, detalles);
            }
            redirectAttributes.addFlashAttribute("mensajeExito", "Compra realizada exitosamente.");
            redirectAttributes.addFlashAttribute("compraExitosa", true);
            return "redirect:/mis-compras";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al realizar la compra: " + e.getMessage());
            e.printStackTrace();
            return "confirmarFormaPago";
        }
    }

    @GetMapping("/mis-compras")
    public String mostrarMisCompras(@RequestParam(value = "estadoCompra", required = false) Long estadoCompraId, Model model, Principal principal) {
        Usuario usuario = usuarioService.buscarPorUsuario(principal.getName());
        model.addAttribute("usuario", new Usuario());
        Rol rolUsuario = usuario.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        List<Compra> compras = compraRepository.findByUsuarioCompra(usuario);
        model.addAttribute("compras", compras);
        List<Long> idsEstadosPedidos = List.of(3L, 4L, 5L, 6L);
        List<Estado> estados = estadoService.listarEstadosParaPedidos(idsEstadosPedidos);
        List<Compra> compraf = compraService.filtrarCompras(estadoCompraId);
        model.addAttribute("compras", compraf);
        model.addAttribute("estados", estados);
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
    public String mostrarPedidos(@RequestParam(value = "estadoCompra", required = false) Long estadoCompraId, Model model, Principal principal) {
        Usuario usuario = usuarioService.buscarPorUsuario(principal.getName());
        Rol rolUsuario = usuario.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        List<Long> idsEstadosPedidos = List.of(3L, 4L, 5L, 6L);
        List<Estado> estados = estadoService.listarEstadosParaPedidos(idsEstadosPedidos);
        List<Compra> compras = compraRepository.findAll();
        model.addAttribute("compras", compras);
        List<Compra> compraf = compraService.filtrarCompras(estadoCompraId);
        model.addAttribute("compras", compraf);
        model.addAttribute("estados", estados);
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
        List<Long> idsEstadosPedidos = List.of(3L, 4L, 5L, 6L);
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

    @GetMapping("/descargar-factura/{id}")
    public void generarFactura(@PathVariable("id") Long idCompra, HttpServletResponse response) {
        try {
            Compra compra = compraRepository.findById(idCompra)
                    .orElseThrow(() -> new RuntimeException("Compra no encontrada"));
            List<DetalleCompra> detalles = detalleCompraRepository.findByCompraDetalle(compra);
            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();
            try {
                Image logo = Image.getInstance("src/main/resources/static/logo.png");
                logo.scaleToFit(150, 150);
                logo.setAlignment(Element.ALIGN_CENTER);
                document.add(logo);
            } catch (Exception e) {
                System.err.println("No se pudo cargar el logo: " + e.getMessage());
            }
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Paragraph titulo = new Paragraph("FACTURA DE COMPRA", boldFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(10);
            document.add(titulo);
            document.add(new Paragraph("Alimentos Niyovi SAS", boldFont));
            document.add(new Paragraph("Nit: 1234567890"));
            document.add(new Paragraph("Finca El Triunfo Km 16 Vía Apulo Naranjalito, Apulo, Cundinamarca"));
            document.add(new Paragraph("\n"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String fechaFormateada = compra.getFechaCreacionCompra().format(formatter);
            document.add(new Paragraph("Fecha: " + fechaFormateada));
            document.add(new Paragraph("ID Compra: " + compra.getIdCompra()));
            document.add(new Paragraph("Cliente: " + compra.getUsuarioCompra().getNombresUsuario() + " " + compra.getUsuarioCompra().getApellidosUsuario()));
            document.add(new Paragraph("Tipo documento: " + compra.getUsuarioCompra().getTipoDocUsuario().getNombreTipoDoc()));
            document.add(new Paragraph("Número documento: " + compra.getUsuarioCompra().getNumeroDocUsuario()));
            document.add(new Paragraph("Dirección: " + compra.getUsuarioCompra().getDireccionUsuario()));
            document.add(new Paragraph("Teléfono: " + compra.getUsuarioCompra().getCelularUsuario()));
            document.add(new Paragraph("Correo: " + compra.getUsuarioCompra().getCorreoUsuario()));
            document.add(new Paragraph("\n"));
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setWidths(new int[]{3, 3, 4, 2, 2, 2});
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            String[] headers = {"Código", "Producto", "Descripción", "Precio Unitario", "Cantidad", "Subtotal"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }
            NumberFormat format = NumberFormat.getInstance(new Locale("es", "CO"));
            for (DetalleCompra detalle : detalles) {
                table.addCell(String.valueOf(detalle.getProductoDetalle().getIdProducto()));
                table.addCell(detalle.getProductoDetalle().getNombreProducto());
                table.addCell(detalle.getProductoDetalle().getDescripcionProducto());
                table.addCell("$" + format.format(detalle.getPrecioDetalle()));
                table.addCell(detalle.getCantidadDetalle().toString());
                table.addCell("$" + format.format(detalle.getSubtotalDetalle()));
            }
            PdfPCell totalCell = new PdfPCell(new Phrase("Total", headerFont));
            totalCell.setColspan(5);
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalCell.setPadding(5);
            table.addCell(totalCell);
            PdfPCell totalValueCell = new PdfPCell(new Phrase("$" + format.format(compra.getPrecioTotalCompra()), headerFont));
            totalValueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            totalValueCell.setPadding(5);
            table.addCell(totalValueCell);
            document.add(table);
            document.close();
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=factura_" + idCompra + ".pdf");
            response.getOutputStream().write(out.toByteArray());
            response.getOutputStream().flush();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Error al generar la factura", e);
        }
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
            compraExistente.setReseñaCompra(compra.getReseñaCompra());
            compraService.actualizarCompra(compraExistente);
            String correoAdministrador = usuarioService.obtenerCorreoAdministrador();
            if (correoAdministrador != null) {
                compraService.enviarCorreoReseñaPedido(correoAdministrador, compraExistente.getIdCompra(), compraExistente.getReseñaCompra());
            }
            redirectAttributes.addFlashAttribute("mensajeExito", "Reseña actualizada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al actualizar la reseña.");
        }
        return "redirect:/mis-compras";
    }

    @GetMapping("/reporte-ventas")
    public String mostrarReporteVentas(Model model) {
        return "reporteVentas";
    }

    @GetMapping("/reporte-ventas/generar")
    public String generarReporteVentas(
            @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Model model) {
        if (fechaInicio == null) {
            fechaInicio = LocalDate.now().minusMonths(12).withDayOfMonth(1);
        }
        if (fechaFin == null) {
            fechaFin = LocalDate.now();
        }
        List<Compra> compras = compraRepository.findByFechaCreacionCompraBetween(
                fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59));
        long totalVentas = compras.size();
        double ingresosTotales = compras.stream().mapToDouble(Compra::getPrecioTotalCompra).sum();
        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("ingresosTotales", ingresosTotales);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        return "reporteVentas";
    }
}