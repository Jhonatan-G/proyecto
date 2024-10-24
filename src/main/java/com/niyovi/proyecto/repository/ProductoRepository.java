package com.niyovi.proyecto.repository;

import com.niyovi.proyecto.model.Categoria;
import com.niyovi.proyecto.model.Estado;
import com.niyovi.proyecto.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByEstadoProducto(Estado estadoProducto);

    List<Producto> findByCategoriaProducto(Categoria categoriaProducto);

    List<Producto> findByCategoriaProductoAndNombreProducto(Categoria categoriaProducto, String nombreProducto);

    List<Producto> findByNombreProducto(String nombreProducto);
}
