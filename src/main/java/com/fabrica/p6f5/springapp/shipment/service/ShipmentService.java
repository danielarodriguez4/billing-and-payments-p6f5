package com.fabrica.p6f5.springapp.shipment.service;

import com.fabrica.p6f5.springapp.shipment.repository.ShipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Shipment Service following Single Responsibility Principle.
 * Handles all shipment business logic.
 */
@Service
public class ShipmentService {
    
    @Autowired
    private ShipmentRepository shipmentRepository;
    
    // Business logic methods can be added here as needed
}

