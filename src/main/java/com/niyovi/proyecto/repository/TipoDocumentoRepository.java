package com.niyovi.proyecto.repository;

import com.niyovi.proyecto.model.Estado;
import com.niyovi.proyecto.model.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Long> {

    List<TipoDocumento> findByEstadoTipoDoc(Estado estadoDocumento);
}
