package com.fabrica.p6f5.springapp.invoice.service;

import com.fabrica.p6f5.springapp.audit.service.AuditService;
import com.fabrica.p6f5.springapp.common.exception.ResourceNotFoundException;
import com.fabrica.p6f5.springapp.invoice.dto.CreateInvoiceRequest;
import com.fabrica.p6f5.springapp.invoice.dto.InvoiceResponse;
import com.fabrica.p6f5.springapp.invoice.model.Invoice;
import com.fabrica.p6f5.springapp.invoice.repository.InvoiceRepository;
import com.fabrica.p6f5.springapp.invoice.repository.InvoiceItemRepository;
import com.fabrica.p6f5.springapp.invoice.repository.InvoiceShipmentRepository;
import com.fabrica.p6f5.springapp.shipment.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private InvoiceItemRepository invoiceItemRepository;
    @Mock
    private InvoiceShipmentRepository invoiceShipmentRepository;
    @Mock
    private ShipmentRepository shipmentRepository;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private InvoiceService invoiceService;

    private Invoice invoice;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        invoice = new Invoice();
        invoice.setId(1L);
        invoice.setClientName("Client");
        invoice.setStatus(Invoice.InvoiceStatus.DRAFT);
        invoice.setTaxAmount(BigDecimal.TEN);
    }

    @Test
    void testGetInvoiceById_Success() {
        // Arrange
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        // Act
        InvoiceResponse result = invoiceService.getInvoiceById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Client", result.getClientName());
        verify(invoiceRepository, times(1)).findById(1L);
    }

    @Test
    void testGetInvoiceById_NotFound() {
        // Arrange
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> invoiceService.getInvoiceById(99L));
    }

    @Test
    void testCreateDraftInvoice_Success() {
        // Arrange
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setClientName("Client A");
        request.setInvoiceDate(LocalDate.now());
        request.setDueDate(LocalDate.now().plusDays(5));
        request.setTaxAmount(BigDecimal.valueOf(5));
        request.setCurrency("USD");
        request.setItems(java.util.Collections.emptyList());

        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> {
            Invoice inv = i.getArgument(0);
            inv.setId(1L);
            return inv;
        });
        when(invoiceRepository.findById(anyLong())).thenReturn(Optional.of(invoice));

        // Act
        InvoiceResponse result = invoiceService.createDraftInvoice(request, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("Client", result.getClientName());
        verify(invoiceRepository, atLeastOnce()).save(any(Invoice.class));
    }
}
