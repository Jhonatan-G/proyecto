package com.niyovi.proyecto.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long idRol;

    @Column(name = "nombre_rol")
    private String nombreRol;

    @ManyToOne
    @JoinColumn(name = "fk_estado_rol")
    private Estado estadoRol;

    public Rol() {
    }

    public Long getIdRol() {
        return idRol;
    }

    public void setIdRol(Long idRol) {
        this.idRol = idRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    public Estado getEstadoRol() {
        return estadoRol;
    }

    public void setEstadoRol(Estado estadoRol) {
        this.estadoRol = estadoRol;
    }
}
