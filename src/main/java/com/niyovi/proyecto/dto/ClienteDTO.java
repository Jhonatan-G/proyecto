package com.niyovi.proyecto.dto;

import java.util.Objects;

public class ClienteDTO {

    private String nombre;
    private String apellido;
    private String celular;

    public ClienteDTO(String nombre, String apellido, String celular) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.celular = celular;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClienteDTO that = (ClienteDTO) obj;
        return Objects.equals(nombre, that.nombre) &&
                Objects.equals(apellido, that.apellido) &&
                Objects.equals(celular, that.celular);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, apellido, celular);
    }
}
