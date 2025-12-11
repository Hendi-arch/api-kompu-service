package com.kompu.api.entity.member.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.kompu.api.entity.member.exception.MemberNotFoundException;
import com.kompu.api.entity.member.model.MemberModel;

/**
 * MemberGateway defines the contract for all member data access operations.
 * 
 * This interface abstracts the underlying persistence mechanism from business
 * logic.
 * All member operations are scoped to a specific tenant.
 * 
 * Gateway Pattern: Allows the entity layer to remain independent of
 * infrastructure details.
 */
public interface MemberGateway {

    /**
     * Create a new member record
     * 
     * @param memberModel the member to create
     * @return the created member with ID assigned
     */
    MemberModel create(MemberModel memberModel);

    /**
     * Update an existing member
     * 
     * @param memberModel the member with updated values
     * @return the updated member
     */
    MemberModel update(MemberModel memberModel);

    /**
     * Delete a member by ID
     * 
     * @param id the member ID
     * @throws MemberNotFoundException if member not found
     */
    void delete(UUID id) throws MemberNotFoundException;

    /**
     * Find a member by ID
     * 
     * @param id the member ID
     * @return Optional containing the member, or empty if not found
     */
    Optional<MemberModel> findById(UUID id);

    /**
     * Find a member by user ID and tenant ID
     * 
     * @param userId   the user ID
     * @param tenantId the tenant ID
     * @return Optional containing the member, or empty if not found
     */
    Optional<MemberModel> findByUserIdAndTenantId(UUID userId, UUID tenantId);

    /**
     * Find a member by member code within a tenant
     * 
     * @param tenantId   the tenant ID
     * @param memberCode the member code
     * @return Optional containing the member, or empty if not found
     */
    Optional<MemberModel> findByTenantIdAndMemberCode(UUID tenantId, String memberCode);

    /**
     * Find all members for a given tenant
     * 
     * @param tenantId the tenant ID
     * @return list of all members in the tenant
     */
    List<MemberModel> findByTenantId(UUID tenantId);

    /**
     * Find all active members for a given tenant
     * 
     * @param tenantId the tenant ID
     * @return list of active members in the tenant
     */
    List<MemberModel> findActiveByTenantId(UUID tenantId);

    /**
     * Check if a member exists by ID
     * 
     * @param id the member ID
     * @return true if member exists, false otherwise
     */
    boolean existsById(UUID id);

    /**
     * Check if a member code exists within a tenant
     * 
     * @param tenantId   the tenant ID
     * @param memberCode the member code
     * @return true if member code exists, false otherwise
     */
    boolean existsByTenantIdAndMemberCode(UUID tenantId, String memberCode);
}
