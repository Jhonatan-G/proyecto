package com.niyovi.proyecto.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @ManyToOne
    @JoinColumn(name = "fk_tipo_doc_usuario")
    private TipoDocumento tipoDocUsuario;

    @Column(name = "numero_doc_usuario")
    private String numeroDocUsuario;

    @Column(name = "nombres_usuario")
    private String nombresUsuario;

    @Column(name = "apellidos_usuario")
    private String apellidosUsuario;

    @Column(name = "celular_usuario")
    private String celularUsuario;

    @Column(name = "direccion_usuario")
    private String direccionUsuario;

    @Column(name = "correo_usuario")
    private String correoUsuario;

    @Column(name = "usuario_usuario")
    private String usuarioUsuario;

    @Column(name = "clave_usuario")
    private String claveUsuario;

    @ManyToOne
    @JoinColumn(name = "fk_estado_usuario")
    private Estado estadoUsuario;

    @ManyToOne
    @JoinColumn(name = "fk_rol_usuario")
    private Rol rolUsuario;

    @Transient
    private String nuevaClave;

    @Transient
    private String confirmarClave;

    public Usuario() {
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public TipoDocumento getTipoDocUsuario() {
        return tipoDocUsuario;
    }

    public void setTipoDocUsuario(TipoDocumento tipoDocUsuario) {
        this.tipoDocUsuario = tipoDocUsuario;
    }

    public String getNumeroDocUsuario() {
        return numeroDocUsuario;
    }

    public void setNumeroDocUsuario(String numeroDocUsuario) {
        this.numeroDocUsuario = numeroDocUsuario;
    }

    public String getNombresUsuario() {
        return nombresUsuario;
    }

    public void setNombresUsuario(String nombresUsuario) {
        this.nombresUsuario = nombresUsuario;
    }

    public String getApellidosUsuario() {
        return apellidosUsuario;
    }

    public void setApellidosUsuario(String apellidosUsuario) {
        this.apellidosUsuario = apellidosUsuario;
    }

    public String getCelularUsuario() {
        return celularUsuario;
    }

    public void setCelularUsuario(String celularUsuario) {
        this.celularUsuario = celularUsuario;
    }

    public String getDireccionUsuario() {
        return direccionUsuario;
    }

    public void setDireccionUsuario(String direccionUsuario) {
        this.direccionUsuario = direccionUsuario;
    }

    public String getCorreoUsuario() {
        return correoUsuario;
    }

    public void setCorreoUsuario(String correoUsuario) {
        this.correoUsuario = correoUsuario;
    }

    public String getUsuarioUsuario() {
        return usuarioUsuario;
    }

    public void setUsuarioUsuario(String usuarioUsuario) {
        this.usuarioUsuario = usuarioUsuario;
    }

    public String getClaveUsuario() {
        return claveUsuario;
    }

    public void setClaveUsuario(String claveUsuario) {
        this.claveUsuario = claveUsuario;
    }

    public Estado getEstadoUsuario() {
        return estadoUsuario;
    }

    public void setEstadoUsuario(Estado estadoUsuario) {
        this.estadoUsuario = estadoUsuario;
    }

    public Rol getRolUsuario() {
        return rolUsuario;
    }

    public void setRolUsuario(Rol rolUsuario) {
        this.rolUsuario = rolUsuario;
    }

    public String getNuevaClave() {
        return nuevaClave;
    }

    public void setNuevaClave(String nuevaClave) {
        this.nuevaClave = nuevaClave;
    }

    public String getConfirmarClave() {
        return confirmarClave;
    }

    public void setConfirmarClave(String confirmarClave) {
        this.confirmarClave = confirmarClave;
    }
}
