package com.niyovi.proyecto.service;

import com.niyovi.proyecto.model.Categoria;
import com.niyovi.proyecto.model.Estado;
import com.niyovi.proyecto.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Categoria> obtenerCategoriasActivas(Estado estadoActivo) {
        return categoriaRepository.findByEstadoCategoria(estadoActivo);
    }
}
