package com.niyovi.proyecto.dto;

public class ProductoDTO {

    private Long idProducto;
    private Integer cantidadCarrito;

    public ProductoDTO() {
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getCantidadCarrito() {
        return cantidadCarrito;
    }

    public void setCantidadCarrito(Integer cantidadCarrito) {
        this.cantidadCarrito = cantidadCarrito;
    }
}
