package com.niyovi.proyecto.service;

import com.niyovi.proyecto.model.Carrito;
import com.niyovi.proyecto.model.DetalleCarrito;
import com.niyovi.proyecto.model.Producto;
import com.niyovi.proyecto.model.Usuario;
import com.niyovi.proyecto.repository.CarritoRepository;
import com.niyovi.proyecto.repository.DetalleCarritoRepository;
import com.niyovi.proyecto.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private DetalleCarritoRepository detalleCarritoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Optional<Carrito> obtenerCarritoPorUsuario(Usuario usuario) {
        return carritoRepository.findByUsuarioCarrito(usuario);
    }

    public int contarProductosEnCarrito(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        Optional<Carrito> carritoOptional = carritoRepository.findByUsuarioCarrito(usuario);
        if (carritoOptional.isPresent()) {
            Carrito carrito = carritoOptional.get();
            return detalleCarritoRepository.findAllByIdCarritoDetalle(carrito).stream()
                    .mapToInt(DetalleCarrito::getCantidadDetalle)
                    .sum();
        }
        return 0;
    }

    public Carrito agregarProductoAlCarrito(Carrito carrito, Producto producto, int cantidad) {
        Optional<DetalleCarrito> detalleExistente = detalleCarritoRepository.findByIdCarritoDetalleAndIdProductoDetalle(carrito, producto);
        if (detalleExistente.isPresent()) {
            DetalleCarrito detalleCarrito = detalleExistente.get();
            detalleCarrito.setCantidadDetalle(detalleCarrito.getCantidadDetalle() + cantidad);
            detalleCarritoRepository.save(detalleCarrito);
        } else {
            DetalleCarrito detalleCarrito = new DetalleCarrito();
            detalleCarrito.setIdCarritoDetalle(carrito);
            detalleCarrito.setIdProductoDetalle(producto);
            detalleCarrito.setCantidadDetalle(cantidad);
            detalleCarritoRepository.save(detalleCarrito);
        }
        return carrito;
    }

    public void eliminarProductoDelCarrito(Carrito carrito, Producto producto) {
        Optional<DetalleCarrito> detalleCarrito = detalleCarritoRepository.findByIdCarritoDetalleAndIdProductoDetalle(carrito, producto);
        detalleCarrito.ifPresent(detalleCarritoRepository::delete);
    }

    public List<DetalleCarrito> obtenerDetallesDelCarrito(Carrito carrito) {
        return detalleCarritoRepository.findAllByIdCarritoDetalle(carrito);
    }

    public double calcularTotalCarrito(Carrito carrito) {
        List<DetalleCarrito> detalles = detalleCarritoRepository.findAllByIdCarritoDetalle(carrito);
        return detalles.stream().mapToDouble(detalle -> detalle.getIdProductoDetalle().getPrecioProducto() * detalle.getCantidadDetalle()).sum();
    }
}