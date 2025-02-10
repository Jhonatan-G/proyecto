package com.niyovi.proyecto.service;

import com.niyovi.proyecto.model.*;
import com.niyovi.proyecto.repository.CompraRepository;
import com.niyovi.proyecto.repository.DetalleCompraRepository;
import com.niyovi.proyecto.repository.EstadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(correoUsuario);
            mensaje.setSubject("Actualización del estado de su pedido");
            mensaje.setText("Estimado " + nombreUsuario + ",\n\n" +
                    "El estado de su pedido ha cambiado a: " + estadoPedido + ".\n\n" +
                    "Observación: " + observacion + "\n\n" +
                    "Gracias por su compra.");
            javaMailSender.send(mensaje);
        } catch (Exception e) {
            System.out.println("Error al enviar el correo: " + e.getMessage());
            throw new RuntimeException("No se pudo enviar la notificación por correo.");
        }
    }

    public void enviarCorreoNuevoPedido(String correoAdministrador, Compra compra, List<DetalleCompra> detalles) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(correoAdministrador);
            mensaje.setSubject("Nuevo pedido recibido");
            StringBuilder detallesPedido = new StringBuilder();
            detallesPedido.append("Se ha recibido un nuevo pedido.\n\n")
                    .append("Cliente: ").append(compra.getUsuarioCompra().getUsuarioUsuario()).append("\n")
                    .append("Total: $").append(compra.getPrecioTotalCompra()).append("\n")
                    .append("Estado inicial: ").append(compra.getEstadoCompra().getNombreEstado()).append("\n\n")
                    .append("Detalles del pedido:\n");
            for (DetalleCompra detalle : detalles) {
                detallesPedido.append("- Producto: ").append(detalle.getProductoDetalle().getNombreProducto())
                        .append(", Cantidad: ").append(detalle.getCantidadDetalle())
                        .append(", Subtotal: $").append(detalle.getSubtotalDetalle()).append("\n");
            }
            detallesPedido.append("\nPor favor, revise el sistema para más detalles.");
            mensaje.setText(detallesPedido.toString());
            javaMailSender.send(mensaje);
        } catch (Exception e) {
            System.out.println("Error al enviar el correo: " + e.getMessage());
            throw new RuntimeException("No se pudo enviar la notificación por correo.");
        }
    }

    public void enviarCorreoReseñaPedido(String correoAdministrador, Long idPedido, String reseña) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(correoAdministrador);
            mensaje.setSubject("Nueva reseña agregada a un pedido");
            mensaje.setText("Se ha agregado una nueva reseña al pedido con ID: " + idPedido + ".\n\n" +
                    "Reseña: " + reseña + "\n\n" +
                    "Por favor, revisa el sistema para más detalles.");
            javaMailSender.send(mensaje);
        } catch (Exception e) {
            System.out.println("Error al enviar el correo: " + e.getMessage());
            throw new RuntimeException("No se pudo enviar la notificación por correo.");
        }
    }
}