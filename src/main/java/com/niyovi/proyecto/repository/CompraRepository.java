package com.niyovi.proyecto.repository;

import com.niyovi.proyecto.model.Compra;
import com.niyovi.proyecto.model.Estado;
import com.niyovi.proyecto.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    List<Compra> findByUsuarioCompra(Usuario usuario);

    List<Compra> findAll();

    List<Compra> findByEstadoCompra(Estado estadoCompra);

    List<Compra> findByFechaCreacionCompraBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    Page<Compra> findByUsuarioCompra(Usuario usuario, Pageable pageable);

    Page<Compra> findAll(Pageable pageable);

    Page<Compra> findByEstadoCompra(Estado estadoCompra, Pageable pageable);

    Page<Compra> findByUsuarioCompraAndEstadoCompra(Usuario usuario, Estado estadoCompra, Pageable pageable);

    List<Compra> findByCalificacionCompraInOrderByIdCompraDesc(List<Integer> calificaciones);
}
