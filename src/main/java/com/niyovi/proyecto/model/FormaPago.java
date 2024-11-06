package com.niyovi.proyecto.model;


import jakarta.persistence.*;

@Entity
@Table(name = "forma_pago")
public class FormaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_forma_pago")
    private Long idFormaPago;

    @Column(name = "nombre_forma_pago")
    private String nombreFormaPago;

    @ManyToOne
    @JoinColumn(name = "fk_estado_forma_pago")
    private Estado estadoFormaPago;

    public FormaPago() {
    }

    public Long getIdFormaPago() {
        return idFormaPago;
    }

    public void setIdFormaPago(Long idFormaPago) {
        this.idFormaPago = idFormaPago;
    }

    public String getNombreFormaPago() {
        return nombreFormaPago;
    }

    public void setNombreFormaPago(String nombreFormaPago) {
        this.nombreFormaPago = nombreFormaPago;
    }

    public Estado getEstadoFormaPago() {
        return estadoFormaPago;
    }

    public void setEstadoFormaPago(Estado estadoFormaPago) {
        this.estadoFormaPago = estadoFormaPago;
    }
}
