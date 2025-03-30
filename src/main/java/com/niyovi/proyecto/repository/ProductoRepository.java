package com.niyovi.proyecto.repository;

import com.niyovi.proyecto.model.Categoria;
import com.niyovi.proyecto.model.Estado;
import com.niyovi.proyecto.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByEstadoProducto(Estado estadoProducto);

    List<Producto> findByCategoriaProducto(Categoria categoriaProducto);

    List<Producto> findByNombreProductoContainingIgnoreCase(String nombreProducto);

    List<Producto> findByCategoriaProductoAndNombreProductoContainingIgnoreCase(Categoria categoria, String nombreProducto);

    Page<Producto> findByEstadoProducto(Estado estadoProducto, Pageable pageable);

    Page<Producto> findByCategoriaProducto(Categoria categoriaProducto, Pageable pageable);

    Page<Producto> findByNombreProductoContainingIgnoreCase(String nombreProducto, Pageable pageable);

    Page<Producto> findByCategoriaProductoAndNombreProductoContainingIgnoreCase(Categoria categoria, String nombreProducto, Pageable pageable);
}
