package com.kompu.api.entity.member.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kompu.api.entity.AbstractEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * MemberModel represents a member/employee within a tenant organization.
 * 
 * Members are tenant-scoped entities that represent individuals who interact
 * with the system. A user account can be linked to one or more members
 * within different tenants.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MemberModel extends AbstractEntity<UUID> {

    private UUID id;

    /**
     * The tenant to which this member belongs
     */
    private UUID tenantId;

    /**
     * Unique member code within the tenant (e.g., "MEM001", "EMP2025001")
     */
    private String memberCode;

    /**
     * The user account associated with this member (if any)
     */
    private UUID userId;

    /**
     * Full name of the member
     */
    private String fullName;

    /**
     * Email address of the member
     */
    private String email;

    /**
     * Phone number of the member
     */
    private String phone;

    /**
     * Physical address of the member
     */
    private String address;

    /**
     * Date when the member joined the organization
     */
    private LocalDate joinedAt;

    /**
     * Current status of the member: active | inactive | suspended
     */
    private String status;

    /**
     * Additional metadata stored as JSON (custom fields, preferences, etc.)
     */
    @Builder.Default
    private String metadata = "{}";

    /**
     * Timestamp when the member was created
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the member was last updated
     */
    private LocalDateTime updatedAt;

    /**
     * User ID who created the member
     */
    private String createdBy;

    /**
     * User ID who last updated the member
     */
    private String updatedBy;

    /**
     * Soft delete timestamp - null means active
     */
    @JsonIgnore
    private LocalDateTime deletedAt;

    /**
     * Check if the member is active
     * 
     * @return true if status is 'active' and not soft-deleted
     */
    public boolean isActive() {
        return "active".equals(this.status) && this.deletedAt == null;
    }

    /**
     * Check if the member is suspended
     * 
     * @return true if status is 'suspended'
     */
    public boolean isSuspended() {
        return "suspended".equals(this.status);
    }

    /**
     * Activate the member
     */
    public void activate() {
        this.status = "active";
        this.deletedAt = null;
    }

    /**
     * Suspend the member
     */
    public void suspend() {
        this.status = "suspended";
    }

    /**
     * Deactivate the member (soft delete)
     */
    public void deactivate() {
        this.status = "inactive";
        this.deletedAt = LocalDateTime.now();
    }
}
