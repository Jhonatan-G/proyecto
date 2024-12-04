package com.niyovi.proyecto.model;

import jakarta.persistence.*;

@Entity
@Table(name = "detalle_compra")
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_compra")
    private Long idDetalleCompra;

    @ManyToOne
    @JoinColumn(name = "fk_compra")
    private Compra compraDetalle;

    @ManyToOne
    @JoinColumn(name = "fk_producto")
    private Producto productoDetalle;

    @Column(name = "subtotal_detalle")
    private Double subtotalDetalle;

    public DetalleCompra() {
    }

    public Long getIdDetalleCompra() {
        return idDetalleCompra;
    }

    public void setIdDetalleCompra(Long idDetalleCompra) {
        this.idDetalleCompra = idDetalleCompra;
    }

    public Compra getCompraDetalle() {
        return compraDetalle;
    }

    public void setCompraDetalle(Compra compraDetalle) {
        this.compraDetalle = compraDetalle;
    }

    public Producto getProductoDetalle() {
        return productoDetalle;
    }

    public void setProductoDetalle(Producto productoDetalle) {
        this.productoDetalle = productoDetalle;
    }

    public Double getSubtotalDetalle() {
        return subtotalDetalle;
    }

    public void setSubtotalDetalle(Double subtotalDetalle) {
        this.subtotalDetalle = subtotalDetalle;
    }
}
