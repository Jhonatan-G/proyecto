package com.niyovi.proyecto.service;

import com.niyovi.proyecto.model.*;
import com.niyovi.proyecto.repository.CompraRepository;
import com.niyovi.proyecto.repository.DetalleCompraRepository;
import com.niyovi.proyecto.repository.EstadoRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private DetalleCompraRepository detalleCompraRepository;

    @Autowired
    private EstadoRepository estadoRepository;

    private final JavaMailSender javaMailSender;

    public CompraService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public Compra crearCompra(Compra compra, List<DetalleCompra> detalles) {
        compra.setFechaCreacionCompra(LocalDateTime.now());
        Compra compraGuardada = compraRepository.save(compra);
        for (DetalleCompra detalle : detalles) {
            detalle.setCompraDetalle(compraGuardada);
            detalleCompraRepository.save(detalle);
        }
        return compraGuardada;
    }

    public Compra obtenerCompraPorId(Long id) {
        return compraRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
    }

    public List<Compra> filtrarCompras(Long estadoPedidoId) {
        Estado estado = null;
        if (estadoPedidoId != null) {
            estado = estadoRepository.findById(estadoPedidoId)
                    .orElse(null);
        }
        if (estado != null) {
            return compraRepository.findByEstadoCompra(estado);
        } else {
            return compraRepository.findAll();
        }
    }

    public void actualizarCompra(Compra compra) {
        compraRepository.save(compra);
    }

    public void enviarCorreoCambioEstado(String correoUsuario, String nombreUsuario, String estadoPedido, String observacion) {
        try {
            MimeMessage mensaje = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setTo(correoUsuario);
            helper.setSubject("Actualización del estado de su pedido");
            String contenidoHtml = String.format("""
                    <div style="font-family: Arial, sans-serif; color: #333; padding: 20px; border: 1px solid #ddd; border-radius: 10px; max-width: 600px; margin: auto; background: #fff;">
                        <div style="text-align: center; padding-bottom: 20px;">
                            <h1 style="color: #D35400; margin: 0;">Alimentos Niyovi SAS</h1>
                        </div>
                        <hr style="border: 0; height: 1px; background: #D35400; margin-bottom: 20px;">
                        <h2 style="color: #D35400; text-align: center;">Actualización del Estado de su Pedido</h2>
                        <p>Estimado <strong>%s</strong>,</p>
                        <p>Queremos informarle que el estado de su pedido ha cambiado a:</p>
                        <p style="font-size: 20px; font-weight: bold; background: #f4f4f4; padding: 12px; text-align: center; border-radius: 5px; display: inline-block;">
                            %s
                        </p>
                        <p><strong>Observación:</strong></p>
                        <p style="background: #f9f9f9; padding: 10px; border-radius: 5px;">%s</p>
                        <p>Gracias por su compra. Si tiene alguna pregunta, no dude en contactarnos.</p>
                        <br>
                        <hr style="border: 0; height: 1px; background: #ddd; margin-bottom: 20px;">
                        <p style="text-align: center; font-size: 12px; color: #777;">Alimentos Niyovi SAS</p>
                        <p style="text-align: center; font-size: 12px; color: #777;">Nit: 9014298845</p>
                        <p style="text-align: center; font-size: 12px; color: #777;">Finca El Triunfo Km 16 Vía Apulo Naranjalito, Apulo, Cundinamarca</p>
                    </div>
                    """, nombreUsuario, estadoPedido, observacion);
            helper.setText(contenidoHtml, true);
            javaMailSender.send(mensaje);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo enviar la notificación por correo.");
        }
    }

    public void enviarCorreoNuevoPedido(String correoAdministrador, Compra compra, List<DetalleCompra> detalles) {
        try {
            MimeMessage mensaje = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setTo(correoAdministrador);
            helper.setSubject("Nuevo pedido recibido");
            StringBuilder detallesPedido = new StringBuilder();
            for (DetalleCompra detalle : detalles) {
                detallesPedido.append(String.format("""
                                <tr>
                                    <td style="border: 1px solid #ddd; padding: 10px; text-align: center;">%s</td>
                                    <td style="border: 1px solid #ddd; padding: 10px; text-align: center;">%d</td>
                                    <td style="border: 1px solid #ddd; padding: 10px; text-align: center;">$%.2f</td>
                                </tr>
                                """,
                        detalle.getProductoDetalle().getNombreProducto(),
                        detalle.getCantidadDetalle(),
                        detalle.getSubtotalDetalle()));
            }
            String contenidoHtml = String.format("""
                            <div style="font-family: Arial, sans-serif; color: #333; padding: 20px; border: 1px solid #ddd; border-radius: 10px; max-width: 600px; margin: auto; background: #fff;">
                                <div style="text-align: center; padding-bottom: 20px;">
                                    <h1 style="color: #D35400; margin: 0;">Alimentos Niyovi SAS</h1>
                                </div>
                                <hr style="border: 0; height: 1px; background: #D35400; margin-bottom: 20px;">
                                <h2 style="color: #D35400; text-align: center;">Nuevo Pedido Recibido</h2>
                                <p>Se ha recibido un nuevo pedido con la siguiente información:</p>
                                <p><strong>Cliente:</strong> %s</p>
                                <p><strong>Total:</strong> $%.2f</p>
                                <p><strong>Estado inicial:</strong> %s</p>
                                <h3 style="color: #D35400;">Detalles del Pedido:</h3>
                                <table style="width: 100%%; border-collapse: collapse;">
                                    <tr>
                                        <th style="border: 1px solid #ddd; padding: 10px; background: #D35400; color: #fff;">Producto</th>
                                        <th style="border: 1px solid #ddd; padding: 10px; background: #D35400; color: #fff;">Cantidad</th>
                                        <th style="border: 1px solid #ddd; padding: 10px; background: #D35400; color: #fff;">Subtotal</th>
                                    </tr>
                                    %s
                                </table>
                                <p>Por favor, revise el sistema para más detalles.</p>
                                <br>
                                <hr style="border: 0; height: 1px; background: #ddd; margin-bottom: 20px;">
                                <p style="text-align: center; font-size: 12px; color: #777;">Alimentos Niyovi SAS</p>
                                <p style="text-align: center; font-size: 12px; color: #777;">Nit: 9014298845</p>
                                <p style="text-align: center; font-size: 12px; color: #777;">Finca El Triunfo Km 16 Vía Apulo Naranjalito, Apulo, Cundinamarca</p>
                            </div>
                            """,
                    compra.getUsuarioCompra().getUsuarioUsuario(),
                    compra.getPrecioTotalCompra(),
                    compra.getEstadoCompra().getNombreEstado(),
                    detallesPedido.toString());
            helper.setText(contenidoHtml, true);
            javaMailSender.send(mensaje);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo enviar la notificación por correo.");
        }
    }

    public void enviarCorreoReseñaPedido(String correoAdministrador, Long idPedido, int calificacion, String reseña) {
        try {
            MimeMessage mensaje = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setTo(correoAdministrador);
            helper.setSubject("Nueva reseña agregada a un pedido");
            String estrellas = "★".repeat(calificacion) + "☆".repeat(5 - calificacion);
            String contenidoHtml = String.format("""
                    <div style="font-family: Arial, sans-serif; color: #333; padding: 20px; border: 1px solid #ddd; border-radius: 10px; max-width: 600px; margin: auto; background: #fff;">
                        <div style="text-align: center; padding-bottom: 20px;">
                            <h1 style="color: #D35400; margin: 0;">Alimentos Niyovi SAS</h1>
                        </div>
                        <hr style="border: 0; height: 1px; background: #D35400; margin-bottom: 20px;">
                        <h2 style="color: #D35400; text-align: center;">Nueva Reseña de Pedido</h2>
                        <p>Se ha agregado una nueva reseña al pedido con ID:</p>
                        <p style="font-size: 20px; font-weight: bold; background: #f4f4f4; padding: 12px; text-align: center; border-radius: 5px; display: inline-block;">%d</p>
                        <h3 style="color: #D35400;">Calificación:</h3>
                        <p style="font-size: 22px; text-align: center; margin: 10px 0;">%s</p>                   
                        <h3 style="color: #D35400;">Reseña:</h3>
                        <p style="border-left: 5px solid #D35400; padding-left: 10px; font-style: italic;">"%s"</p>                   
                        <p>Por favor, revisa el sistema para más detalles.</p>
                        <br>
                        <hr style="border: 0; height: 1px; background: #ddd; margin-bottom: 20px;">
                        <p style="text-align: center; font-size: 12px; color: #777;">Alimentos Niyovi SAS</p>
                        <p style="text-align: center; font-size: 12px; color: #777;">Nit: 9014298845</p>
                        <p style="text-align: center; font-size: 12px; color: #777;">Finca El Triunfo Km 16 Vía Apulo Naranjalito, Apulo, Cundinamarca</p>
                    </div>
                    """, idPedido, estrellas, reseña);
            helper.setText(contenidoHtml, true);
            javaMailSender.send(mensaje);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo enviar la notificación por correo.");
        }
    }

    public void enviarCorreoBajoStock(String correoAdministrador, List<Producto> productosBajoStock) {
        try {
            MimeMessage mensaje = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setTo(correoAdministrador);
            helper.setSubject("⚠️ Alerta: Productos con Bajo Stock");
            StringBuilder productosHtml = new StringBuilder();
            for (Producto producto : productosBajoStock) {
                productosHtml.append(String.format("""
                            <li style="margin-bottom: 5px;">
                                <strong>%s</strong> - Stock actual: <span style="color: red; font-weight: bold;">%d</span>
                            </li>
                        """, producto.getNombreProducto(), producto.getStockProducto()));
            }
            String contenidoHtml = String.format("""
                    <div style="font-family: Arial, sans-serif; color: #333; padding: 20px; border: 1px solid #ddd; border-radius: 10px; max-width: 600px; margin: auto; background: #fff;">
                        <div style="text-align: center; padding-bottom: 20px;">
                            <h1 style="color: #D35400; margin: 0;">Alimentos Niyovi SAS</h1>
                        </div>
                        <hr style="border: 0; height: 1px; background: #D35400; margin-bottom: 20px;">
                        <h2 style="color: #D35400; text-align: center;">⚠️ Alerta: Productos con Bajo Stock</h2>
                        <p>Los siguientes productos tienen un stock bajo y requieren reposición:</p>
                        <ul style="background: #f8f8f8; padding: 15px; border-radius: 5px; list-style: none;">
                            %s
                        </ul>
                        <p>Por favor, revise el inventario y tome las medidas necesarias.</p>
                        <br>
                        <hr style="border: 0; height: 1px; background: #ddd; margin-bottom: 20px;">
                        <p style="text-align: center; font-size: 12px; color: #777;">Alimentos Niyovi SAS</p>
                        <p style="text-align: center; font-size: 12px; color: #777;">Nit: 9014298845</p>
                        <p style="text-align: center; font-size: 12px; color: #777;">Finca El Triunfo Km 16 Vía Apulo Naranjalito, Apulo, Cundinamarca</p>
                    </div>
                    """, productosHtml.toString());
            helper.setText(contenidoHtml, true);
            javaMailSender.send(mensaje);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo enviar la notificación de bajo stock.");
        }
    }

    public List<Compra> obtenerReseñasClientes() {
        return compraRepository.findByCalificacionCompraInOrderByIdCompraDesc(List.of(3, 4, 5));
    }
}