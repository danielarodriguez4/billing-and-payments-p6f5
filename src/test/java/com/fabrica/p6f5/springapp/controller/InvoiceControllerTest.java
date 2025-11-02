package com.fabrica.p6f5.springapp.invoice.controller;

import com.fabrica.p6f5.springapp.invoice.dto.CreateInvoiceRequest;
import com.fabrica.p6f5.springapp.invoice.dto.InvoiceResponse;
import com.fabrica.p6f5.springapp.invoice.service.InvoiceService;
import com.fabrica.p6f5.springapp.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoiceControllerTest {

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private InvoiceController invoiceController;

    private CreateInvoiceRequest request;
    private InvoiceResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        request = new CreateInvoiceRequest();
        request.setClientName("Test Client");
        request.setCurrency("USD");
        request.setInvoiceDate(LocalDate.now());
        request.setDueDate(LocalDate.now().plusDays(10));
        request.setTaxAmount(BigDecimal.valueOf(10));
        request.setItems(Collections.emptyList());

        response = new InvoiceResponse();
        response.setId(1L);
        response.setClientName("Test Client");
        response.setTotalAmount(BigDecimal.valueOf(100));
    }

    @Test
    void testCreateDraftInvoice_Success() {
        when(invoiceService.createDraftInvoice(any(CreateInvoiceRequest.class), anyLong()))
                .thenReturn(response);

        ResponseEntity<ApiResponse<InvoiceResponse>> result = invoiceController.createDraftInvoice(request, 1L);

        assertNotNull(result);
        assertTrue(result.getBody().isSuccess());
        assertEquals("Test Client", result.getBody().getData().getClientName());
        verify(invoiceService, times(1)).createDraftInvoice(any(), anyLong());
    }

    @Test
    void testGetInvoiceById_Success() {
        when(invoiceService.getInvoiceById(1L)).thenReturn(response);

        ResponseEntity<ApiResponse<InvoiceResponse>> result = invoiceController.getInvoiceById(1L);

        assertNotNull(result);
        assertEquals("Test Client", result.getBody().getData().getClientName());
        verify(invoiceService, times(1)).getInvoiceById(1L);
    }

    @Test
    void testGetAllInvoices_Success() {
        when(invoiceService.getAllInvoices()).thenReturn(Collections.singletonLi
