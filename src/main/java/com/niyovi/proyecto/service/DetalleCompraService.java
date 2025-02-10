package com.niyovi.proyecto.service;

import com.niyovi.proyecto.model.DetalleCompra;
import com.niyovi.proyecto.repository.DetalleCompraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DetalleCompraService {

    private final DetalleCompraRepository detalleCompraRepository;

    public DetalleCompraService(DetalleCompraRepository detalleCompraRepository) {
        this.detalleCompraRepository = detalleCompraRepository;
    }

    @Transactional
    public List<DetalleCompra> guardarDetallesCompra(List<DetalleCompra> detallesCompra) {
        return detalleCompraRepository.saveAll(detallesCompra);
    }
}