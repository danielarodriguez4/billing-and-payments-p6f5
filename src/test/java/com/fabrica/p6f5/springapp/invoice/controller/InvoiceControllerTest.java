package com.fabrica.p6f5.springapp.invoice.controller;

import com.fabrica.p6f5.springapp.factory.FakeUserFactory;
import com.fabrica.p6f5.springapp.invoice.dto.CreateInvoiceRequest;
import com.fabrica.p6f5.springapp.invoice.dto.InvoiceResponse;
import com.fabrica.p6f5.springapp.invoice.service.InvoiceService;
import com.fabrica.p6f5.springapp.dto.ApiResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class InvoiceControllerTest {

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private InvoiceController invoiceController;

    private CreateInvoiceRequest request;
    private InvoiceResponse response;
    private FakeUserFactory fakeUserFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fakeUserFactory = new FakeUserFactory();

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
        // Arrange
        var user = fakeUserFactory.createFakeUser();
        when(invoiceService.createDraftInvoice(ArgumentMatchers.any(CreateInvoiceRequest.class), ArgumentMatchers.anyLong()))
                .thenReturn(response);

        // Act
        ResponseEntity<ApiResponse<InvoiceResponse>> result = invoiceController.createDraftInvoice(request, user);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getBody());
        Assertions.assertTrue(result.getBody().isSuccess());
        assertEquals("Test Client", result.getBody().getData().getClientName());
        Mockito.verify(invoiceService, Mockito.times(1)).createDraftInvoice(ArgumentMatchers.any(), ArgumentMatchers.anyLong());
    }

    @Test
    void testGetInvoiceById_Success() {
        // Arrange
        when(invoiceService.getInvoiceById(1L)).thenReturn(response);

        // Act
        ResponseEntity<ApiResponse<InvoiceResponse>> result = invoiceController.getInvoiceById(1L);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getBody());
        assertEquals("Test Client", result.getBody().getData().getClientName());
        Mockito.verify(invoiceService, Mockito.times(1)).getInvoiceById(1L);
    }

    @Test
        // Assert
    void testGetAllInvoices() {

        // Arrange
        ResponseEntity<ApiResponse<List<InvoiceResponse>>> result = invoiceController.getAllInvoices();

        // Act
        assertEquals(200, result.getStatusCode().value());
        Assertions.assertNotNull(result.getBody());
        // Assert
        assertEquals(1, result.getBody().getData().size());
    }
}
