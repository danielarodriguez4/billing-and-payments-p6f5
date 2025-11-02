package com.fabrica.p6f5.springapp.invoice.controller;

import com.fabrica.p6f5.springapp.invoice.dto.InvoiceHistoryResponse;
import com.fabrica.p6f5.springapp.invoice.service.InvoiceHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoiceHistoryControllerTest {

    @Mock
    private InvoiceHistoryService invoiceHistoryService;

    @InjectMocks
    private InvoiceHistoryController invoiceHistoryController;

    private InvoiceHistoryResponse historyResponse;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        historyResponse = new InvoiceHistoryResponse();
        historyResponse.setId(1L);
        historyResponse.setInvoiceId(100L);
        historyResponse.setAction("CREATE");
        historyResponse.setTimestamp(LocalDateTime.now());
        historyResponse.setUserName("admin");
        historyResponse.setDetails("Factura creada");
    }

    @Test
    void testGetAllHistories() {
        when(invoiceHistoryService.getAllHistories()).thenReturn(List.of(historyResponse));

        ResponseEntity<List<InvoiceHistoryResponse>> result = invoiceHistoryController.getAllHistories();

        assertEquals(200, result.getStatusCodeValue());
        assertEquals(1, result.getBody().size());
        assertEquals("CREATE", result.getBody().get(0).getAction());
        verify(invoiceHistoryService, times(1)).getAllHistories();
    }

    @Test
    void testGetAllHistories_EmptyList() {
        when(invoiceHistoryService.getAllHistories()).thenReturn(Collections.emptyList());

        ResponseEntity<List<InvoiceHistoryResponse>> result = invoiceHistoryController.getAllHistories();

        assertEquals(200, result.getStatusCodeValue());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    void testGetHistoryById_Success() {
        when(invoiceHistoryService.getHistoryById(1L)).thenReturn(historyResponse);

        ResponseEntity<InvoiceHistoryResponse> result = invoiceHistoryController.getHistoryById(1L);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals(100L, result.getBody().getInvoiceId());
        verify(invoiceHistoryService, times(1)).getHistoryById(1L);
    }

    @Test
    void testGetHistoryById_NotFound() {
        when(invoiceHistoryService.getHistoryById(99L)).thenReturn(null);

        ResponseEntity<InvoiceHistoryResponse> result = invoiceHistoryController.getHistoryById(99L);

        assertEquals(404, result.getStatusCodeValue());
        assertNull(result.getBody());
    }

    @Test
    void testGetHistoriesByInvoiceId_Success() {
        when(invoiceHistoryService.getHistoriesByInvoiceId(100L)).thenReturn(List.of(historyResponse));

        ResponseEntity<List<InvoiceHistoryResponse>> result = invoiceHistoryController.getHistoriesByInvoiceId(100L);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals(1, result.getBody().size());
        assertEquals("CREATE", result.getBody().get(0).getAction());
    }

    @Test
    void testGetHistoriesByInvoiceId_Empty() {
        when(invoiceHistoryService.getHistoriesByInvoiceId(100L)).thenReturn(Collections.emptyList());

        ResponseEntity<List<InvoiceHistoryResponse>> result = invoiceHistoryController.getHistoriesByInvoiceId(100L);

        assertEquals(200, result.getStatusCodeValue());
        assertTrue(result.getBody().isEmpty());
    }
}
