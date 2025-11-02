package com.fabrica.p6f5.springapp.invoice.controller;

import com.fabrica.p6f5.springapp.audit.model.InvoiceHistory;
import com.fabrica.p6f5.springapp.audit.service.AuditService;
import com.fabrica.p6f5.springapp.dto.ApiResponse;
import com.fabrica.p6f5.springapp.invoice.dto.InvoiceHistoryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoiceHistoryControllerTest {

    @Mock
    private AuditService auditService;

    @InjectMocks
    private InvoiceHistoryController invoiceHistoryController;

    private InvoiceHistory mockHistory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockHistory = new InvoiceHistory();
        mockHistory.setId(1L);
        mockHistory.setInvoiceId(100L);
        mockHistory.setVersion(1);
        mockHistory.setFiscalFolio("FOLIO-001");
        mockHistory.setInvoiceNumber("INV-001");
        mockHistory.setInvoiceData("{json}");
        mockHistory.setCreatedBy(10L);
        mockHistory.setCreatedAt(LocalDateTime.now());
        mockHistory.setIsReverted(false);
    }

    @Test
    void testGetInvoiceHistory_Success() {
        // Arrange
        when(auditService.getInvoiceHistory(100L)).thenReturn(Arrays.asList(mockHistory));

        // Act
        ResponseEntity<ApiResponse<List<InvoiceHistoryResponse>>> response =
                invoiceHistoryController.getInvoiceHistory(100L);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Invoice history retrieved successfully", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().size());
        verify(auditService, times(1)).getInvoiceHistory(100L);
    }

    @Test
    void testGetInvoiceVersion_Found() {
        // Arrange
        when(auditService.getInvoiceHistoryVersion(100L, 1)).thenReturn(Optional.of(mockHistory));

        // Act
        ResponseEntity<ApiResponse<InvoiceHistoryResponse>> response =
                invoiceHistoryController.getInvoiceVersion(100L, 1);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Invoice version retrieved successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        verify(auditService, times(1)).getInvoiceHistoryVersion(100L, 1);
    }

    @Test
    void testGetInvoiceVersion_NotFound() {
        // Arrange
        when(auditService.getInvoiceHistoryVersion(100L, 99)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ApiResponse<InvoiceHistoryResponse>> response =
                invoiceHistoryController.getInvoiceVersion(100L, 99);

        assertNotNull(response);
        // Assert
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Version not found", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        verify(auditService, times(1)).getInvoiceHistoryVersion(100L, 99);
    }
}
