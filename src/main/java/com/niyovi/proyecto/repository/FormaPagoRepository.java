package com.niyovi.proyecto.repository;

import com.niyovi.proyecto.model.Estado;
import com.niyovi.proyecto.model.FormaPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormaPagoRepository extends JpaRepository<FormaPago, Long> {

    List<FormaPago> findByEstadoFormaPago(Estado estadoForma);
}
