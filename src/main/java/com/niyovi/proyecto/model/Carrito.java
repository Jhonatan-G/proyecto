package com.niyovi.proyecto.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "carrito_compras")
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrito")
    private Long idCarrito;

    @ManyToOne
    @JoinColumn(name = "fk_usuario_carrito")
    private Usuario usuarioCarrito;

    @Column(name = "fecha_creacion_carrito")
    private LocalDateTime fechaCreacionCarrito;

    public Carrito() {
    }

    public Long getIdCarrito() {
        return idCarrito;
    }

    public void setIdCarrito(Long idCarrito) {
        this.idCarrito = idCarrito;
    }

    public Usuario getUsuarioCarrito() {
        return usuarioCarrito;
    }

    public void setUsuarioCarrito(Usuario usuarioCarrito) {
        this.usuarioCarrito = usuarioCarrito;
    }

    public LocalDateTime getFechaCreacionCarrito() {
        return fechaCreacionCarrito;
    }

    public void setFechaCreacionCarrito(LocalDateTime fechaCreacionCarrito) {
        this.fechaCreacionCarrito = fechaCreacionCarrito;
    }
}
