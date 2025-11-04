package com.fabrica.p6f5.springapp.invoice.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la clase Invoice usando el patrón AAA (Arrange-Act-Assert)
 */
@DisplayName("Invoice Model Tests")
public class InvoiceTest {

    private Invoice invoice;

    @BeforeEach
    void setUp() {
        invoice = new Invoice();
    }

    @Test
    @DisplayName("Debe crear una factura con valores por defecto")
    void testInvoiceDefaultValues() {
        // Arrange - No se requiere preparación adicional, se usa la instancia del setUp

        // Act - Crear una nueva instancia
        Invoice newInvoice = new Invoice();

        // Assert - Verificar valores por defecto
        assertEquals("USD", newInvoice.getCurrency());
        assertEquals(Invoice.InvoiceStatus.DRAFT, newInvoice.getStatus());
        assertEquals(BigDecimal.ZERO, newInvoice.getTaxAmount());
        assertEquals(1, newInvoice.getVersion());
        assertNotNull(newInvoice.getItems());
        assertNotNull(newInvoice.getShipments());
        assertTrue(newInvoice.getItems().isEmpty());
        assertTrue(newInvoice.getShipments().isEmpty());
    }

    @Test
    @DisplayName("Debe crear una factura con todos los campos usando constructor con argumentos")
    void testInvoiceAllArgsConstructor() {
        // Arrange - Preparar todos los datos necesarios
        Long id = 1L;
        String fiscalFolio = "FISCAL-001";
        String invoiceNumber = "INV-001";
        String clientName = "Cliente Test";
        LocalDate invoiceDate = LocalDate.of(2025, 11, 3);
        LocalDate dueDate = LocalDate.of(2025, 12, 3);
        BigDecimal subtotal = new BigDecimal("1000.00");
        BigDecimal taxAmount = new BigDecimal("160.00");
        BigDecimal totalAmount = new BigDecimal("1160.00");
        String currency = "MXN";
        Invoice.InvoiceStatus status = Invoice.InvoiceStatus.ISSUED;
        String pdfUrl = "http://example.com/invoice.pdf";
        Long createdBy = 100L;
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        Integer version = 1;

        // Act - Crear la factura con todos los argumentos
        Invoice invoice = new Invoice(
            id, fiscalFolio, invoiceNumber, clientName, invoiceDate, dueDate,
            subtotal, taxAmount, totalAmount, currency, status, pdfUrl,
            createdBy, createdAt, updatedAt, version, new ArrayList<>(), new ArrayList<>()
        );

        // Assert - Verificar que todos los campos se asignaron correctamente
        assertEquals(id, invoice.getId());
        assertEquals(fiscalFolio, invoice.getFiscalFolio());
        assertEquals(invoiceNumber, invoice.getInvoiceNumber());
        assertEquals(clientName, invoice.getClientName());
        assertEquals(invoiceDate, invoice.getInvoiceDate());
        assertEquals(dueDate, invoice.getDueDate());
        assertEquals(subtotal, invoice.getSubtotal());
        assertEquals(taxAmount, invoice.getTaxAmount());
        assertEquals(totalAmount, invoice.getTotalAmount());
        assertEquals(currency, invoice.getCurrency());
        assertEquals(status, invoice.getStatus());
        assertEquals(pdfUrl, invoice.getPdfUrl());
        assertEquals(createdBy, invoice.getCreatedBy());
        assertEquals(version, invoice.getVersion());
    }

    @Test
    @DisplayName("Debe establecer la fecha de creación y actualización en PrePersist")
    void testOnCreate() {
        // Arrange - Configurar una factura sin fechas
        invoice.setInvoiceDate(null);
        invoice.setCreatedAt(null);
        invoice.setUpdatedAt(null);

        // Act - Llamar al método onCreate
        invoice.onCreate();

        // Assert - Verificar que las fechas se establecieron
        assertNotNull(invoice.getCreatedAt());
        assertNotNull(invoice.getUpdatedAt());
        assertNotNull(invoice.getInvoiceDate());
        assertEquals(LocalDate.now(), invoice.getInvoiceDate());
    }

    @Test
    @DisplayName("Debe preservar la fecha de factura existente en PrePersist")
    void testOnCreateWithExistingInvoiceDate() {
        // Arrange - Configurar una factura con fecha específica
        LocalDate specificDate = LocalDate.of(2025, 10, 15);
        invoice.setInvoiceDate(specificDate);

        // Act - Llamar al método onCreate
        invoice.onCreate();

        // Assert - Verificar que la fecha original se preservó
        assertEquals(specificDate, invoice.getInvoiceDate());
        assertNotNull(invoice.getCreatedAt());
        assertNotNull(invoice.getUpdatedAt());
    }

    @Test
    @DisplayName("Debe actualizar la fecha de modificación en PreUpdate")
    void testOnUpdate() {
        // Arrange - Configurar fechas iniciales
        LocalDateTime initialCreatedAt = LocalDateTime.of(2025, 11, 1, 10, 0);
        LocalDateTime initialUpdatedAt = LocalDateTime.of(2025, 11, 1, 10, 0);
        invoice.setCreatedAt(initialCreatedAt);
        invoice.setUpdatedAt(initialUpdatedAt);

        // Act - Simular una actualización
        try {
            Thread.sleep(100); // Pequeña pausa para asegurar diferencia de tiempo
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        invoice.onUpdate();

        // Assert - Verificar que updatedAt cambió pero createdAt no
        assertEquals(initialCreatedAt, invoice.getCreatedAt());
        assertNotEquals(initialUpdatedAt, invoice.getUpdatedAt());
        assertTrue(invoice.getUpdatedAt().isAfter(initialUpdatedAt));
    }

    @Test
    @DisplayName("Debe permitir editar factura en estado DRAFT")
    void testCanBeEditedWhenDraft() {
        // Arrange - Configurar factura en estado DRAFT
        invoice.setStatus(Invoice.InvoiceStatus.DRAFT);

        // Act - Verificar si puede ser editada
        boolean result = invoice.canBeEdited();

        // Assert - Debe permitir edición
        assertTrue(result);
    }

    @Test
    @DisplayName("No debe permitir editar factura en estado ISSUED")
    void testCannotBeEditedWhenIssued() {
        // Arrange - Configurar factura en estado ISSUED
        invoice.setStatus(Invoice.InvoiceStatus.ISSUED);

        // Act - Verificar si puede ser editada
        boolean result = invoice.canBeEdited();

        // Assert - No debe permitir edición
        assertFalse(result);
    }

    @Test
    @DisplayName("No debe permitir editar factura en estado PAID")
    void testCannotBeEditedWhenPaid() {
        // Arrange - Configurar factura en estado PAID
        invoice.setStatus(Invoice.InvoiceStatus.PAID);

        // Act - Verificar si puede ser editada
        boolean result = invoice.canBeEdited();

        // Assert - No debe permitir edición
        assertFalse(result);
    }

    @Test
    @DisplayName("No debe permitir editar factura en estado CANCELLED")
    void testCannotBeEditedWhenCancelled() {
        // Arrange - Configurar factura en estado CANCELLED
        invoice.setStatus(Invoice.InvoiceStatus.CANCELLED);

        // Act - Verificar si puede ser editada
        boolean result = invoice.canBeEdited();

        // Assert - No debe permitir edición
        assertFalse(result);
    }

    @Test
    @DisplayName("Debe permitir emitir factura válida en estado DRAFT")
    void testCanBeIssuedWhenValid() {
        // Arrange - Configurar una factura válida para emisión
        invoice.setStatus(Invoice.InvoiceStatus.DRAFT);
        invoice.setSubtotal(new BigDecimal("1000.00"));
        invoice.setClientName("Cliente Test");

        InvoiceItem item = new InvoiceItem();
        invoice.getItems().add(item);

        // Act - Verificar si puede ser emitida
        boolean result = invoice.canBeIssued();

        // Assert - Debe permitir emisión
        assertTrue(result);
    }

    @Test
    @DisplayName("No debe permitir emitir factura sin items")
    void testCannotBeIssuedWithoutItems() {
        // Arrange - Configurar factura sin items
        invoice.setStatus(Invoice.InvoiceStatus.DRAFT);
        invoice.setSubtotal(new BigDecimal("1000.00"));
        invoice.setClientName("Cliente Test");
        invoice.setItems(new ArrayList<>());

        // Act - Verificar si puede ser emitida
        boolean result = invoice.canBeIssued();

        // Assert - No debe permitir emisión
        assertFalse(result);
    }

    @Test
    @DisplayName("No debe permitir emitir factura con subtotal cero")
    void testCannotBeIssuedWithZeroSubtotal() {
        // Arrange - Configurar factura con subtotal cero
        invoice.setStatus(Invoice.InvoiceStatus.DRAFT);
        invoice.setSubtotal(BigDecimal.ZERO);
        invoice.setClientName("Cliente Test");

        InvoiceItem item = new InvoiceItem();
        invoice.getItems().add(item);

        // Act - Verificar si puede ser emitida
        boolean result = invoice.canBeIssued();

        // Assert - No debe permitir emisión
        assertFalse(result);
    }

    @Test
    @DisplayName("No debe permitir emitir factura con subtotal null")
    void testCannotBeIssuedWithNullSubtotal() {
        // Arrange - Configurar factura con subtotal null
        invoice.setStatus(Invoice.InvoiceStatus.DRAFT);
        invoice.setSubtotal(null);
        invoice.setClientName("Cliente Test");

        InvoiceItem item = new InvoiceItem();
        invoice.getItems().add(item);

        // Act - Verificar si puede ser emitida
        boolean result = invoice.canBeIssued();

        // Assert - No debe permitir emisión
        assertFalse(result);
    }

    @Test
    @DisplayName("No debe permitir emitir factura sin nombre de cliente")
    void testCannotBeIssuedWithoutClientName() {
        // Arrange - Configurar factura sin nombre de cliente
        invoice.setStatus(Invoice.InvoiceStatus.DRAFT);
        invoice.setSubtotal(new BigDecimal("1000.00"));
        invoice.setClientName(null);

        InvoiceItem item = new InvoiceItem();
        invoice.getItems().add(item);

        // Act - Verificar si puede ser emitida
        boolean result = invoice.canBeIssued();

        // Assert - No debe permitir emisión
        assertFalse(result);
    }

    @Test
    @DisplayName("No debe permitir emitir factura con nombre de cliente vacío")
    void testCannotBeIssuedWithEmptyClientName() {
        // Arrange - Configurar factura con nombre de cliente vacío
        invoice.setStatus(Invoice.InvoiceStatus.DRAFT);
        invoice.setSubtotal(new BigDecimal("1000.00"));
        invoice.setClientName("   ");

        InvoiceItem item = new InvoiceItem();
        invoice.getItems().add(item);

        // Act - Verificar si puede ser emitida
        boolean result = invoice.canBeIssued();

        // Assert - No debe permitir emisión
        assertFalse(result);
    }

    @Test
    @DisplayName("No debe permitir emitir factura que no está en estado DRAFT")
    void testCannotBeIssuedWhenNotDraft() {
        // Arrange - Configurar factura válida pero en estado ISSUED
        invoice.setStatus(Invoice.InvoiceStatus.ISSUED);
        invoice.setSubtotal(new BigDecimal("1000.00"));
        invoice.setClientName("Cliente Test");

        InvoiceItem item = new InvoiceItem();
        invoice.getItems().add(item);

        // Act - Verificar si puede ser emitida
        boolean result = invoice.canBeIssued();

        // Assert - No debe permitir emisión
        assertFalse(result);
    }

    @Test
    @DisplayName("Debe establecer y obtener todos los campos correctamente")
    void testSettersAndGetters() {
        // Arrange - Preparar datos de prueba
        Long id = 1L;
        String fiscalFolio = "FISCAL-123";
        String invoiceNumber = "INV-123";
        String clientName = "Cliente Prueba";
        LocalDate invoiceDate = LocalDate.of(2025, 11, 3);
        LocalDate dueDate = LocalDate.of(2025, 12, 3);
        BigDecimal subtotal = new BigDecimal("5000.00");
        BigDecimal taxAmount = new BigDecimal("800.00");
        BigDecimal totalAmount = new BigDecimal("5800.00");
        String currency = "EUR";
        Invoice.InvoiceStatus status = Invoice.InvoiceStatus.PAID;
        String pdfUrl = "http://test.com/pdf";
        Long createdBy = 50L;
        Integer version = 2;

        // Act - Establecer todos los valores
        invoice.setId(id);
        invoice.setFiscalFolio(fiscalFolio);
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setClientName(clientName);
        invoice.setInvoiceDate(invoiceDate);
        invoice.setDueDate(dueDate);
        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotalAmount(totalAmount);
        invoice.setCurrency(currency);
        invoice.setStatus(status);
        invoice.setPdfUrl(pdfUrl);
        invoice.setCreatedBy(createdBy);
        invoice.setVersion(version);

        // Assert - Verificar que todos los valores se establecieron correctamente
        assertEquals(id, invoice.getId());
        assertEquals(fiscalFolio, invoice.getFiscalFolio());
        assertEquals(invoiceNumber, invoice.getInvoiceNumber());
        assertEquals(clientName, invoice.getClientName());
        assertEquals(invoiceDate, invoice.getInvoiceDate());
        assertEquals(dueDate, invoice.getDueDate());
        assertEquals(subtotal, invoice.getSubtotal());
        assertEquals(taxAmount, invoice.getTaxAmount());
        assertEquals(totalAmount, invoice.getTotalAmount());
        assertEquals(currency, invoice.getCurrency());
        assertEquals(status, invoice.getStatus());
        assertEquals(pdfUrl, invoice.getPdfUrl());
        assertEquals(createdBy, invoice.getCreatedBy());
        assertEquals(version, invoice.getVersion());
    }

    @Test
    @DisplayName("Debe verificar todos los valores del enum InvoiceStatus")
    void testInvoiceStatusEnum() {
        // Arrange - No se requiere preparación

        // Act - Obtener todos los valores del enum
        Invoice.InvoiceStatus[] statuses = Invoice.InvoiceStatus.values();

        // Assert - Verificar que existen los 4 estados esperados
        assertEquals(4, statuses.length);
        assertEquals(Invoice.InvoiceStatus.DRAFT, Invoice.InvoiceStatus.valueOf("DRAFT"));
        assertEquals(Invoice.InvoiceStatus.ISSUED, Invoice.InvoiceStatus.valueOf("ISSUED"));
        assertEquals(Invoice.InvoiceStatus.PAID, Invoice.InvoiceStatus.valueOf("PAID"));
        assertEquals(Invoice.InvoiceStatus.CANCELLED, Invoice.InvoiceStatus.valueOf("CANCELLED"));
    }

    @Test
    @DisplayName("Debe manejar correctamente la lista de items")
    void testInvoiceItemsList() {
        // Arrange - Crear items de prueba
        InvoiceItem item1 = new InvoiceItem();
        InvoiceItem item2 = new InvoiceItem();

        // Act - Agregar items a la factura
        invoice.getItems().add(item1);
        invoice.getItems().add(item2);

        // Assert - Verificar que los items se agregaron
        assertEquals(2, invoice.getItems().size());
        assertTrue(invoice.getItems().contains(item1));
        assertTrue(invoice.getItems().contains(item2));
    }

    @Test
    @DisplayName("Debe manejar correctamente la lista de shipments")
    void testInvoiceShipmentsList() {
        // Arrange - Crear shipments de prueba
        InvoiceShipment shipment1 = new InvoiceShipment();
        InvoiceShipment shipment2 = new InvoiceShipment();

        // Act - Agregar shipments a la factura
        invoice.getShipments().add(shipment1);
        invoice.getShipments().add(shipment2);

        // Assert - Verificar que los shipments se agregaron
        assertEquals(2, invoice.getShipments().size());
        assertTrue(invoice.getShipments().contains(shipment1));
        assertTrue(invoice.getShipments().contains(shipment2));
    }
}
