package com.niyovi.proyecto.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long idCategoria;

    @Column(name = "nombre_categoria")
    private String nombreCategoria;

    @ManyToOne
    @JoinColumn(name = "fk_estado_categoria")
    private Estado estadoCategoria;

    public Categoria() {
    }

    public Long getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Long idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public Estado getEstadoCategoria() {
        return estadoCategoria;
    }

    public void setEstadoCategoria(Estado estadoCategoria) {
        this.estadoCategoria = estadoCategoria;
    }
}
