package com.niyovi.proyecto.repository;

import com.niyovi.proyecto.model.Estado;
import com.niyovi.proyecto.model.MetodoEntrega;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetodoEntregaRepository extends JpaRepository<MetodoEntrega, Long> {

    List<MetodoEntrega> findByEstadoMetodoEntrega(Estado estadoMetodo);
}
