package com.kompu.api.usecase.member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.kompu.api.entity.member.gateway.MemberGateway;
import com.kompu.api.entity.member.model.MemberModel;

/**
 * CreateMemberUseCase orchestrates the creation of member records within a
 * tenant.
 * 
 * This use case handles:
 * 1. Member model creation with validation
 * 2. Generation of unique member code
 * 3. Linking to a user account (optional)
 * 4. Persisting the member via the gateway
 * 
 * Responsibilities:
 * - Validate member data (name, email, phone)
 * - Generate unique member codes
 * - Set default status to 'active'
 * - Prevent duplicate member codes within tenant
 * - Link to user account if provided
 * 
 * This class is completely framework-agnostic and contains pure business logic.
 */
public class CreateMemberUseCase {

    private final MemberGateway memberGateway;

    /**
     * Constructor with gateway dependency injection
     * 
     * @param memberGateway the gateway for member persistence
     */
    public CreateMemberUseCase(MemberGateway memberGateway) {
        this.memberGateway = memberGateway;
    }

    /**
     * Create a new member record within a tenant.
     * 
     * @param tenantId the tenant the member belongs to
     * @param fullName the member's full name
     * @param email    the member's email address
     * @param phone    the member's phone number
     * @return the created member with ID and generated code assigned
     * @throws IllegalArgumentException if required fields are invalid
     */
    public MemberModel createMember(
            UUID tenantId,
            String fullName,
            String email,
            String phone) {
        return createMemberWithUserId(tenantId, fullName, email, phone, null);
    }

    /**
     * Create a new member record linked to a user account.
     * 
     * @param tenantId the tenant the member belongs to
     * @param fullName the member's full name
     * @param email    the member's email address
     * @param phone    the member's phone number
     * @param userId   the user ID to link (optional, can be null)
     * @return the created member with ID and generated code assigned
     * @throws IllegalArgumentException if required fields are invalid
     */
    public MemberModel createMemberWithUserId(
            UUID tenantId,
            String fullName,
            String email,
            String phone,
            UUID userId) {

        // Validate input
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }

        // Generate unique member code
        String memberCode = generateUniqueMemberCode(tenantId);

        // Build the member model
        MemberModel newMember = MemberModel.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .memberCode(memberCode)
                .userId(userId)
                .fullName(fullName.trim())
                .email(email != null ? email.trim() : null)
                .phone(phone != null ? phone.trim() : null)
                .status("active")
                .metadata("{}")
                .joinedAt(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(userId != null ? userId.toString() : "system")
                .updatedBy(userId != null ? userId.toString() : "system")
                .build();

        // Persist via gateway
        return memberGateway.create(newMember);
    }

    /**
     * Create a new member with full details and metadata.
     * 
     * @param tenantId the tenant the member belongs to
     * @param fullName the member's full name
     * @param email    the member's email address
     * @param phone    the member's phone number
     * @param address  the member's address
     * @param userId   the user ID to link (optional)
     * @param metadata additional JSON metadata
     * @return the created member with all details set
     * @throws IllegalArgumentException if required fields are invalid
     */
    public MemberModel createMemberFull(
            UUID tenantId,
            String fullName,
            String email,
            String phone,
            String address,
            UUID userId,
            String metadata) {

        // Validate input
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }

        // Generate unique member code
        String memberCode = generateUniqueMemberCode(tenantId);

        // Use provided metadata or default to empty JSON
        String finalMetadata = (metadata != null && !metadata.isEmpty()) ? metadata : "{}";

        // Build the member model with all details
        MemberModel newMember = MemberModel.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .memberCode(memberCode)
                .userId(userId)
                .fullName(fullName.trim())
                .email(email != null ? email.trim() : null)
                .phone(phone != null ? phone.trim() : null)
                .address(address != null ? address.trim() : null)
                .status("active")
                .metadata(finalMetadata)
                .joinedAt(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(userId != null ? userId.toString() : "system")
                .updatedBy(userId != null ? userId.toString() : "system")
                .build();

        // Persist via gateway
        return memberGateway.create(newMember);
    }

    /**
     * Generate a unique member code for a tenant.
     * 
     * Format: MEM<year><sequential-number>
     * Example: MEM202500001, MEM202500002, etc.
     * 
     * @param tenantId the tenant context
     * @return a unique member code
     */
    private String generateUniqueMemberCode(UUID tenantId) {
        int year = LocalDate.now().getYear();
        int sequenceNumber = 1;
        String candidateCode;

        // Try to find a unique code by incrementing sequence
        do {
            candidateCode = String.format("MEM%d%05d", year, sequenceNumber);
            sequenceNumber++;
        } while (memberGateway.existsByTenantIdAndMemberCode(tenantId, candidateCode)
                && sequenceNumber < 100000); // Prevent infinite loop

        if (sequenceNumber >= 100000) {
            // Fallback to UUID-based code if sequence limit reached
            candidateCode = "MEM" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        return candidateCode;
    }
}
