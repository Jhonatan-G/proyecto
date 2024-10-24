package com.niyovi.proyecto.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tipo_documento")
public class TipoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_doc")
    private Long idTipoDoc;

    @Column(name = "nombre_tipo_doc")
    private String nombreTipoDoc;

    @ManyToOne
    @JoinColumn(name = "fk_estado_tipo_doc")
    private Estado estadoTipoDoc;

    public TipoDocumento() {
    }

    public Long getIdTipoDoc() {
        return idTipoDoc;
    }

    public void setIdTipoDoc(Long idTipoDoc) {
        this.idTipoDoc = idTipoDoc;
    }

    public String getNombreTipoDoc() {
        return nombreTipoDoc;
    }

    public void setNombreTipoDoc(String nombreTipoDoc) {
        this.nombreTipoDoc = nombreTipoDoc;
    }

    public Estado getEstadoTipoDoc() {
        return estadoTipoDoc;
    }

    public void setEstadoTipoDoc(Estado estadoTipoDoc) {
        this.estadoTipoDoc = estadoTipoDoc;
    }
}
