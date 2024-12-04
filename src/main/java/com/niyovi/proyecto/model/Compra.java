package com.niyovi.proyecto.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "compra")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra")
    private Long idCompra;

    @ManyToOne
    @JoinColumn(name = "fk_usuario_compra")
    private Usuario usuarioCompra;

    @Column(name = "fecha_creacion_compra")
    private LocalDateTime fechaCreacionCompra;

    @Column(name = "hora_creacion_compra")
    private LocalDate horaCreacionCompra;

    @Column(name = "precio_total_compra")
    private Double precioTotalCompra;

    @ManyToOne
    @JoinColumn(name = "fk_metodo_entrega_compra")
    private MetodoEntrega metodoEntregaCompra;

    @ManyToOne
    @JoinColumn(name = "fk_forma_pago_compra")
    private FormaPago formaPagoCompra;

    @Column(name = "imagen_comprobante_compra")
    private String imagenComprobanteCompra;

    @ManyToOne
    @JoinColumn(name = "fk_estado_compra")
    private Estado estadoCompra;

    public Compra() {
    }

    public Long getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(Long idCompra) {
        this.idCompra = idCompra;
    }

    public Usuario getUsuarioCompra() {
        return usuarioCompra;
    }

    public void setUsuarioCompra(Usuario usuarioCompra) {
        this.usuarioCompra = usuarioCompra;
    }

    public LocalDateTime getFechaCreacionCompra() {
        return fechaCreacionCompra;
    }

    public void setFechaCreacionCompra(LocalDateTime fechaCreacionCompra) {
        this.fechaCreacionCompra = fechaCreacionCompra;
    }

    public LocalDate getHoraCreacionCompra() {
        return horaCreacionCompra;
    }

    public void setHoraCreacionCompra(LocalDate horaCreacionCompra) {
        this.horaCreacionCompra = horaCreacionCompra;
    }

    public Double getPrecioTotalCompra() {
        return precioTotalCompra;
    }

    public void setPrecioTotalCompra(Double precioTotalCompra) {
        this.precioTotalCompra = precioTotalCompra;
    }

    public MetodoEntrega getMetodoEntregaCompra() {
        return metodoEntregaCompra;
    }

    public void setMetodoEntregaCompra(MetodoEntrega metodoEntregaCompra) {
        this.metodoEntregaCompra = metodoEntregaCompra;
    }

    public FormaPago getFormaPagoCompra() {
        return formaPagoCompra;
    }

    public void setFormaPagoCompra(FormaPago formaPagoCompra) {
        this.formaPagoCompra = formaPagoCompra;
    }

    public String getImagenComprobanteCompra() {
        return imagenComprobanteCompra;
    }

    public void setImagenComprobanteCompra(String imagenComprobanteCompra) {
        this.imagenComprobanteCompra = imagenComprobanteCompra;
    }

    public Estado getEstadoCompra() {
        return estadoCompra;
    }

    public void setEstadoCompra(Estado estadoCompra) {
        this.estadoCompra = estadoCompra;
    }
}
