package com.niyovi.proyecto.repository;

import com.niyovi.proyecto.model.Estado;
import com.niyovi.proyecto.model.TipoDocumento;
import com.niyovi.proyecto.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Usuario findByUsuarioUsuario(String usuario);

    Optional<Usuario> findByTipoDocUsuarioAndNumeroDocUsuarioAndCorreoUsuario(
            TipoDocumento tipoDocUsuario, String numeroDocUsuario, String correoUsuario);

    Page<Usuario> findByEstadoUsuario(Estado estadoUsuario, Pageable pageable);

    Page<Usuario> findByTipoDocUsuarioAndNumeroDocUsuario(TipoDocumento tipoDocUsuario, String numeroDocUsuario, Pageable pageable);

    List<Usuario> findByTipoDocUsuarioAndNumeroDocUsuario(TipoDocumento tipoDocUsuario, String numeroDocUsuario);

    Usuario findByRolUsuarioIdRol(Long idRol);
}
