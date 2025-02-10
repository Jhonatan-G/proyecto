package com.niyovi.proyecto.repository;

import com.niyovi.proyecto.model.Compra;
import com.niyovi.proyecto.model.Estado;
import com.niyovi.proyecto.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    List<Compra> findByUsuarioCompra(Usuario usuario);

    List<Compra> findAll();

    List<Compra> findByEstadoCompra(Estado estadoCompra);

    List<Compra> findByFechaCreacionCompraBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
