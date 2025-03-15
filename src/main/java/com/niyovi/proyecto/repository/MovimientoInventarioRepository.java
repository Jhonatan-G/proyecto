package com.niyovi.proyecto.repository;

import com.niyovi.proyecto.enums.TipoMovimiento;
import com.niyovi.proyecto.model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    List<MovimientoInventario> findByFechaMovimientoBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    List<MovimientoInventario> findByTipoMovimientoAndFechaMovimientoBetween(
            TipoMovimiento tipoMovimiento, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    List<MovimientoInventario> findByFechaMovimientoBetweenAndTipoMovimiento(
            LocalDateTime fechaInicio, LocalDateTime fechaFin, TipoMovimiento tipoMovimiento);
}

