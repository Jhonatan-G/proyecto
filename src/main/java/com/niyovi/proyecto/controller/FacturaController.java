package com.niyovi.proyecto.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.niyovi.proyecto.model.*;
import com.niyovi.proyecto.repository.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Controller
public class FacturaController {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private DetalleCompraRepository detalleCompraRepository;

    @GetMapping("/descargar-factura/{id}")
    public void generarFactura(@PathVariable("id") Long idCompra, HttpServletResponse response) {
        try {
            Compra compra = compraRepository.findById(idCompra)
                    .orElseThrow(() -> new RuntimeException("Compra no encontrada"));
            List<DetalleCompra> detalles = detalleCompraRepository.findByCompraDetalle(compra);
            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();
            try {
                Image logo = Image.getInstance("src/main/resources/static/logo.png");
                logo.scaleToFit(150, 150);
                logo.setAlignment(Element.ALIGN_CENTER);
                document.add(logo);
            } catch (Exception e) {
                System.err.println("No se pudo cargar el logo: " + e.getMessage());
            }
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Paragraph titulo = new Paragraph("FACTURA DE COMPRA", boldFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(10);
            document.add(titulo);
            document.add(new Paragraph("Alimentos Niyovi SAS", boldFont));
            document.add(new Paragraph("Nit: 9014298845"));
            document.add(new Paragraph("Finca El Triunfo Km 16 Vía Apulo Naranjalito, Apulo, Cundinamarca"));
            document.add(new Paragraph("\n"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String fechaFormateada = compra.getFechaCreacionCompra().format(formatter);
            document.add(new Paragraph("Fecha: " + fechaFormateada));
            document.add(new Paragraph("ID Compra: " + compra.getIdCompra()));
            document.add(new Paragraph("Cliente: " + compra.getUsuarioCompra().getNombresUsuario() + " " + compra.getUsuarioCompra().getApellidosUsuario()));
            document.add(new Paragraph("Tipo documento: " + compra.getUsuarioCompra().getTipoDocUsuario().getNombreTipoDoc()));
            document.add(new Paragraph("Número documento: " + compra.getUsuarioCompra().getNumeroDocUsuario()));
            document.add(new Paragraph("Dirección: " + compra.getUsuarioCompra().getDireccionUsuario()));
            document.add(new Paragraph("Teléfono: " + compra.getUsuarioCompra().getCelularUsuario()));
            document.add(new Paragraph("Correo: " + compra.getUsuarioCompra().getCorreoUsuario()));
            document.add(new Paragraph("\n"));
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setWidths(new int[]{3, 3, 4, 2, 2, 2});
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            String[] headers = {"Código", "Producto", "Descripción", "Precio Unitario", "Cantidad", "Subtotal"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }
            NumberFormat format = NumberFormat.getInstance(new Locale("es", "CO"));
            for (DetalleCompra detalle : detalles) {
                table.addCell(String.valueOf(detalle.getProductoDetalle().getIdProducto()));
                table.addCell(detalle.getProductoDetalle().getNombreProducto());
                table.addCell(detalle.getProductoDetalle().getDescripcionProducto());
                table.addCell("$" + format.format(detalle.getPrecioDetalle()));
                table.addCell(detalle.getCantidadDetalle().toString());
                table.addCell("$" + format.format(detalle.getSubtotalDetalle()));
            }
            PdfPCell totalCell = new PdfPCell(new Phrase("Total", headerFont));
            totalCell.setColspan(5);
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalCell.setPadding(5);
            table.addCell(totalCell);
            PdfPCell totalValueCell = new PdfPCell(new Phrase("$" + format.format(compra.getPrecioTotalCompra()), headerFont));
            totalValueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            totalValueCell.setPadding(5);
            table.addCell(totalValueCell);
            document.add(table);
            document.close();
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=factura_" + idCompra + ".pdf");
            response.getOutputStream().write(out.toByteArray());
            response.getOutputStream().flush();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Error al generar la factura", e);
        }
    }
}