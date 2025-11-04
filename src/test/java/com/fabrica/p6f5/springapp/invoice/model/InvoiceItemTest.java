package com.fabrica.p6f5.springapp.invoice.model;

import com.fabrica.p6f5.springapp.factory.FakeInvoiceItemFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la clase InvoiceItem usando el patrón AAA (Arrange-Act-Assert)
 */
@DisplayName("InvoiceItem Model Tests")
class InvoiceItemTest {

    private FakeInvoiceItemFactory factory;

    @BeforeEach
    void setUp() {
        factory = new FakeInvoiceItemFactory();
    }

    @Test
    @DisplayName("Debe crear un item de factura con valores por defecto")
    void testInvoiceItemDefaultValues() {
        // Arrange - No se requiere preparación adicional

        // Act - Crear una nueva instancia usando la factory
        InvoiceItem newItem = factory.createNewInvoiceItem();

        // Assert - Verificar valores por defecto
        assertNull(newItem.getId());
        assertNull(newItem.getInvoice());
        assertNotNull(newItem.getDescription());
        assertNotNull(newItem.getUnitPrice());
        assertNotNull(newItem.getQuantity());
        assertTrue(newItem.getQuantity() >= 1);
    }

    @Test
    @DisplayName("Debe crear un item de factura con todos los campos usando constructor con argumentos")
    void testInvoiceItemAllArgsConstructor() {
        // Arrange - Preparar todos los datos necesarios usando la factory
        Long id = 1L;
        Invoice testInvoice = new Invoice();
        String description = "Producto de prueba";
        Integer quantity = 5;
        BigDecimal unitPrice = new BigDecimal("100.00");
        BigDecimal totalPrice = new BigDecimal("500.00");
        LocalDateTime createdAt = LocalDateTime.now();

        // Act - Crear el item con todos los argumentos usando factory
        InvoiceItem item = factory.createFakeInvoiceItemCustom(
            id, testInvoice, description, quantity, unitPrice, totalPrice, createdAt
        );

        // Assert - Verificar que todos los campos se asignaron correctamente
        assertEquals(id, item.getId());
        assertEquals(testInvoice, item.getInvoice());
        assertEquals(description, item.getDescription());
        assertEquals(quantity, item.getQuantity());
        assertEquals(unitPrice, item.getUnitPrice());
        assertEquals(totalPrice, item.getTotalPrice());
        assertEquals(createdAt, item.getCreatedAt());
    }

    @Test
    @DisplayName("Debe establecer la fecha de creación en PrePersist")
    void testOnCreateSetsCreatedAt() {
        // Arrange - Configurar un item sin fecha de creación usando factory
        InvoiceItem item = factory.createFakeInvoiceItemWithQuantityAndPrice(
            2, new BigDecimal("50.00")
        );
        item.setCreatedAt(null);

        // Act - Llamar al método onCreate
        item.onCreate();

        // Assert - Verificar que la fecha de creación se estableció
        assertNotNull(item.getCreatedAt());
        assertTrue(item.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(item.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    @DisplayName("Debe calcular el precio total en PrePersist cuando totalPrice es null")
    void testOnCreateCalculatesTotalPrice() {
        // Arrange - Configurar un item sin precio total usando factory
        InvoiceItem item = factory.createFakeInvoiceItemWithQuantityAndPrice(
            3, new BigDecimal("75.50")
        );
        item.setTotalPrice(null);

        // Act - Llamar al método onCreate
        item.onCreate();

        // Assert - Verificar que el precio total se calculó correctamente (75.50 * 3 = 226.50)
        BigDecimal expectedTotal = new BigDecimal("226.50");
        assertEquals(0, expectedTotal.compareTo(item.getTotalPrice()));
    }

    @Test
    @DisplayName("Debe preservar el precio total existente en PrePersist")
    void testOnCreatePreservesExistingTotalPrice() {
        // Arrange - Configurar un item con precio total ya establecido usando factory
        BigDecimal existingTotalPrice = new BigDecimal("150.00");
        InvoiceItem item = factory.createFakeInvoiceItemWithQuantityAndPrice(
            2, new BigDecimal("100.00")
        );
        item.setTotalPrice(existingTotalPrice);

        // Act - Llamar al método onCreate
        item.onCreate();

        // Assert - Verificar que el precio total existente se preservó
        assertEquals(existingTotalPrice, item.getTotalPrice());
        assertEquals(0, new BigDecimal("150.00").compareTo(item.getTotalPrice()));
    }

    @Test
    @DisplayName("Debe calcular el precio total correctamente con el método calculateTotal")
    void testCalculateTotalWithValidValues() {
        // Arrange - Configurar precio unitario y cantidad usando factory
        InvoiceItem item = factory.createFakeInvoiceItemWithQuantityAndPrice(
            4, new BigDecimal("25.75")
        );
        item.setTotalPrice(null);

        // Act - Calcular el total
        item.calculateTotal();

        // Assert - Verificar que el total se calculó correctamente (25.75 * 4 = 103.00)
        BigDecimal expectedTotal = new BigDecimal("103.00");
        assertEquals(0, expectedTotal.compareTo(item.getTotalPrice()));
    }

    @Test
    @DisplayName("Debe calcular precio total con cantidad 1")
    void testCalculateTotalWithQuantityOne() {
        // Arrange - Configurar precio unitario y cantidad 1 usando factory
        BigDecimal unitPrice = new BigDecimal("99.99");
        InvoiceItem item = factory.createFakeInvoiceItemWithQuantityAndPrice(1, unitPrice);
        item.setTotalPrice(null);

        // Act - Calcular el total
        item.calculateTotal();

        // Assert - Verificar que el total es igual al precio unitario
        assertEquals(0, unitPrice.compareTo(item.getTotalPrice()));
    }

    @Test
    @DisplayName("Debe calcular precio total con valores decimales")
    void testCalculateTotalWithDecimalValues() {
        // Arrange - Configurar precio unitario con decimales usando factory
        InvoiceItem item = factory.createFakeInvoiceItemWithQuantityAndPrice(
            10, new BigDecimal("12.345")
        );
        item.setTotalPrice(null);

        // Act - Calcular el total
        item.calculateTotal();

        // Assert - Verificar que el total se calculó correctamente (12.345 * 10 = 123.45)
        BigDecimal expectedTotal = new BigDecimal("123.450");
        assertEquals(0, expectedTotal.compareTo(item.getTotalPrice()));
    }

    @Test
    @DisplayName("No debe calcular precio total si unitPrice es null")
    void testCalculateTotalWithNullUnitPrice() {
        // Arrange - Configurar item con unitPrice null usando factory
        InvoiceItem item = factory.createFakeInvoiceItemWithQuantity(5);
        item.setUnitPrice(null);
        item.setTotalPrice(null);

        // Act - Intentar calcular el total
        item.calculateTotal();

        // Assert - Verificar que el precio total sigue siendo null
        assertNull(item.getTotalPrice());
    }

    @Test
    @DisplayName("No debe calcular precio total si quantity es null")
    void testCalculateTotalWithNullQuantity() {
        // Arrange - Configurar item con quantity null usando factory
        InvoiceItem item = factory.createFakeInvoiceItemWithUnitPrice(new BigDecimal("50.00"));
        item.setQuantity(null);
        item.setTotalPrice(null);

        // Act - Intentar calcular el total
        item.calculateTotal();

        // Assert - Verificar que el precio total sigue siendo null
        assertNull(item.getTotalPrice());
    }

    @Test
    @DisplayName("No debe calcular precio total si ambos valores son null")
    void testCalculateTotalWithBothValuesNull() {
        // Arrange - Configurar item con ambos valores null usando factory
        InvoiceItem item = factory.createNewInvoiceItem();
        item.setUnitPrice(null);
        item.setQuantity(null);
        item.setTotalPrice(null);

        // Act - Intentar calcular el total
        item.calculateTotal();

        // Assert - Verificar que el precio total sigue siendo null
        assertNull(item.getTotalPrice());
    }

    @Test
    @DisplayName("Debe establecer y obtener el ID correctamente")
    void testSetAndGetId() {
        // Arrange - Preparar un item usando factory
        Long expectedId = 123L;
        InvoiceItem item = factory.createFakeInvoiceItem();

        // Act - Establecer el ID
        item.setId(expectedId);

        // Assert - Verificar que el ID se estableció correctamente
        assertEquals(expectedId, item.getId());
    }

    @Test
    @DisplayName("Debe establecer y obtener la factura correctamente")
    void testSetAndGetInvoice() {
        // Arrange - Preparar una factura de prueba y un item usando factory
        Invoice testInvoice = new Invoice();
        testInvoice.setId(10L);
        testInvoice.setInvoiceNumber("INV-001");
        InvoiceItem item = factory.createFakeInvoiceItem();

        // Act - Establecer la factura
        item.setInvoice(testInvoice);

        // Assert - Verificar que la factura se estableció correctamente
        assertNotNull(item.getInvoice());
        assertEquals(testInvoice, item.getInvoice());
        assertEquals(10L, item.getInvoice().getId());
        assertEquals("INV-001", item.getInvoice().getInvoiceNumber());
    }

    @Test
    @DisplayName("Debe establecer y obtener la descripción correctamente")
    void testSetAndGetDescription() {
        // Arrange - Preparar una descripción de prueba y un item usando factory
        String expectedDescription = "Producto de prueba con descripción detallada";
        InvoiceItem item = factory.createFakeInvoiceItem();

        // Act - Establecer la descripción
        item.setDescription(expectedDescription);

        // Assert - Verificar que la descripción se estableció correctamente
        assertEquals(expectedDescription, item.getDescription());
    }

    @Test
    @DisplayName("Debe establecer y obtener la cantidad correctamente")
    void testSetAndGetQuantity() {
        // Arrange - Preparar una cantidad de prueba y un item usando factory
        Integer expectedQuantity = 15;
        InvoiceItem item = factory.createFakeInvoiceItem();

        // Act - Establecer la cantidad
        item.setQuantity(expectedQuantity);

        // Assert - Verificar que la cantidad se estableció correctamente
        assertEquals(expectedQuantity, item.getQuantity());
    }

    @Test
    @DisplayName("Debe establecer y obtener el precio unitario correctamente")
    void testSetAndGetUnitPrice() {
        // Arrange - Preparar un precio unitario de prueba y un item usando factory
        BigDecimal expectedUnitPrice = new BigDecimal("299.99");
        InvoiceItem item = factory.createFakeInvoiceItem();

        // Act - Establecer el precio unitario
        item.setUnitPrice(expectedUnitPrice);

        // Assert - Verificar que el precio unitario se estableció correctamente
        assertEquals(expectedUnitPrice, item.getUnitPrice());
        assertEquals(0, new BigDecimal("299.99").compareTo(item.getUnitPrice()));
    }

    @Test
    @DisplayName("Debe establecer y obtener el precio total correctamente")
    void testSetAndGetTotalPrice() {
        // Arrange - Preparar un precio total de prueba y un item usando factory
        BigDecimal expectedTotalPrice = new BigDecimal("1500.00");
        InvoiceItem item = factory.createFakeInvoiceItem();

        // Act - Establecer el precio total
        item.setTotalPrice(expectedTotalPrice);

        // Assert - Verificar que el precio total se estableció correctamente
        assertEquals(expectedTotalPrice, item.getTotalPrice());
        assertEquals(0, new BigDecimal("1500.00").compareTo(item.getTotalPrice()));
    }

    @Test
    @DisplayName("Debe manejar correctamente cantidades grandes")
    void testCalculateTotalWithLargeQuantity() {
        // Arrange - Configurar una cantidad grande usando factory
        InvoiceItem item = factory.createFakeInvoiceItemWithQuantityAndPrice(
            1000, new BigDecimal("10.00")
        );
        item.setTotalPrice(null);

        // Act - Calcular el total
        item.calculateTotal();

        // Assert - Verificar que el cálculo es correcto (10.00 * 1000 = 10000.00)
        BigDecimal expectedTotal = new BigDecimal("10000.00");
        assertEquals(0, expectedTotal.compareTo(item.getTotalPrice()));
    }

    @Test
    @DisplayName("Debe manejar correctamente precios muy pequeños")
    void testCalculateTotalWithSmallPrice() {
        // Arrange - Configurar un precio muy pequeño usando factory
        InvoiceItem item = factory.createFakeInvoiceItemWithQuantityAndPrice(
            3, new BigDecimal("0.01")
        );
        item.setTotalPrice(null);

        // Act - Calcular el total
        item.calculateTotal();

        // Assert - Verificar que el cálculo es correcto (0.01 * 3 = 0.03)
        BigDecimal expectedTotal = new BigDecimal("0.03");
        assertEquals(0, expectedTotal.compareTo(item.getTotalPrice()));
    }

    @Test
    @DisplayName("Debe recalcular el precio total cuando cambian los valores")
    void testRecalculateTotalAfterChanges() {
        // Arrange - Configurar valores iniciales usando factory
        InvoiceItem item = factory.createFakeInvoiceItemWithQuantityAndPrice(
            2, new BigDecimal("50.00")
        );
        BigDecimal initialTotal = item.getTotalPrice();

        // Act - Cambiar valores y recalcular
        item.setUnitPrice(new BigDecimal("75.00"));
        item.setQuantity(3);
        item.calculateTotal();

        // Assert - Verificar que el total se recalculó correctamente
        assertNotEquals(initialTotal, item.getTotalPrice());
        BigDecimal expectedNewTotal = new BigDecimal("225.00");
        assertEquals(0, expectedNewTotal.compareTo(item.getTotalPrice()));
    }

    @Test
    @DisplayName("Debe manejar correctamente el shipment")
    void testSetAndGetShipment() {
        // Arrange - Crear un item usando factory
        InvoiceItem item = factory.createFakeInvoiceItem();
        com.fabrica.p6f5.springapp.shipment.model.Shipment shipment = null;

        // Act - Establecer el shipment
        item.setShipment(shipment);

        // Assert - Verificar que el shipment se estableció correctamente
        assertEquals(shipment, item.getShipment());
    }

    @Test
    @DisplayName("Debe mantener la relación con la factura correctamente")
    void testInvoiceRelationship() {
        // Arrange - Crear una factura y un item usando factory
        Invoice parentInvoice = new Invoice();
        parentInvoice.setId(100L);
        parentInvoice.setInvoiceNumber("INV-2025-001");

        InvoiceItem item = factory.createFakeInvoiceItemWithDescription("Item de prueba");
        item.setQuantity(5);
        item.setUnitPrice(new BigDecimal("20.00"));

        // Act - Establecer la relación
        item.setInvoice(parentInvoice);

        // Assert - Verificar que la relación se estableció correctamente
        assertNotNull(item.getInvoice());
        assertEquals(100L, item.getInvoice().getId());
        assertEquals("INV-2025-001", item.getInvoice().getInvoiceNumber());
    }

    @Test
    @DisplayName("Debe crear un item completo con todos los campos válidos")
    void testCreateCompleteInvoiceItem() {
        // Arrange - Preparar todos los datos para un item completo usando factory
        Invoice testInvoice = new Invoice();
        testInvoice.setId(1L);

        String description = "Laptop Dell XPS 15";
        Integer quantity = 2;
        BigDecimal unitPrice = new BigDecimal("1500.00");

        // Act - Configurar el item completo usando factory
        InvoiceItem item = factory.createFakeInvoiceItemWithQuantityAndPrice(quantity, unitPrice);
        item.setInvoice(testInvoice);
        item.setDescription(description);

        // Assert - Verificar que todos los campos están correctos
        assertNotNull(item.getInvoice());
        assertEquals(description, item.getDescription());
        assertEquals(quantity, item.getQuantity());
        assertEquals(unitPrice, item.getUnitPrice());
        assertEquals(0, new BigDecimal("3000.00").compareTo(item.getTotalPrice()));
    }
}
