package com.fabrica.p6f5.springapp.invoice.controller;

import com.fabrica.p6f5.springapp.dto.ApiResponse;
import com.fabrica.p6f5.springapp.entity.User;
import com.fabrica.p6f5.springapp.invoice.dto.CreateInvoiceRequest;
import com.fabrica.p6f5.springapp.invoice.dto.InvoiceResponse;
import com.fabrica.p6f5.springapp.invoice.dto.UpdateInvoiceRequest;
import com.fabrica.p6f5.springapp.invoice.model.Invoice;
import com.fabrica.p6f5.springapp.invoice.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Invoice Controller following Single Responsibility Principle.
 * Handles all invoice-related HTTP requests.
 */
@RestController
@RequestMapping("/api/v1/invoices")
@Tag(name = "Invoice API", description = "API for managing invoices")
public class InvoiceController {
    
    private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);
    
    @Autowired
    private InvoiceService invoiceService;
    
    @Autowired
    private com.fabrica.p6f5.springapp.pdf.service.PdfService pdfService;
    
    /**
     * Create a draft invoice
     */
    @PostMapping
    @Operation(summary = "Create a draft invoice", description = "Creates a new invoice in DRAFT status")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createDraftInvoice(
            @Valid @RequestBody CreateInvoiceRequest request,
            @AuthenticationPrincipal User user) {
        logger.info("Creating draft invoice by user: {}", user.getUsername());
        InvoiceResponse response = invoiceService.createDraftInvoice(request, user.getId());
        ApiResponse<InvoiceResponse> apiResponse = new ApiResponse<>(
            true,
            "Draft invoice created successfully",
            response
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }
    
    /**
     * Update a draft invoice
     */
    @PutMapping("/{invoiceId}")
    @Operation(summary = "Update a draft invoice", description = "Updates an existing invoice in DRAFT status with optimistic concurrency control")
    public ResponseEntity<ApiResponse<InvoiceResponse>> updateDraftInvoice(
            @Parameter(description = "Invoice ID") @PathVariable Long invoiceId,
            @Valid @RequestBody UpdateInvoiceRequest request,
            @AuthenticationPrincipal User user) {
        logger.info("Updating draft invoice id: {} by user: {}", invoiceId, user.getUsername());
        InvoiceResponse response = invoiceService.updateDraftInvoice(invoiceId, request, user.getId());
        ApiResponse<InvoiceResponse> apiResponse = new ApiResponse<>(
            true,
            "Draft invoice updated successfully",
            response
        );
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * Issue an invoice
     */
    @PostMapping("/{invoiceId}/issue")
    @Operation(summary = "Issue an invoice", description = "Transitions an invoice from DRAFT to ISSUED status")
    public ResponseEntity<ApiResponse<InvoiceResponse>> issueInvoice(
            @Parameter(description = "Invoice ID") @PathVariable Long invoiceId,
            @AuthenticationPrincipal User user) {
        logger.info("Issuing invoice id: {} by user: {}", invoiceId, user.getUsername());
        InvoiceResponse response = invoiceService.issueInvoice(invoiceId, user.getId());
        ApiResponse<InvoiceResponse> apiResponse = new ApiResponse<>(
            true,
            "Invoice issued successfully",
            response
        );
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * Get invoice by ID
     */
    @GetMapping("/{invoiceId}")
    @Operation(summary = "Get invoice by ID", description = "Retrieves an invoice by its ID")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(
            @Parameter(description = "Invoice ID") @PathVariable Long invoiceId) {
        logger.info("Getting invoice id: {}", invoiceId);
        InvoiceResponse response = invoiceService.getInvoiceById(invoiceId);
        ApiResponse<InvoiceResponse> apiResponse = new ApiResponse<>(
            true,
            "Invoice retrieved successfully",
            response
        );
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * Get all invoices
     */
    @GetMapping
    @Operation(summary = "Get all invoices", description = "Retrieves all invoices")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getAllInvoices() {
        logger.info("Getting all invoices");
        List<InvoiceResponse> response = invoiceService.getAllInvoices();
        ApiResponse<List<InvoiceResponse>> apiResponse = new ApiResponse<>(
            true,
            "Invoices retrieved successfully",
            response
        );
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * Get invoices by status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get invoices by status", description = "Retrieves invoices filtered by status")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoicesByStatus(
            @Parameter(description = "Invoice status") @PathVariable String status) {
        logger.info("Getting invoices with status: {}", status);
        Invoice.InvoiceStatus invoiceStatus = Invoice.InvoiceStatus.valueOf(status.toUpperCase());
        List<InvoiceResponse> response = invoiceService.getInvoicesByStatus(invoiceStatus);
        ApiResponse<List<InvoiceResponse>> apiResponse = new ApiResponse<>(
            true,
            "Invoices retrieved successfully",
            response
        );
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * Generate PDF for an issued invoice
     */
    @PostMapping("/{invoiceId}/pdf")
    @Operation(summary = "Generate PDF", description = "Generates a PDF for an issued invoice")
    public ResponseEntity<ApiResponse<String>> generateInvoicePDF(
            @Parameter(description = "Invoice ID") @PathVariable Long invoiceId,
            @AuthenticationPrincipal User user) {
        logger.info("Generating PDF for invoice id: {} by user: {}", invoiceId, user.getUsername());
        String pdfUrl = pdfService.generateInvoicePDF(invoiceId, user.getId());
        ApiResponse<String> apiResponse = new ApiResponse<>(
            true,
            "PDF generated successfully",
            pdfUrl
        );
        return ResponseEntity.ok(apiResponse);
    }
}

