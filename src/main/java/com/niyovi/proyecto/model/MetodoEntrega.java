package com.niyovi.proyecto.model;

import jakarta.persistence.*;

@Entity
@Table(name = "metodo_entrega")
public class MetodoEntrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metodo_entrega")
    private Long idMetodoEntrega;

    @Column(name = "nombre_metodo_entrega")
    private String nombreMetodoEntrega;

    @ManyToOne
    @JoinColumn(name = "fk_estado_metodo_entrega")
    private Estado estadoMetodoEntrega;

    public MetodoEntrega() {
    }

    public Long getIdMetodoEntrega() {
        return idMetodoEntrega;
    }

    public void setIdMetodoEntrega(Long idMetodoEntrega) {
        this.idMetodoEntrega = idMetodoEntrega;
    }

    public String getNombreMetodoEntrega() {
        return nombreMetodoEntrega;
    }

    public void setNombreMetodoEntrega(String nombreMetodoEntrega) {
        this.nombreMetodoEntrega = nombreMetodoEntrega;
    }

    public com.niyovi.proyecto.model.Estado getEstadoMetodoEntrega() {
        return estadoMetodoEntrega;
    }

    public void setEstadoMetodoEntrega(com.niyovi.proyecto.model.Estado estadoMetodoEntrega) {
        this.estadoMetodoEntrega = estadoMetodoEntrega;
    }
}
