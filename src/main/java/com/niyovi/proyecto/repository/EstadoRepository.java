package com.niyovi.proyecto.repository;

import com.niyovi.proyecto.model.Estado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EstadoRepository extends JpaRepository<Estado, Long> {

    List<Estado> findByNombreEstado(String nombreEstado);

    List<Estado> findByIdEstadoIn(List<Long> ids);
}
