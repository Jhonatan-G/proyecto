package com.niyovi.proyecto.repository;

import com.niyovi.proyecto.model.Estado;
import com.niyovi.proyecto.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolRepository extends JpaRepository<Rol, Long> {

    List<Rol> findByEstadoRol(Estado estadoRol);
}
