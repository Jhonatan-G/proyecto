package com.niyovi.proyecto.controller;

import com.amazonaws.util.IOUtils;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.niyovi.proyecto.enums.TipoMovimiento;
import com.niyovi.proyecto.model.*;
import com.niyovi.proyecto.repository.*;
import com.niyovi.proyecto.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ReporteInventarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private DetalleCompraRepository detalleCompraRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @GetMapping("/reporte-inventario")
    public String mostrarReporteInventario(Model model, Principal principal) {
        Usuario usuario = usuarioService.buscarPorUsuario(principal.getName());
        Rol rolUsuario = usuario.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        return "reporteInventario";
    }

    @GetMapping("/reporte-inventario/generar")
    public String generarReporteInventario(
            @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Model model, Principal principal) {
        Usuario usuario = usuarioService.buscarPorUsuario(principal.getName());
        Rol rolUsuario = usuario.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        if (fechaInicio == null) {
            fechaInicio = LocalDate.now().minusMonths(12).withDayOfMonth(1);
        }
        if (fechaFin == null) {
            fechaFin = LocalDate.now();
        }
        List<Producto> productos = productoRepository.findAll();
        Map<String, Integer> stockActual = productos.stream()
                .collect(Collectors.toMap(
                        Producto::getNombreProducto,
                        Producto::getStockProducto
                ));
        Map<String, Integer> productosBajoStock = stockActual.entrySet().stream()
                .filter(entry -> entry.getValue() <= 5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        List<DetalleCompra> detalles = detalleCompraRepository.findByCompraDetalleFechaCreacionCompraBetween(
                fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59));
        Map<String, Long> productosSalidas = detalles.stream()
                .collect(Collectors.groupingBy(
                        detalle -> detalle.getProductoDetalle().getNombreProducto(),
                        Collectors.summingLong(DetalleCompra::getCantidadDetalle)
                ));
        Map<String, Long> productosMasVendidos = productosSalidas.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new
                ));
        List<MovimientoInventario> movimientosEntradas = movimientoInventarioRepository
                .findByFechaMovimientoBetweenAndTipoMovimiento(
                        fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59), TipoMovimiento.ENTRADA);
        Map<String, Long> productosEntradas = movimientosEntradas.stream()
                .collect(Collectors.groupingBy(
                        mov -> mov.getProducto().getNombreProducto(),
                        Collectors.summingLong(MovimientoInventario::getCantidad)
                ));
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("stockActual", stockActual);
        model.addAttribute("productosBajoStock", productosBajoStock);
        model.addAttribute("productosVendidos", productosMasVendidos);
        model.addAttribute("productosEntradas", productosEntradas);
        model.addAttribute("productosSalidas", productosSalidas);
        return "reporteInventario";
    }

    @GetMapping("reporte-inventario/exportar-excel")
    public void exportarExcelInventario(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=reporte_inventario.xlsx");
        LocalDate fechaInicio = LocalDate.now().minusMonths(12).withDayOfMonth(1);
        LocalDate fechaFin = LocalDate.now();
        List<Producto> productos = productoRepository.findAll();
        Map<String, Integer> stockActual = productos.stream()
                .collect(Collectors.toMap(
                        Producto::getNombreProducto,
                        Producto::getStockProducto
                ));
        Map<String, Integer> productosBajoStock = stockActual.entrySet().stream()
                .filter(entry -> entry.getValue() <= 5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        List<DetalleCompra> detalles = detalleCompraRepository.findByCompraDetalleFechaCreacionCompraBetween(
                fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59));
        Map<String, Long> productosSalidas = detalles.stream()
                .collect(Collectors.groupingBy(
                        detalle -> detalle.getProductoDetalle().getNombreProducto(),
                        Collectors.summingLong(DetalleCompra::getCantidadDetalle)
                ));
        Map<String, Long> productosMasVendidos = productosSalidas.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new
                ));
        List<MovimientoInventario> movimientosEntradas = movimientoInventarioRepository
                .findByFechaMovimientoBetweenAndTipoMovimiento(
                        fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59), TipoMovimiento.ENTRADA);
        Map<String, Long> productosEntradas = movimientosEntradas.stream()
                .collect(Collectors.groupingBy(
                        mov -> mov.getProducto().getNombreProducto(),
                        Collectors.summingLong(MovimientoInventario::getCantidad)
                ));
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reporte de Inventario");
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        InputStream inputStream = new FileInputStream("src/main/resources/static/logo.png");
        byte[] bytes = IOUtils.toByteArray(inputStream);
        int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
        inputStream.close();
        CreationHelper helper = workbook.getCreationHelper();
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(0);
        anchor.setRow1(0);
        anchor.setCol2(2);
        anchor.setRow2(3);
        Picture pict = drawing.createPicture(anchor, pictureIdx);
        pict.resize(1.0);
        int rowNum = 1;
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(2);
        titleCell.setCellValue("REPORTE DE INVENTARIO");
        titleCell.setCellStyle(headerStyle);
        rowNum++;
        rowNum++;
        Row empresaRow = sheet.createRow(rowNum++);
        Cell empresaCell = empresaRow.createCell(0);
        empresaCell.setCellValue("Alimentos Niyovi SAS");
        empresaCell.setCellStyle(headerStyle);
        Row nitRow = sheet.createRow(rowNum++);
        Cell nitCell = nitRow.createCell(0);
        nitCell.setCellValue("Nit: 9014298845");
        Row direccionRow = sheet.createRow(rowNum++);
        Cell direccionCell = direccionRow.createCell(0);
        direccionCell.setCellValue("Finca El Triunfo Km 16 Vía Apulo Naranjalito, Apulo, Cundinamarca");
        LocalDateTime fechaHoraActual = LocalDateTime.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String fechaFormateada = fechaHoraActual.format(formato);
        Row fechaRow = sheet.createRow(rowNum++);
        Cell fechaCell = fechaRow.createCell(0);
        fechaCell.setCellValue("Fecha generación: " + fechaFormateada);
        CellStyle headerCellStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        Row headerRow = sheet.createRow(rowNum++);
        Row stockActualRow = sheet.createRow(rowNum++);
        Cell stockActualCell = stockActualRow.createCell(0);
        stockActualCell.setCellValue("Stock Actual de Productos:");
        stockActualCell.setCellStyle(headerStyle);
        Row stockActualHeader = sheet.createRow(rowNum++);
        stockActualHeader.createCell(0).setCellValue("Producto");
        stockActualHeader.createCell(1).setCellValue("Stock");
        for (int i = 0; i < 2; i++) {
            stockActualHeader.getCell(i).setCellStyle(headerCellStyle);
        }
        for (Map.Entry<String, Integer> entry : stockActual.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
            for (int i = 0; i < 2; i++) {
                row.getCell(i).setCellStyle(cellStyle);
            }
        }
        rowNum++;
        Row productosStockRow = sheet.createRow(rowNum++);
        Cell productosStockCell = productosStockRow.createCell(0);
        productosStockCell.setCellValue("Productos con Bajo Stock (≤ 5 unidades):");
        productosStockCell.setCellStyle(headerStyle);
        Row productosStockHeader = sheet.createRow(rowNum++);
        productosStockHeader.createCell(0).setCellValue("Producto");
        productosStockHeader.createCell(1).setCellValue("Stock");
        for (int i = 0; i < 2; i++) {
            productosStockHeader.getCell(i).setCellStyle(headerCellStyle);
        }
        for (Map.Entry<String, Integer> entry : productosBajoStock.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
            for (int i = 0; i < 2; i++) {
                row.getCell(i).setCellStyle(cellStyle);
            }
        }
        rowNum++;
        Row productosVendidosRow = sheet.createRow(rowNum++);
        Cell productosVendidosCell = productosVendidosRow.createCell(0);
        productosVendidosCell.setCellValue("Productos Más Vendidos:");
        productosVendidosCell.setCellStyle(headerStyle);
        Row productosVendidosHeader = sheet.createRow(rowNum++);
        productosVendidosHeader.createCell(0).setCellValue("Producto");
        productosVendidosHeader.createCell(1).setCellValue("Cantidad Vendida");
        for (int i = 0; i < 2; i++) {
            productosVendidosHeader.getCell(i).setCellStyle(headerCellStyle);
        }
        for (Map.Entry<String, Long> entry : productosMasVendidos.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
            for (int i = 0; i < 2; i++) {
                row.getCell(i).setCellStyle(cellStyle);
            }
        }
        rowNum++;
        Row productosEntradaSalidaRow = sheet.createRow(rowNum++);
        Cell productosEntradaSalidaCell = productosEntradaSalidaRow.createCell(0);
        productosEntradaSalidaCell.setCellValue("Entradas y Salidas de Productos:");
        productosEntradaSalidaCell.setCellStyle(headerStyle);
        Row productosEntradaSalidaHeader = sheet.createRow(rowNum++);
        productosEntradaSalidaHeader.createCell(0).setCellValue("Producto");
        productosEntradaSalidaHeader.createCell(1).setCellValue("Entradas");
        productosEntradaSalidaHeader.createCell(2).setCellValue("Salidas");
        for (int i = 0; i < 3; i++) {
            productosEntradaSalidaHeader.getCell(i).setCellStyle(headerCellStyle);
        }
        for (Map.Entry<String, Long> entry : productosEntradas.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
            row.createCell(2).setCellValue(productosSalidas.getOrDefault(entry.getKey(), 0L));
            for (int i = 0; i < 3; i++) {
                row.getCell(i).setCellStyle(cellStyle);
            }
        }
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/reporte-inventario/exportar-pdf")
    public void exportarPDFInventario(HttpServletResponse response) throws IOException, DocumentException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=reporte_inventario.pdf");
        LocalDate fechaInicio = LocalDate.now().minusMonths(12).withDayOfMonth(1);
        LocalDate fechaFin = LocalDate.now();
        List<Producto> productos = productoRepository.findAll();
        Map<String, Integer> stockActual = productos.stream()
                .collect(Collectors.toMap(
                        Producto::getNombreProducto,
                        Producto::getStockProducto
                ));
        Map<String, Integer> productosBajoStock = stockActual.entrySet().stream()
                .filter(entry -> entry.getValue() <= 5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        List<DetalleCompra> detalles = detalleCompraRepository.findByCompraDetalleFechaCreacionCompraBetween(
                fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59));
        Map<String, Long> productosSalidas = detalles.stream()
                .collect(Collectors.groupingBy(
                        detalle -> detalle.getProductoDetalle().getNombreProducto(),
                        Collectors.summingLong(DetalleCompra::getCantidadDetalle)
                ));
        Map<String, Long> productosMasVendidos = productosSalidas.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new
                ));
        List<MovimientoInventario> movimientosEntradas = movimientoInventarioRepository
                .findByFechaMovimientoBetweenAndTipoMovimiento(
                        fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59), TipoMovimiento.ENTRADA);
        Map<String, Long> productosEntradas = movimientosEntradas.stream()
                .collect(Collectors.groupingBy(
                        mov -> mov.getProducto().getNombreProducto(),
                        Collectors.summingLong(MovimientoInventario::getCantidad)
                ));
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        try {
            Image logo = Image.getInstance("src/main/resources/static/logo.png");
            logo.scaleToFit(150, 150);
            logo.setAlignment(Element.ALIGN_CENTER);
            document.add(logo);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el logo: " + e.getMessage());
        }
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        Paragraph titulo = new Paragraph("REPORTE DE INVENTARIO", boldFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(10);
        document.add(titulo);
        document.add(new Paragraph("Alimentos Niyovi SAS", boldFont));
        document.add(new Paragraph("Nit: 9014298845"));
        document.add(new Paragraph("Finca El Triunfo Km 16 Vía Apulo Naranjalito, Apulo, Cundinamarca"));
        LocalDateTime fechaHoraActual = LocalDateTime.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String fechaFormateada = fechaHoraActual.format(formato);
        document.add(new Paragraph("Fecha generación: " + fechaFormateada));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Stock Actual de Productos:", headerFont));
        document.add(new Paragraph(" "));
        PdfPTable stockActualTable = new PdfPTable(2);
        stockActualTable.setWidthPercentage(100);
        stockActualTable.addCell(getCell("Producto", headerFont, Element.ALIGN_CENTER));
        stockActualTable.addCell(getCell("Stock", headerFont, Element.ALIGN_CENTER));
        for (Map.Entry<String, Integer> entry : stockActual.entrySet()) {
            stockActualTable.addCell(getCell(entry.getKey(), cellFont, Element.ALIGN_CENTER));
            stockActualTable.addCell(getCell(entry.getValue().toString(), cellFont, Element.ALIGN_CENTER));
        }
        document.add(stockActualTable);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Productos con Bajo Stock (≤ 5 unidades):", headerFont));
        document.add(new Paragraph(" "));
        PdfPTable bajoStockTable = new PdfPTable(2);
        bajoStockTable.setWidthPercentage(100);
        bajoStockTable.addCell(getCell("Producto", headerFont, Element.ALIGN_CENTER));
        bajoStockTable.addCell(getCell("Stock", headerFont, Element.ALIGN_CENTER));
        for (Map.Entry<String, Integer> entry : productosBajoStock.entrySet()) {
            bajoStockTable.addCell(getCell(entry.getKey(), cellFont, Element.ALIGN_CENTER));
            bajoStockTable.addCell(getCell(entry.getValue().toString(), cellFont, Element.ALIGN_CENTER));
        }
        document.add(bajoStockTable);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Productos Más Vendidos:", headerFont));
        document.add(new Paragraph(" "));
        PdfPTable productosVendidosTable = new PdfPTable(2);
        productosVendidosTable.setWidthPercentage(100);
        productosVendidosTable.addCell(getCell("Producto", headerFont, Element.ALIGN_CENTER));
        productosVendidosTable.addCell(getCell("Cantidad Vendida", headerFont, Element.ALIGN_CENTER));
        for (Map.Entry<String, Long> entry : productosMasVendidos.entrySet()) {
            productosVendidosTable.addCell(getCell(entry.getKey(), cellFont, Element.ALIGN_CENTER));
            productosVendidosTable.addCell(getCell(entry.getValue().toString(), cellFont, Element.ALIGN_CENTER));
        }
        document.add(productosVendidosTable);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Entradas y Salidas de Productos:", headerFont));
        document.add(new Paragraph(" "));
        PdfPTable productosEntradaSalidasTable = new PdfPTable(3);
        productosEntradaSalidasTable.setWidthPercentage(100);
        productosEntradaSalidasTable.addCell(getCell("Producto", headerFont, Element.ALIGN_CENTER));
        productosEntradaSalidasTable.addCell(getCell("Entradas", headerFont, Element.ALIGN_CENTER));
        productosEntradaSalidasTable.addCell(getCell("Salidas", headerFont, Element.ALIGN_CENTER));
        for (Map.Entry<String, Long> entry : productosEntradas.entrySet()) {
            productosEntradaSalidasTable.addCell(getCell(entry.getKey(), cellFont, Element.ALIGN_CENTER));
            productosEntradaSalidasTable.addCell(getCell(entry.getValue().toString(), cellFont, Element.ALIGN_CENTER));
            productosEntradaSalidasTable.addCell(getCell(productosSalidas.getOrDefault(entry.getKey(), 0L).toString(), cellFont, Element.ALIGN_CENTER));
        }
        document.add(productosEntradaSalidasTable);
        document.close();
    }

    private PdfPCell getCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }
}