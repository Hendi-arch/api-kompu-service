package com.kompu.api.usecase.tenant;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kompu.api.entity.tenant.gateway.TenantGateway;
import com.kompu.api.entity.tenant.model.TenantModel;

/**
 * CreateTenantUseCase orchestrates the creation of a new tenant organization.
 * 
 * This use case handles:
 * 1. Tenant model creation with validation
 * 2. Persisting the tenant via the gateway
 * 3. Assigning founder user to the tenant
 * 
 * Responsibilities:
 * - Validate tenant data (name, code uniqueness)
 * - Generate initial tenant metadata
 * - Set default status to 'active'
 * - Persist via TenantGateway
 * 
 * This class is completely framework-agnostic and contains pure business logic.
 */
public class CreateTenantUseCase {

    private final TenantGateway tenantGateway;

    /**
     * Constructor with gateway dependency injection
     * 
     * @param tenantGateway the gateway for tenant persistence
     */
    public CreateTenantUseCase(TenantGateway tenantGateway) {
        this.tenantGateway = tenantGateway;
    }

    /**
     * Create a new tenant organization for a user (founder).
     * 
     * @param tenantName    the human-readable name of the tenant
     * @param tenantCode    the short code for the tenant (e.g., "kmj001")
     * @param founderUserId the user ID of the tenant founder
     * @return the created tenant with ID assigned
     * @throws IllegalArgumentException if tenant name or code is invalid
     */
    public TenantModel createTenant(String tenantName, String tenantCode, UUID founderUserId) {
        // Validate input
        if (tenantName == null || tenantName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant name cannot be empty");
        }
        if (tenantCode == null || tenantCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant code cannot be empty");
        }
        if (founderUserId == null) {
            throw new IllegalArgumentException("Founder user ID cannot be null");
        }

        // Build the tenant model
        TenantModel newTenant = TenantModel.builder()
                .id(UUID.randomUUID())
                .name(tenantName.trim())
                .code(tenantCode.trim().toLowerCase())
                .status("active")
                .founderUserId(founderUserId)
                .metadata("{}")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(founderUserId.toString())
                .updatedBy(founderUserId.toString())
                .build();

        // Persist via gateway
        return tenantGateway.create(newTenant);
    }

    /**
     * Create a new tenant with extended configuration.
     * 
     * @param tenantName    the human-readable name
     * @param tenantCode    the short code
     * @param founderUserId the founder's user ID
     * @param metadata      additional JSON metadata (settings, config, etc.)
     * @return the created tenant with ID assigned
     */
    public TenantModel createTenantWithMetadata(
            String tenantName,
            String tenantCode,
            UUID founderUserId,
            String metadata) {

        // Validate input
        if (tenantName == null || tenantName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant name cannot be empty");
        }
        if (tenantCode == null || tenantCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant code cannot be empty");
        }
        if (founderUserId == null) {
            throw new IllegalArgumentException("Founder user ID cannot be null");
        }

        // Use provided metadata or default to empty JSON object
        String finalMetadata = (metadata != null && !metadata.isEmpty()) ? metadata : "{}";

        // Build the tenant model
        TenantModel newTenant = TenantModel.builder()
                .id(UUID.randomUUID())
                .name(tenantName.trim())
                .code(tenantCode.trim().toLowerCase())
                .status("active")
                .founderUserId(founderUserId)
                .metadata(finalMetadata)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(founderUserId.toString())
                .updatedBy(founderUserId.toString())
                .build();

        // Persist via gateway
        return tenantGateway.create(newTenant);
    }
}
