package com.niyovi.proyecto.repository;

import com.niyovi.proyecto.model.Categoria;
import com.niyovi.proyecto.model.Estado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByEstadoCategoria(Estado estadoCategoria);
}
