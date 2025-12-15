package com.kompu.api.entity.subscription.gateway;

import java.util.Optional;
import java.util.UUID;

import com.kompu.api.entity.subscription.model.TenantRegistrationModel;

/**
 * TenantRegistrationGateway - Gateway interface for registration audit trail
 * Defines contract for managing tenant registration records
 */
public interface TenantRegistrationGateway {

    /**
     * Find registration record by tenant ID
     */
    Optional<TenantRegistrationModel> findByTenantId(UUID tenantId);

    /**
     * Find registration record by email address
     */
    Optional<TenantRegistrationModel> findByEmail(String email);

    /**
     * Find registration record by ID
     */
    Optional<TenantRegistrationModel> findById(UUID id);

    /**
     * Save new registration record
     */
    TenantRegistrationModel save(TenantRegistrationModel model);

    /**
     * Update registration record
     */
    TenantRegistrationModel update(TenantRegistrationModel model);

    /**
     * Mark email as verified
     */
    void markEmailVerified(UUID tenantId);

    /**
     * Mark terms and privacy as accepted
     */
    void markTermsAndPrivacyAccepted(UUID tenantId);
}
