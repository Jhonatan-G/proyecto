package com.niyovi.proyecto.service;

import com.niyovi.proyecto.model.Categoria;
import com.niyovi.proyecto.model.Estado;
import com.niyovi.proyecto.model.Producto;
import com.niyovi.proyecto.repository.CategoriaRepository;
import com.niyovi.proyecto.repository.EstadoRepository;
import com.niyovi.proyecto.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private EstadoRepository estadoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    public Producto guardarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    public List<Producto> obtenerProductosActivos() {
        Estado estadoActivo = estadoRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
        return productoRepository.findByEstadoProducto(estadoActivo);
    }

    public List<Producto> filtrarProductos(Long categoriaId, String nombreProducto) {
        Categoria categoria = null;
        if (categoriaId != null) {
            categoria = categoriaRepository.findById(categoriaId)
                    .orElse(null);
        }
        if (categoria != null && nombreProducto != null && !nombreProducto.isEmpty()) {
            return productoRepository.findByCategoriaProductoAndNombreProducto(categoria, nombreProducto);
        } else if (categoria != null) {
            return productoRepository.findByCategoriaProducto(categoria);
        } else if (nombreProducto != null && !nombreProducto.isEmpty()) {
            return productoRepository.findByNombreProducto(nombreProducto);
        } else {
            Estado estadoActivo = estadoRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
            return productoRepository.findByEstadoProducto(estadoActivo);
        }
    }

    public void actualizarProducto(Producto producto) {
        productoRepository.save(producto);
    }

    public Producto obtenerProductoPorId(Long id) {
        return productoRepository.findById(id).orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    public void eliminarProducto(Producto producto) {
        Producto productoExistente = productoRepository.findById(producto.getIdProducto()).orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        productoExistente.setEstadoProducto(producto.getEstadoProducto());
        productoRepository.save(productoExistente);
    }

    public List<Producto> filtrarProductosCatalogo(Long categoriaId, String nombreProducto, Integer precioMin, Integer precioMax, String ordenarPor) {
        List<Producto> productos;
        Estado estadoActivo = estadoRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Estado activo no encontrado"));
        if (categoriaId != null && nombreProducto != null && !nombreProducto.isEmpty()) {
            Categoria categoria = categoriaRepository.findById(categoriaId)
                    .orElse(null);
            productos = productoRepository.findByCategoriaProductoAndNombreProducto(categoria, nombreProducto);
        } else if (categoriaId != null) {
            Categoria categoria = categoriaRepository.findById(categoriaId)
                    .orElse(null);
            productos = productoRepository.findByCategoriaProducto(categoria);
        } else if (nombreProducto != null && !nombreProducto.isEmpty()) {
            productos = productoRepository.findByNombreProducto(nombreProducto);
        } else {
            productos = productoRepository.findByEstadoProducto(estadoActivo);
        }
        if (precioMin != null && precioMax != null) {
            productos = productos.stream()
                    .filter(p -> p.getPrecioProducto() >= precioMin && p.getPrecioProducto() <= precioMax)
                    .collect(Collectors.toList());
        }
        if ("mayor".equalsIgnoreCase(ordenarPor)) {
            productos.sort(Comparator.comparing(Producto::getPrecioProducto).reversed());
        } else {
            productos.sort(Comparator.comparing(Producto::getPrecioProducto));
        }
        return productos;
    }

    public Producto agregarProductoAlCarrito(Long idProducto) {
        Producto producto = obtenerProductoPorId(idProducto);
        if (producto == null) {
            throw new RuntimeException("Producto no encontrado");
        }
        return producto;
    }
}