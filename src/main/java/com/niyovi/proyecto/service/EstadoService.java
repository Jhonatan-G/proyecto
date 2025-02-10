package com.niyovi.proyecto.service;

import com.niyovi.proyecto.model.Estado;
import com.niyovi.proyecto.repository.EstadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstadoService {

    @Autowired
    private EstadoRepository estadoRepository;

    public List<Estado> listarEstados() {
        return estadoRepository.findAll();
    }

    public List<Estado> listarEstadosParaPedidos(List<Long> ids) {
        return estadoRepository.findByIdEstadoIn(ids);
    }

    public Estado obtenerPorId(Long id) {
        return estadoRepository.findById(id).orElse(null);
    }
}