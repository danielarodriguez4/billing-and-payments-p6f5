package com.fabrica.p6f5.springapp.invoice.service;

import com.fabrica.p6f5.springapp.audit.model.AuditLog;
import com.fabrica.p6f5.springapp.audit.service.AuditService;
import com.fabrica.p6f5.springapp.exception.BusinessException;
import com.fabrica.p6f5.springapp.exception.ResourceNotFoundException;
import com.fabrica.p6f5.springapp.invoice.dto.CreateInvoiceRequest;
import com.fabrica.p6f5.springapp.invoice.dto.InvoiceResponse;
import com.fabrica.p6f5.springapp.invoice.dto.UpdateInvoiceRequest;
import com.fabrica.p6f5.springapp.invoice.model.Invoice;
import com.fabrica.p6f5.springapp.invoice.model.InvoiceItem;
import com.fabrica.p6f5.springapp.invoice.model.InvoiceShipment;
import com.fabrica.p6f5.springapp.invoice.repository.InvoiceItemRepository;
import com.fabrica.p6f5.springapp.invoice.repository.InvoiceRepository;
import com.fabrica.p6f5.springapp.invoice.repository.InvoiceShipmentRepository;
import com.fabrica.p6f5.springapp.shipment.model.Shipment;
import com.fabrica.p6f5.springapp.shipment.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    private CreateInvoiceRequest createRequest;
    private UpdateInvoiceRequest updateRequest;
    private Invoice invoice;
    private Shipment shipment;
    private Long userId = 1L;

    @BeforeEach
    void setUp() {
        // Setup create request
        createRequest = new CreateInvoiceRequest();
        createRequest.setClientName("Test Client");
        createRequest.setInvoiceDate(LocalDate.now());
        createRequest.setDueDate(LocalDate.now().plusDays(30));
        createRequest.setCurrency("USD");
        createRequest.setTaxAmount(BigDecimal.valueOf(10.0));

        CreateInvoiceRequest.InvoiceItemRequest itemRequest = new CreateInvoiceRequest.InvoiceItemRequest();
        itemRequest.setDescription("Test Item");
        itemRequest.setQuantity(2);
        itemRequest.setUnitPrice(BigDecimal.valueOf(50.0));
        itemRequest.setShipmentId(1L);
        createRequest.setItems(Arrays.asList(itemRequest));
        createRequest.setShipmentIds(Arrays.asList(1L));

        // Setup update request
        updateRequest = new UpdateInvoiceRequest();
        updateRequest.setClientName("Updated Client");
        updateRequest.setInvoiceDate(LocalDate.now());
        updateRequest.setDueDate(LocalDate.now().plusDays(30));
        updateRequest.setCurrency("USD");
        updateRequest.setTaxAmount(BigDecimal.valueOf(15.0));
        updateRequest.setVersion(1L);

        UpdateInvoiceRequest.InvoiceItemRequest updateItemRequest = new UpdateInvoiceRequest.InvoiceItemRequest();
        updateItemRequest.setDescription("Updated Item");
        updateItemRequest.setQuantity(3);
        updateItemRequest.setUnitPrice(BigDecimal.valueOf(60.0));
        updateRequest.setItems(Arrays.asList(updateItemRequest));

        // Setup invoice
        invoice = new Invoice();
        invoice.setId(1L);
        invoice.setInvoiceNumber("INV-12345");
        invoice.setClientName("Test Client");
        invoice.setStatus(Invoice.InvoiceStatus.DRAFT);
        invoice.setSubtotal(BigDecimal.valueOf(100.0));
        invoice.setTaxAmount(BigDecimal.valueOf(10.0));
        invoice.setTotalAmount(BigDecimal.valueOf(110.0));
        invoice.setCurrency("USD");
        invoice.setVersion(1L);
        invoice.setCreatedBy(userId);

        // Setup shipment
        shipment = new Shipment();
        shipment.setId(1L);
    }

    @Test
    void testCreateDraftInvoice_Success() {
        // Arrange
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(invoiceShipmentRepository.existsByShipmentId(1L)).thenReturn(false);
        when(invoiceItemRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        when(invoiceShipmentRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        doNothing().when(auditService).logEvent(anyString(), anyLong(), any(), anyLong(), any(), any(), anyString());

        // Act
        InvoiceResponse response = invoiceService.createDraftInvoice(createRequest, userId);

        // Assert
        assertNotNull(response);
        verify(invoiceRepository, times(2)).save(any(Invoice.class));
        verify(invoiceItemRepository, times(1)).saveAll(anyList());
        verify(invoiceShipmentRepository, times(1)).saveAll(anyList());
        verify(auditService, times(1)).logEvent(eq("Invoice"), anyLong(), eq(AuditLog.AuditAction.CREATE), eq(userId), isNull(), any(), anyString());
    }

    @Test
    void testCreateDraftInvoice_ShipmentNotFound() {
        // Arrange
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> invoiceService.createDraftInvoice(createRequest, userId));
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void testCreateDraftInvoice_ShipmentAlreadyLinked() {
        // Arrange
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(invoiceItemRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        when(invoiceShipmentRepository.existsByShipmentId(1L)).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessException.class, 
            () -> invoiceService.createDraftInvoice(createRequest, userId));
    }

    @Test
    void testUpdateDraftInvoice_Success() {
        // Arrange
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
        when(invoiceItemRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        doNothing().when(invoiceItemRepository).deleteByInvoiceId(1L);
        doNothing().when(invoiceShipmentRepository).deleteByInvoiceId(1L);
        doNothing().when(auditService).saveInvoiceHistory(anyLong(), anyLong(), anyString(), anyString(), any(), anyLong());
        doNothing().when(auditService).logEvent(anyString(), anyLong(), any(), anyLong(), any(), any(), anyString());

        // Act
        InvoiceResponse response = invoiceService.updateDraftInvoice(1L, updateRequest, userId);

        // Assert
        assertNotNull(response);
        verify(invoiceRepository, times(2)).findById(1L);
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
        verify(invoiceItemRepository, times(1)).deleteByInvoiceId(1L);
        verify(auditService, times(1)).logEvent(eq("Invoice"), eq(1L), eq(AuditLog.AuditAction.UPDATE), eq(userId), any(), any(), anyString());
    }

    @Test
    void testUpdateDraftInvoice_InvoiceNotFound() {
        // Arrange
        when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> invoiceService.updateDraftInvoice(999L, updateRequest, userId));
    }

    @Test
    void testUpdateDraftInvoice_CannotBeEdited() {
        // Arrange
        invoice.setStatus(Invoice.InvoiceStatus.ISSUED);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        // Act & Assert
        assertThrows(BusinessException.class, 
            () -> invoiceService.updateDraftInvoice(1L, updateRequest, userId));
    }

    @Test
    void testUpdateDraftInvoice_OptimisticLockingFailure() {
        // Arrange
        updateRequest.setVersion(2L);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        // Act & Assert
        assertThrows(BusinessException.class, 
            () -> invoiceService.updateDraftInvoice(1L, updateRequest, userId));
    }

    @Test
    void testIssueInvoice_Success() {
        // Arrange
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
        doNothing().when(auditService).logEvent(anyString(), anyLong(), any(), anyLong(), any(), any(), anyString());
        doNothing().when(auditService).saveInvoiceHistory(anyLong(), anyLong(), anyString(), anyString(), any(), anyLong());

        // Act
        InvoiceResponse response = invoiceService.issueInvoice(1L, userId);

        // Assert
        assertNotNull(response);
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
        verify(auditService, times(1)).logEvent(eq("Invoice"), eq(1L), eq(AuditLog.AuditAction.ISSUE), eq(userId), any(), any(), anyString());
        verify(auditService, times(1)).saveInvoiceHistory(anyLong(), anyLong(), anyString(), anyString(), any(), eq(userId));
    }

    @Test
    void testIssueInvoice_InvoiceNotFound() {
        // Arrange
        when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> invoiceService.issueInvoice(999L, userId));
    }

    @Test
    void testGetInvoiceById_Success() {
        // Arrange
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        // Act
        InvoiceResponse response = invoiceService.getInvoiceById(1L);

        // Assert
        assertNotNull(response);
        verify(invoiceRepository, times(1)).findById(1L);
    }

    @Test
    void testGetInvoiceById_NotFound() {
        // Arrange
        when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> invoiceService.getInvoiceById(999L));
    }

    @Test
    void testGetInvoicesByStatus_Success() {
        // Arrange
        List<Invoice> invoices = Arrays.asList(invoice);
        when(invoiceRepository.findByStatusOrderByCreatedAtDesc(Invoice.InvoiceStatus.DRAFT))
            .thenReturn(invoices);

        // Act
        List<InvoiceResponse> responses = invoiceService.getInvoicesByStatus(Invoice.InvoiceStatus.DRAFT);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(invoiceRepository, times(1)).findByStatusOrderByCreatedAtDesc(Invoice.InvoiceStatus.DRAFT);
    }

    @Test
    void testGetAllInvoices_Success() {
        // Arrange
        List<Invoice> invoices = Arrays.asList(invoice);
        when(invoiceRepository.findAll()).thenReturn(invoices);

        // Act
        List<InvoiceResponse> responses = invoiceService.getAllInvoices();

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(invoiceRepository, times(1)).findAll();
    }

    @Test
    void testGetAllInvoices_EmptyList() {
        // Arrange
        when(invoiceRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<InvoiceResponse> responses = invoiceService.getAllInvoices();

        // Assert
        assertNotNull(responses);
        assertEquals(0, responses.size());
        verify(invoiceRepository, times(1)).findAll();
    }
}
