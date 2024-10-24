package com.niyovi.proyecto.model;

import jakarta.persistence.*;

@Entity
@Table(name = "detalle_carrito")
public class DetalleCarrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_carrito")
    private Long idDetalleCarrito;

    @ManyToOne
    @JoinColumn(name = "fk_id_carrito_detalle")
    private Carrito idCarritoDetalle;

    @ManyToOne
    @JoinColumn(name = "fk_id_producto_detalle")
    private Producto idProductoDetalle;

    @Column(name = "cantidad_detalle")
    private int cantidadDetalle;

    public DetalleCarrito() {
    }

    public Long getIdDetalleCarrito() {
        return idDetalleCarrito;
    }

    public void setIdDetalleCarrito(Long idDetalleCarrito) {
        this.idDetalleCarrito = idDetalleCarrito;
    }

    public Carrito getIdCarritoDetalle() {
        return idCarritoDetalle;
    }

    public void setIdCarritoDetalle(Carrito idCarritoDetalle) {
        this.idCarritoDetalle = idCarritoDetalle;
    }

    public Producto getIdProductoDetalle() {
        return idProductoDetalle;
    }

    public void setIdProductoDetalle(Producto idProductoDetalle) {
        this.idProductoDetalle = idProductoDetalle;
    }

    public int getCantidadDetalle() {
        return cantidadDetalle;
    }

    public void setCantidadDetalle(int cantidadDetalle) {
        this.cantidadDetalle = cantidadDetalle;
    }
}
