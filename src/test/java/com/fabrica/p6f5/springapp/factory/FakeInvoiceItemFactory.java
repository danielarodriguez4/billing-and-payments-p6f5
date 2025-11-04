package com.fabrica.p6f5.springapp.factory;

import com.fabrica.p6f5.springapp.invoice.model.Invoice;
import com.fabrica.p6f5.springapp.invoice.model.InvoiceItem;
import net.datafaker.Faker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Factory para crear instancias de InvoiceItem con datos fake para testing
 */
public class FakeInvoiceItemFactory {

    private final Faker faker = new Faker();

    /**
     * Crea un InvoiceItem con valores fake por defecto
     */
    public InvoiceItem createFakeInvoiceItem() {
        InvoiceItem item = new InvoiceItem();
        item.setId(faker.number().randomNumber());
        item.setDescription(faker.commerce().productName());
        item.setQuantity(faker.number().numberBetween(1, 100));
        item.setUnitPrice(generateRandomPrice());
        item.calculateTotal();
        item.setCreatedAt(LocalDateTime.now());
        return item;
    }

    /**
     * Crea un InvoiceItem con una factura padre específica
     */
    public InvoiceItem createFakeInvoiceItemWithInvoice(Invoice invoice) {
        InvoiceItem item = createFakeInvoiceItem();
        item.setInvoice(invoice);
        return item;
    }

    /**
     * Crea un InvoiceItem con descripción personalizada
     */
    public InvoiceItem createFakeInvoiceItemWithDescription(String description) {
        InvoiceItem item = createFakeInvoiceItem();
        item.setDescription(description);
        return item;
    }

    /**
     * Crea un InvoiceItem con cantidad específica
     */
    public InvoiceItem createFakeInvoiceItemWithQuantity(Integer quantity) {
        InvoiceItem item = createFakeInvoiceItem();
        item.setQuantity(quantity);
        item.calculateTotal();
        return item;
    }

    /**
     * Crea un InvoiceItem con precio unitario específico
     */
    public InvoiceItem createFakeInvoiceItemWithUnitPrice(BigDecimal unitPrice) {
        InvoiceItem item = createFakeInvoiceItem();
        item.setUnitPrice(unitPrice);
        item.calculateTotal();
        return item;
    }

    /**
     * Crea un InvoiceItem con cantidad y precio unitario específicos
     */
    public InvoiceItem createFakeInvoiceItemWithQuantityAndPrice(Integer quantity, BigDecimal unitPrice) {
        InvoiceItem item = createFakeInvoiceItem();
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.calculateTotal();
        return item;
    }

    /**
     * Crea un InvoiceItem sin calcular el total (para probar el método calculateTotal)
     */
    public InvoiceItem createFakeInvoiceItemWithoutTotal() {
        InvoiceItem item = new InvoiceItem();
        item.setId(faker.number().randomNumber());
        item.setDescription(faker.commerce().productName());
        item.setQuantity(faker.number().numberBetween(1, 100));
        item.setUnitPrice(generateRandomPrice());
        // No se calcula el total intencionalmente
        item.setCreatedAt(LocalDateTime.now());
        return item;
    }

    /**
     * Crea un InvoiceItem con todos los campos personalizados
     */
    public InvoiceItem createFakeInvoiceItemCustom(Long id, Invoice invoice, String description,
                                                    Integer quantity, BigDecimal unitPrice,
                                                    BigDecimal totalPrice, LocalDateTime createdAt) {
        return new InvoiceItem(id, invoice, null, description, quantity, unitPrice, totalPrice, createdAt);
    }

    /**
     * Crea un InvoiceItem básico sin ID ni fechas (para simular un nuevo item)
     */
    public InvoiceItem createNewInvoiceItem() {
        InvoiceItem item = new InvoiceItem();
        item.setDescription(faker.commerce().productName());
        item.setQuantity(faker.number().numberBetween(1, 50));
        item.setUnitPrice(generateRandomPrice());
        return item;
    }

    /**
     * Genera un precio aleatorio entre 1.00 y 9999.99
     */
    private BigDecimal generateRandomPrice() {
        double price = faker.number().randomDouble(2, 1, 9999);
        return BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
    }
}

