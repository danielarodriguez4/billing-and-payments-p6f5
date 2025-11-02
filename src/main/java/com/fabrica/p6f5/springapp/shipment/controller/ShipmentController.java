package com.fabrica.p6f5.springapp.shipment.controller;

import com.fabrica.p6f5.springapp.dto.ApiResponse;
import com.fabrica.p6f5.springapp.shipment.model.Shipment;
import com.fabrica.p6f5.springapp.shipment.repository.ShipmentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Shipment Controller following Single Responsibility Principle.
 * Handles all shipment-related HTTP requests.
 */
@RestController
@RequestMapping("/api/v1/shipments")
@Tag(name = "Shipment API", description = "API for managing shipments")
public class ShipmentController {
    
    @Autowired
    private ShipmentRepository shipmentRepository;
    
    /**
     * Get all shipments
     */
    @GetMapping
    @Operation(summary = "Get all shipments", description = "Retrieves all shipments")
    public ResponseEntity<ApiResponse<List<Shipment>>> getAllShipments() {
        List<Shipment> shipments = shipmentRepository.findAll();
        ApiResponse<List<Shipment>> apiResponse = new ApiResponse<>(
            true,
            "Shipments retrieved successfully",
            shipments
        );
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * Get shipments by status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get shipments by status", description = "Retrieves shipments filtered by status")
    public ResponseEntity<ApiResponse<List<Shipment>>> getShipmentsByStatus(
            @Parameter(description = "Shipment status") @PathVariable String status) {
        Shipment.ShipmentStatus shipmentStatus = Shipment.ShipmentStatus.valueOf(status.toUpperCase());
        List<Shipment> shipments = shipmentRepository.findByStatusOrderByCreatedAtDesc(shipmentStatus);
        ApiResponse<List<Shipment>> apiResponse = new ApiResponse<>(
            true,
            "Shipments retrieved successfully",
            shipments
        );
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * Get unlinked shipments
     */
    @GetMapping("/unlinked")
    @Operation(summary = "Get unlinked shipments", description = "Retrieves shipments not yet linked to any invoice")
    public ResponseEntity<ApiResponse<List<Shipment>>> getUnlinkedShipments() {
        List<Shipment> shipments = shipmentRepository.findUnlinkedShipments();
        ApiResponse<List<Shipment>> apiResponse = new ApiResponse<>(
            true,
            "Unlinked shipments retrieved successfully",
            shipments
        );
        return ResponseEntity.ok(apiResponse);
    }
}

