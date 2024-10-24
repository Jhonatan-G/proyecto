package com.niyovi.proyecto.repository;

import com.niyovi.proyecto.model.Carrito;
import com.niyovi.proyecto.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    Optional<Carrito> findByUsuarioCarrito(Usuario usuario);
}
