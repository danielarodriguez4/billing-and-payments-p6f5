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
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Invoice Service following Single Responsibility Principle.
 * Handles all invoice business logic.
 */
@Service
public class InvoiceService {
    
    private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private InvoiceItemRepository invoiceItemRepository;
    
    @Autowired
    private InvoiceShipmentRepository invoiceShipmentRepository;
    
    @Autowired
    private ShipmentRepository shipmentRepository;
    
    @Autowired
    private AuditService auditService;
    
    /**
     * Create a draft invoice
     */
    @Transactional
    public InvoiceResponse createDraftInvoice(CreateInvoiceRequest request, Long createdBy) {
        logger.info("Creating draft invoice for client: {}", request.getClientName());
        
        // Generate unique invoice number
        String invoiceNumber = generateInvoiceNumber();
        
        // Create invoice
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setClientName(request.getClientName());
        invoice.setInvoiceDate(request.getInvoiceDate());
        invoice.setDueDate(request.getDueDate());
        invoice.setStatus(Invoice.InvoiceStatus.DRAFT);
        invoice.setCurrency(request.getCurrency());
        invoice.setCreatedBy(createdBy);
        
        // Calculate amounts
        BigDecimal subtotal = calculateSubtotal(request.getItems());
        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(request.getTaxAmount());
        invoice.setTotalAmount(subtotal.add(request.getTaxAmount()));
        
        // Save invoice
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // Add items
        List<InvoiceItem> items = new ArrayList<>();
        for (CreateInvoiceRequest.InvoiceItemRequest itemRequest : request.getItems()) {
            InvoiceItem item = new InvoiceItem();
            item.setInvoice(savedInvoice);
            item.setDescription(itemRequest.getDescription());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getUnitPrice());
            item.calculateTotal();
            items.add(item);
            
            // Link to shipment if provided
            if (itemRequest.getShipmentId() != null) {
                Shipment shipment = shipmentRepository.findById(itemRequest.getShipmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + itemRequest.getShipmentId()));
                item.setShipment(shipment);
            }
        }
        invoiceItemRepository.saveAll(items);
        
        // Link shipments
        if (request.getShipmentIds() != null && !request.getShipmentIds().isEmpty()) {
            List<InvoiceShipment> invoiceShipments = new ArrayList<>();
            for (Long shipmentId : request.getShipmentIds()) {
                Shipment shipment = shipmentRepository.findById(shipmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + shipmentId));
                
                // Check if shipment is already linked to an invoice
                if (invoiceShipmentRepository.existsByShipmentId(shipmentId)) {
                    throw new BusinessException("Shipment " + shipmentId + " is already linked to an invoice");
                }
                
                InvoiceShipment invoiceShipment = new InvoiceShipment();
                invoiceShipment.setInvoice(savedInvoice);
                invoiceShipment.setShipment(shipment);
                invoiceShipments.add(invoiceShipment);
            }
            invoiceShipmentRepository.saveAll(invoiceShipments);
        }
        
        // Log audit event
        auditService.logEvent("Invoice", savedInvoice.getId(), AuditLog.AuditAction.CREATE,
            createdBy, null, savedInvoice, "Created draft invoice");
        
        logger.info("Draft invoice created with id: {}", savedInvoice.getId());
        
        return InvoiceResponse.fromEntity(invoiceRepository.findById(savedInvoice.getId()).get());
    }
    
    /**
     * Update a draft invoice
     */
    @Transactional
    public InvoiceResponse updateDraftInvoice(Long invoiceId, UpdateInvoiceRequest request, Long updatedBy) {
        logger.info("Updating draft invoice id: {}", invoiceId);
        
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));
        
        // Check if invoice can be edited
        if (!invoice.canBeEdited()) {
            throw new BusinessException("Invoice cannot be edited. Status: " + invoice.getStatus());
        }
        
        // Optimistic concurrency control
        if (request.getVersion() != null && !request.getVersion().equals(invoice.getVersion())) {
            throw new BusinessException("Invoice has been modified by another user. Please refresh and try again.");
        }
        
        // Save history before updating
        auditService.saveInvoiceHistory(invoice.getId(), invoice.getVersion(),
            invoice.getFiscalFolio(), invoice.getInvoiceNumber(), invoice, invoice.getCreatedBy());
        
        // Save old data for audit
        Invoice oldInvoice = copyInvoice(invoice);
        
        // Update invoice fields
        invoice.setClientName(request.getClientName());
        invoice.setInvoiceDate(request.getInvoiceDate());
        invoice.setDueDate(request.getDueDate());
        invoice.setCurrency(request.getCurrency());
        invoice.setTaxAmount(request.getTaxAmount());
        
        // Calculate amounts
        BigDecimal subtotal = calculateSubtotal(request.getItems());
        invoice.setSubtotal(subtotal);
        invoice.setTotalAmount(subtotal.add(request.getTaxAmount()));
        
        // Delete old items
        invoiceItemRepository.deleteByInvoiceId(invoiceId);
        invoiceShipmentRepository.deleteByInvoiceId(invoiceId);
        
        // Add new items
        List<InvoiceItem> items = new ArrayList<>();
        for (UpdateInvoiceRequest.InvoiceItemRequest itemRequest : request.getItems()) {
            InvoiceItem item = new InvoiceItem();
            item.setInvoice(invoice);
            item.setDescription(itemRequest.getDescription());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getUnitPrice());
            item.calculateTotal();
            items.add(item);
            
            // Link to shipment if provided
            if (itemRequest.getShipmentId() != null) {
                Shipment shipment = shipmentRepository.findById(itemRequest.getShipmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + itemRequest.getShipmentId()));
                item.setShipment(shipment);
            }
        }
        invoiceItemRepository.saveAll(items);
        
        // Link shipments
        if (request.getShipmentIds() != null && !request.getShipmentIds().isEmpty()) {
            List<InvoiceShipment> invoiceShipments = new ArrayList<>();
            for (Long shipmentId : request.getShipmentIds()) {
                Shipment shipment = shipmentRepository.findById(shipmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + shipmentId));
                
                // Check if shipment is already linked to another invoice
                if (invoiceShipmentRepository.existsByShipmentId(shipmentId)) {
                    Optional<InvoiceShipment> existing = invoiceShipmentRepository.findByInvoiceIdAndShipmentId(invoiceId, shipmentId);
                    if (!existing.isPresent()) {
                        throw new BusinessException("Shipment " + shipmentId + " is already linked to another invoice");
                    }
                }
                
                InvoiceShipment invoiceShipment = new InvoiceShipment();
                invoiceShipment.setInvoice(invoice);
                invoiceShipment.setShipment(shipment);
                invoiceShipments.add(invoiceShipment);
            }
            invoiceShipmentRepository.saveAll(invoiceShipments);
        }
        
        // Save updated invoice
        Invoice updatedInvoice = invoiceRepository.save(invoice);
        
        // Log audit event
        auditService.logEvent("Invoice", updatedInvoice.getId(), AuditLog.AuditAction.UPDATE,
            updatedBy, oldInvoice, updatedInvoice, "Updated draft invoice");
        
        logger.info("Draft invoice updated with id: {}", updatedInvoice.getId());
        
        return InvoiceResponse.fromEntity(invoiceRepository.findById(updatedInvoice.getId()).get());
    }
    
    /**
     * Issue an invoice
     */
    @Transactional
    public InvoiceResponse issueInvoice(Long invoiceId, Long issuedBy) {
        logger.info("Issuing invoice id: {}", invoiceId);
        
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));
        
        // Check if invoice can be issued
        if (!invoice.canBeIssued()) {
            throw new BusinessException("Invoice cannot be issued. Missing required data or invalid status.");
        }
        
        // Generate fiscal folio if not exists
        if (invoice.getFiscalFolio() == null) {
            String fiscalFolio = generateFiscalFolio();
            invoice.setFiscalFolio(fiscalFolio);
        }
        
        // Change status to ISSUED
        invoice.setStatus(Invoice.InvoiceStatus.ISSUED);
        
        // Save invoice
        Invoice issuedInvoice = invoiceRepository.save(invoice);
        
        // Log audit event
        auditService.logEvent("Invoice", issuedInvoice.getId(), AuditLog.AuditAction.ISSUE,
            issuedBy, invoice, issuedInvoice, "Issued invoice");
        
        // Save history
        auditService.saveInvoiceHistory(issuedInvoice.getId(), issuedInvoice.getVersion(),
            issuedInvoice.getFiscalFolio(), issuedInvoice.getInvoiceNumber(), issuedInvoice, issuedBy);
        
        logger.info("Invoice issued with fiscal folio: {}", issuedInvoice.getFiscalFolio());
        
        return InvoiceResponse.fromEntity(invoiceRepository.findById(issuedInvoice.getId()).get());
    }
    
    /**
     * Get invoice by ID
     */
    public InvoiceResponse getInvoiceById(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));
        return InvoiceResponse.fromEntity(invoice);
    }
    
    /**
     * Get all invoices by status
     */
    public List<InvoiceResponse> getInvoicesByStatus(Invoice.InvoiceStatus status) {
        return invoiceRepository.findByStatusOrderByCreatedAtDesc(status).stream()
            .map(InvoiceResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all invoices
     */
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll().stream()
            .map(InvoiceResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate subtotal from items
     */
    private BigDecimal calculateSubtotal(List<?> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
            .map(item -> {
                BigDecimal unitPrice;
                Integer quantity;
                if (item instanceof CreateInvoiceRequest.InvoiceItemRequest req) {
                    unitPrice = req.getUnitPrice();
                    quantity = req.getQuantity();
                } else if (item instanceof UpdateInvoiceRequest.InvoiceItemRequest req) {
                    unitPrice = req.getUnitPrice();
                    quantity = req.getQuantity();
                } else {
                    return BigDecimal.ZERO;
                }
                return unitPrice.multiply(BigDecimal.valueOf(quantity));
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Generate unique invoice number
     */
    private String generateInvoiceNumber() {
        return "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-" + System.currentTimeMillis();
    }
    
    /**
     * Generate unique fiscal folio
     */
    private String generateFiscalFolio() {
        return "FISCAL-" + UUID.randomUUID().toString().substring(0, 16).toUpperCase() + "-" + System.currentTimeMillis();
    }
    
    /**
     * Copy invoice for history
     */
    private Invoice copyInvoice(Invoice invoice) {
        Invoice copy = new Invoice();
        copy.setId(invoice.getId());
        copy.setFiscalFolio(invoice.getFiscalFolio());
        copy.setInvoiceNumber(invoice.getInvoiceNumber());
        copy.setClientName(invoice.getClientName());
        copy.setInvoiceDate(invoice.getInvoiceDate());
        copy.setDueDate(invoice.getDueDate());
        copy.setSubtotal(invoice.getSubtotal());
        copy.setTaxAmount(invoice.getTaxAmount());
        copy.setTotalAmount(invoice.getTotalAmount());
        copy.setCurrency(invoice.getCurrency());
        copy.setStatus(invoice.getStatus());
        copy.setCreatedBy(invoice.getCreatedBy());
        copy.setCreatedAt(invoice.getCreatedAt());
        copy.setUpdatedAt(invoice.getUpdatedAt());
        copy.setVersion(invoice.getVersion());
        return copy;
    }
}

