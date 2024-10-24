package com.niyovi.proyecto.repository;

import com.niyovi.proyecto.model.Carrito;
import com.niyovi.proyecto.model.DetalleCarrito;
import com.niyovi.proyecto.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DetalleCarritoRepository extends JpaRepository<DetalleCarrito, Long> {

    Optional<DetalleCarrito> findByIdCarritoDetalleAndIdProductoDetalle(Carrito carrito, Producto producto);

    List<DetalleCarrito> findAllByIdCarritoDetalle(Carrito carrito);
}
