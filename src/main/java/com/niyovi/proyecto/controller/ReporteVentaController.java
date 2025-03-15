package com.niyovi.proyecto.controller;

import com.amazonaws.util.IOUtils;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.niyovi.proyecto.dto.ClienteDTO;
import com.niyovi.proyecto.model.*;
import com.niyovi.proyecto.repository.CompraRepository;
import com.niyovi.proyecto.repository.DetalleCompraRepository;
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
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ReporteVentaController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private DetalleCompraRepository detalleCompraRepository;

    @GetMapping("/reporte-ventas")
    public String mostrarReporteVentas(Model model, Principal principal) {
        Usuario usuario = usuarioService.buscarPorUsuario(principal.getName());
        Rol rolUsuario = usuario.getRolUsuario();
        model.addAttribute("rolUsuario", rolUsuario.getIdRol());
        return "reporteVentas";
    }

    @GetMapping("/reporte-ventas/generar")
    public String generarReporteVentas(
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
        List<Compra> compras = compraRepository.findByFechaCreacionCompraBetween(
                fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59));
        long totalVentas = compras.size();
        double ingresosTotales = compras.stream().mapToDouble(Compra::getPrecioTotalCompra).sum();
        Map<String, Long> ventasPorMes = new LinkedHashMap<>();
        Map<String, Double> ingresosPorMes = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (Compra compra : compras) {
            String mes = compra.getFechaCreacionCompra().format(formatter);
            ventasPorMes.put(mes, ventasPorMes.getOrDefault(mes, 0L) + 1);
            ingresosPorMes.put(mes, ingresosPorMes.getOrDefault(mes, 0.0) + compra.getPrecioTotalCompra());
        }
        List<DetalleCompra> detalles = detalleCompraRepository.findByCompraDetalleFechaCreacionCompraBetween(
                fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59));
        Map<String, Long> productosMasVendidos = detalles.stream()
                .collect(Collectors.groupingBy(detalle -> detalle.getProductoDetalle().getNombreProducto(), Collectors.summingLong(DetalleCompra::getCantidadDetalle)));
        productosMasVendidos = productosMasVendidos.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new
                ));
        Map<ClienteDTO, Long> clientesFrecuentes = compras.stream()
                .collect(Collectors.groupingBy(
                        compra -> new ClienteDTO(
                                compra.getUsuarioCompra().getNombresUsuario(),
                                compra.getUsuarioCompra().getApellidosUsuario(),
                                compra.getUsuarioCompra().getCelularUsuario()
                        ),
                        Collectors.counting()
                ));
        Map<String, Long> clientesFrecuentesMap = compras.stream()
                .collect(Collectors.groupingBy(
                        compra -> compra.getUsuarioCompra().getNombresUsuario() + " " +
                                compra.getUsuarioCompra().getApellidosUsuario(),
                        Collectors.counting()
                ));
        clientesFrecuentes = clientesFrecuentes.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new
                ));
        Map<String, Long> metodosPago = compras.stream()
                .collect(Collectors.groupingBy(compra -> compra.getFormaPagoCompra().getNombreFormaPago(), Collectors.counting()));
        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("ingresosTotales", ingresosTotales);
        model.addAttribute("ventasPorMes", ventasPorMes);
        model.addAttribute("ingresosPorMes", ingresosPorMes);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("productosMasVendidos", productosMasVendidos);
        model.addAttribute("clientesFrecuentes", clientesFrecuentes);
        model.addAttribute("clientesFrecuentesMap", clientesFrecuentesMap);
        model.addAttribute("metodosPago", metodosPago);
        return "reporteVentas";
    }

    @GetMapping("/reporte-ventas/exportar-excel")
    public void exportarExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=reporte_ventas.xlsx");
        LocalDate fechaInicio = LocalDate.now().minusMonths(12).withDayOfMonth(1);
        LocalDate fechaFin = LocalDate.now();
        List<Compra> compras = compraRepository.findByFechaCreacionCompraBetween(
                fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59));
        long totalVentas = compras.size();
        double ingresosTotales = compras.stream().mapToDouble(Compra::getPrecioTotalCompra).sum();
        Map<String, Long> ventasPorMes = new LinkedHashMap<>();
        Map<String, Double> ingresosPorMes = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (Compra compra : compras) {
            String mes = compra.getFechaCreacionCompra().format(formatter);
            ventasPorMes.put(mes, ventasPorMes.getOrDefault(mes, 0L) + 1);
            ingresosPorMes.put(mes, ingresosPorMes.getOrDefault(mes, 0.0) + compra.getPrecioTotalCompra());
        }
        List<DetalleCompra> detalles = detalleCompraRepository.findByCompraDetalleFechaCreacionCompraBetween(
                fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59));
        Map<String, Long> productosMasVendidos = detalles.stream()
                .collect(Collectors.groupingBy(detalle -> detalle.getProductoDetalle().getNombreProducto(),
                        Collectors.summingLong(DetalleCompra::getCantidadDetalle)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
        Map<ClienteDTO, Long> clientesFrecuentes = compras.stream()
                .collect(Collectors.groupingBy(
                        compra -> new ClienteDTO(
                                compra.getUsuarioCompra().getNombresUsuario(),
                                compra.getUsuarioCompra().getApellidosUsuario(),
                                compra.getUsuarioCompra().getCelularUsuario()
                        ),
                        Collectors.counting()
                )).entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        Map<String, Long> metodosPago = compras.stream()
                .collect(Collectors.groupingBy(compra -> compra.getFormaPagoCompra().getNombreFormaPago(),
                        Collectors.counting()));
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reporte de Ventas");
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
        titleCell.setCellValue("REPORTE DE VENTAS");
        titleCell.setCellStyle(headerStyle);
        rowNum++;
        rowNum++;
        Row empresaRow = sheet.createRow(rowNum++);
        Cell empresaCell = empresaRow.createCell(0);
        empresaCell.setCellValue("Alimentos Niyovi SAS");
        empresaCell.setCellStyle(headerStyle);
        Row nitRow = sheet.createRow(rowNum++);
        Cell nitCell = nitRow.createCell(0);
        nitCell.setCellValue("Nit: 1234567890");
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
        rowNum++;
        Row headerRow = sheet.createRow(rowNum++);
        Cell totalVentasCell = headerRow.createCell(0);
        totalVentasCell.setCellValue("Total Ventas");
        totalVentasCell.setCellStyle(headerCellStyle);
        Cell totalVentasValueCell = headerRow.createCell(1);
        totalVentasValueCell.setCellValue(totalVentas);
        totalVentasValueCell.setCellStyle(cellStyle);
        Cell ingresosTotalesCell = headerRow.createCell(2);
        ingresosTotalesCell.setCellValue("Ingresos Totales");
        ingresosTotalesCell.setCellStyle(headerCellStyle);
        Cell ingresosTotalesValueCell = headerRow.createCell(3);
        ingresosTotalesValueCell.setCellValue(ingresosTotales);
        ingresosTotalesValueCell.setCellStyle(cellStyle);
        rowNum++;
        Row ventasMesRow = sheet.createRow(rowNum++);
        Cell ventasMesCell = ventasMesRow.createCell(0);
        ventasMesCell.setCellValue("Ventas por Mes:");
        ventasMesCell.setCellStyle(headerStyle);
        Row ventasHeader = sheet.createRow(rowNum++);
        ventasHeader.createCell(0).setCellValue("Mes");
        ventasHeader.createCell(1).setCellValue("Ventas");
        ventasHeader.createCell(2).setCellValue("Ingresos Totales");
        for (int i = 0; i < 3; i++) {
            ventasHeader.getCell(i).setCellStyle(headerCellStyle);
        }
        for (String mes : ventasPorMes.keySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(mes);
            row.createCell(1).setCellValue(ventasPorMes.get(mes));
            row.createCell(2).setCellValue(ingresosPorMes.get(mes));
            for (int i = 0; i < 3; i++) {
                row.getCell(i).setCellStyle(cellStyle);
            }
        }
        rowNum++;
        Row productosRow = sheet.createRow(rowNum++);
        Cell productosCell = productosRow.createCell(0);
        productosCell.setCellValue("Productos Más Vendidos (Top 5):");
        productosCell.setCellStyle(headerStyle);
        Row productosHeader = sheet.createRow(rowNum++);
        productosHeader.createCell(0).setCellValue("Producto");
        productosHeader.createCell(1).setCellValue("Cantidad Vendida");
        for (int i = 0; i < 2; i++) {
            productosHeader.getCell(i).setCellStyle(headerCellStyle);
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
        Row clientesRow = sheet.createRow(rowNum++);
        Cell clientesCell = clientesRow.createCell(0);
        clientesCell.setCellValue("Clientes Frecuentes (Top 5):");
        clientesCell.setCellStyle(headerStyle);
        Row clientesHeader = sheet.createRow(rowNum++);
        clientesHeader.createCell(0).setCellValue("Cliente");
        clientesHeader.createCell(1).setCellValue("Celular");
        clientesHeader.createCell(2).setCellValue("Compras");
        for (int i = 0; i < 3; i++) {
            clientesHeader.getCell(i).setCellStyle(headerCellStyle);
        }
        for (Map.Entry<ClienteDTO, Long> entry : clientesFrecuentes.entrySet()) {
            ClienteDTO cliente = entry.getKey();
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(cliente.getNombre() + " " + cliente.getApellido());
            row.createCell(1).setCellValue(cliente.getCelular());
            row.createCell(2).setCellValue(entry.getValue());
            for (int i = 0; i < 3; i++) {
                row.getCell(i).setCellStyle(cellStyle);
            }
        }
        rowNum++;
        Row pagosRow = sheet.createRow(rowNum++);
        Cell pagosCell = pagosRow.createCell(0);
        pagosCell.setCellValue("Métodos de Pago Usados:");
        pagosCell.setCellStyle(headerStyle);
        Row pagosHeader = sheet.createRow(rowNum++);
        pagosHeader.createCell(0).setCellValue("Método de Pago");
        pagosHeader.createCell(1).setCellValue("Cantidad");
        for (int i = 0; i < 2; i++) {
            pagosHeader.getCell(i).setCellStyle(headerCellStyle);
        }
        for (Map.Entry<String, Long> entry : metodosPago.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
            for (int i = 0; i < 2; i++) {
                row.getCell(i).setCellStyle(cellStyle);
            }
        }
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/reporte-ventas/exportar-pdf")
    public void exportarPDF(HttpServletResponse response) throws IOException, DocumentException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=reporte_ventas.pdf");
        LocalDate fechaInicio = LocalDate.now().minusMonths(12).withDayOfMonth(1);
        LocalDate fechaFin = LocalDate.now();
        List<Compra> compras = compraRepository.findByFechaCreacionCompraBetween(
                fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59));
        long totalVentas = compras.size();
        double ingresosTotales = compras.stream().mapToDouble(Compra::getPrecioTotalCompra).sum();
        Map<String, Long> ventasPorMes = new LinkedHashMap<>();
        Map<String, Double> ingresosPorMes = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (Compra compra : compras) {
            String mes = compra.getFechaCreacionCompra().format(formatter);
            ventasPorMes.put(mes, ventasPorMes.getOrDefault(mes, 0L) + 1);
            ingresosPorMes.put(mes, ingresosPorMes.getOrDefault(mes, 0.0) + compra.getPrecioTotalCompra());
        }
        List<DetalleCompra> detalles = detalleCompraRepository.findByCompraDetalleFechaCreacionCompraBetween(
                fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59));
        Map<String, Long> productosMasVendidos = detalles.stream()
                .collect(Collectors.groupingBy(detalle -> detalle.getProductoDetalle().getNombreProducto(),
                        Collectors.summingLong(DetalleCompra::getCantidadDetalle)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
        Map<ClienteDTO, Long> clientesFrecuentes = compras.stream()
                .collect(Collectors.groupingBy(
                        compra -> new ClienteDTO(
                                compra.getUsuarioCompra().getNombresUsuario(),
                                compra.getUsuarioCompra().getApellidosUsuario(),
                                compra.getUsuarioCompra().getCelularUsuario()
                        ),
                        Collectors.counting()
                )).entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        Map<String, Long> metodosPago = compras.stream()
                .collect(Collectors.groupingBy(compra -> compra.getFormaPagoCompra().getNombreFormaPago(),
                        Collectors.counting()));
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
        Paragraph titulo = new Paragraph("REPORTE DE VENTAS", boldFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(10);
        document.add(titulo);
        document.add(new Paragraph("Alimentos Niyovi SAS", boldFont));
        document.add(new Paragraph("Nit: 1234567890"));
        document.add(new Paragraph("Finca El Triunfo Km 16 Vía Apulo Naranjalito, Apulo, Cundinamarca"));
        LocalDateTime fechaHoraActual = LocalDateTime.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String fechaFormateada = fechaHoraActual.format(formato);
        document.add(new Paragraph("Fecha generación: " + fechaFormateada));
        document.add(new Paragraph("\n"));
        PdfPTable resumenTable = new PdfPTable(2);
        resumenTable.setWidthPercentage(100);
        resumenTable.addCell(getCell("Total Ventas:", headerFont, Element.ALIGN_LEFT));
        resumenTable.addCell(getCell(String.valueOf(totalVentas), cellFont, Element.ALIGN_RIGHT));
        NumberFormat format = NumberFormat.getInstance(new Locale("es", "CO"));
        resumenTable.addCell(getCell("Ingresos Totales:", headerFont, Element.ALIGN_LEFT));
        resumenTable.addCell(getCell("$" + format.format(ingresosTotales), cellFont, Element.ALIGN_RIGHT));
        document.add(resumenTable);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Ventas por Mes:", headerFont));
        document.add(new Paragraph(" "));
        PdfPTable ventasMesTable = new PdfPTable(3);
        ventasMesTable.setWidthPercentage(100);
        ventasMesTable.addCell(getCell("Mes", headerFont, Element.ALIGN_CENTER));
        ventasMesTable.addCell(getCell("Ventas", headerFont, Element.ALIGN_CENTER));
        ventasMesTable.addCell(getCell("Ingresos Totales", headerFont, Element.ALIGN_CENTER));
        for (String mes : ventasPorMes.keySet()) {
            ventasMesTable.addCell(getCell(mes, cellFont, Element.ALIGN_CENTER));
            ventasMesTable.addCell(getCell(String.valueOf(ventasPorMes.get(mes)), cellFont, Element.ALIGN_CENTER));
            ventasMesTable.addCell(getCell("$" + format.format(ingresosPorMes.get(mes)), cellFont, Element.ALIGN_CENTER));
        }
        document.add(ventasMesTable);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Productos Más Vendidos (Top 5):", headerFont));
        document.add(new Paragraph(" "));
        PdfPTable productosTable = new PdfPTable(2);
        productosTable.setWidthPercentage(100);
        productosTable.addCell(getCell("Producto", headerFont, Element.ALIGN_CENTER));
        productosTable.addCell(getCell("Cantidad", headerFont, Element.ALIGN_CENTER));
        for (Map.Entry<String, Long> entry : productosMasVendidos.entrySet()) {
            productosTable.addCell(getCell(entry.getKey(), cellFont, Element.ALIGN_CENTER));
            productosTable.addCell(getCell(String.valueOf(entry.getValue()), cellFont, Element.ALIGN_CENTER));
        }
        document.add(productosTable);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Clientes Frecuentes (Top 5):", headerFont));
        document.add(new Paragraph(" "));
        PdfPTable clientesTable = new PdfPTable(3);
        clientesTable.setWidthPercentage(100);
        clientesTable.addCell(getCell("Cliente", headerFont, Element.ALIGN_CENTER));
        clientesTable.addCell(getCell("Celular", headerFont, Element.ALIGN_CENTER));
        clientesTable.addCell(getCell("Compras", headerFont, Element.ALIGN_CENTER));
        for (Map.Entry<ClienteDTO, Long> entry : clientesFrecuentes.entrySet()) {
            ClienteDTO cliente = entry.getKey();
            clientesTable.addCell(getCell(cliente.getNombre() + " " + cliente.getApellido(), cellFont, Element.ALIGN_CENTER));
            clientesTable.addCell(getCell(cliente.getCelular(), cellFont, Element.ALIGN_CENTER));
            clientesTable.addCell(getCell(String.valueOf(entry.getValue()), cellFont, Element.ALIGN_CENTER));
        }
        document.add(clientesTable);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Métodos de Pago Usados:", headerFont));
        document.add(new Paragraph(" "));
        PdfPTable pagosTable = new PdfPTable(2);
        pagosTable.setWidthPercentage(100);
        pagosTable.addCell(getCell("Método de Pago", headerFont, Element.ALIGN_CENTER));
        pagosTable.addCell(getCell("Cantidad", headerFont, Element.ALIGN_CENTER));
        for (Map.Entry<String, Long> entry : metodosPago.entrySet()) {
            pagosTable.addCell(getCell(entry.getKey(), cellFont, Element.ALIGN_CENTER));
            pagosTable.addCell(getCell(String.valueOf(entry.getValue()), cellFont, Element.ALIGN_CENTER));
        }
        document.add(pagosTable);
        document.close();
    }

    private PdfPCell getCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }
}