package com.niyovi.proyecto.service;

import com.niyovi.proyecto.model.Estado;
import com.niyovi.proyecto.model.TipoDocumento;
import com.niyovi.proyecto.repository.TipoDocumentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TipoDocumentoService {

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    public TipoDocumento obtenerPorId(Long id) {
        return tipoDocumentoRepository.findById(id).orElse(null);
    }

    public List<TipoDocumento> obtenerTiposDocumentoActivos(Estado estadoActivo) {
        return tipoDocumentoRepository.findByEstadoTipoDoc(estadoActivo);
    }
}