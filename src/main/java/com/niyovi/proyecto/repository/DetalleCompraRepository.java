package com.niyovi.proyecto.repository;

import com.niyovi.proyecto.model.Compra;
import com.niyovi.proyecto.model.DetalleCompra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleCompraRepository extends JpaRepository<DetalleCompra, Long> {

    List<DetalleCompra> findByCompraDetalle(Compra compra);
}
