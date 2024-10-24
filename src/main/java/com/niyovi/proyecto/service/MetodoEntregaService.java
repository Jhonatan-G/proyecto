package com.niyovi.proyecto.service;

import com.niyovi.proyecto.model.Estado;
import com.niyovi.proyecto.model.MetodoEntrega;
import com.niyovi.proyecto.repository.MetodoEntregaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetodoEntregaService {

    @Autowired
    private MetodoEntregaRepository metodoEntregaRepository;

    public List<MetodoEntrega> obtenerMetodosEntregaActivos(Estado estadoActivo) {
        return metodoEntregaRepository.findByEstadoMetodoEntrega(estadoActivo);
    }
}
