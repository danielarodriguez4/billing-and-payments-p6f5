package com.fabrica.p6f5.springapp.invoice.controller;

import com.fabrica.p6f5.springapp.invoice.dto.CreateInvoiceRequest;
import com.fabrica.p6f5.springapp.invoice.dto.InvoiceResponse;
import com.fabrica.p6f5.springapp.invoice.dto.UpdateInvoiceRequest;
import com.fabrica.p6f5.springapp.invoice.model.Invoice;
import com.fabrica.p6f5.springapp.invoice.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoiceControllerTest {

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private InvoiceController invoiceController;

    private CreateInvoiceRequest createRequest;
    private UpdateInvoiceRequest updateRequest;
    private InvoiceResponse response;
    private Long userId = 1L;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        createRequest = new CreateInvoiceRequest();
        createRequest.setClientName("Client A");
        createRequest.setInvoiceDate(LocalDate.now());
        createRequest.setDueDate(LocalDate.now().plusDays(30));
        createRequest.setCurrency("USD");
        createRequest.setTaxAmount(BigDecimal.valueOf(5.0));

        updateRequest = new UpdateInvoiceRequest();
        updateRequest.setClientName("Updated Client");
        updateRequest.setVersion(1L);

        response = new InvoiceResponse();
        response.setId(1L);
        response.setClientName("Client A");
    }

    @Test
    void testCreateDraftInvoice() {
        when(invoiceService.createDraftInvoice(any(CreateInvoiceRequest.class), eq(userId)))
                .thenReturn(response);

        ResponseEntity<InvoiceResponse> result = invoiceController.createDraftInvoice(createRequest, userId);

        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
        assertEquals("Client A", result.getBody().getClientName());
    }

    @Test
    void testUpdateDraftInvoice() {
        when(invoiceService.updateDraftInvoice(eq(1L), any(UpdateInvoiceRequest.class), eq(userId)))
                .thenReturn(response);

        ResponseEntity<InvoiceResponse> result = invoiceController.updateDraftInvoice(1L, updateRequest, userId);

        assertEquals(200, result.getStatusCodeValue());
        verify(invoiceService, times(1)).updateDraftInvoice(1L, updateRequest, userId);
    }

    @Test
    void testIssueInvoice() {
        when(invoiceService.issueInvoice(1L, userId)).thenReturn(response);

        ResponseEntity<InvoiceResponse> result = invoiceController.issueInvoice(1L, userId);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals(1L, result.getBody().getId());
    }

    @Test
    void testGetInvoiceById() {
        when(invoiceService.getInvoiceById(1L)).thenReturn(response);

        ResponseEntity<InvoiceResponse> result = invoiceController.getInvoiceById(1L);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals("Client A", result.getBody().getClientName());
    }

    @Test
    void testGetInvoicesByStatus() {
        when(invoiceService.getInvoicesByStatus(Invoice.InvoiceStatus.DRAFT))
                .thenReturn(List.of(response));

        ResponseEntity<List<InvoiceResponse>> result = invoiceController.getInvoicesByStatus("DRAFT");

        assertEquals(200, result.getStatusCodeValue());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void testGetInvoicesByStatus_InvalidStatus() {
        ResponseEntity<List<InvoiceResponse>> result = invoiceController.getInvoicesByStatus("INVALID");

        assertEquals(400, result.getStatusCodeValue());
    }

    @Test
    void testGetAllInvoices() {
        when(invoiceService.getAllInvoices()).thenReturn(Collections.singletonList(response));

        ResponseEntity<List<InvoiceResponse>> result = invoiceController.getAllInvoices();

        assertEquals(200, result.getStatusCodeValue());
        assertEquals(1, result.getBody().size());
    }
}
