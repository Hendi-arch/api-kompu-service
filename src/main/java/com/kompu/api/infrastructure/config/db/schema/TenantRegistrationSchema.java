package com.kompu.api.infrastructure.config.db.schema;

import java.util.UUID;

import com.kompu.api.entity.subscription.model.TenantRegistrationModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TenantRegistrationSchema - JPA entity mapping to tenant_registrations table
 * Tracks registration audit trail for compliance and security
 */
@Entity
@Table(name = "tenant_registrations", schema = "app", indexes = {
        @Index(name = "idx_tenant_registrations_tenant", columnList = "tenant_id"),
        @Index(name = "idx_tenant_registrations_email", columnList = "email_used")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantRegistrationSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, columnDefinition = "uuid", unique = true)
    private UUID tenantId;

    @Column(nullable = false, length = 50)
    private String registrationType; // 'koperasi', 'non-koperasi'

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID adminUserId;

    @Column(nullable = false, length = 255)
    private String emailUsed;

    @Column(length = 50)
    private String ipAddress;

    @Column(columnDefinition = "text")
    private String userAgent;

    @Column
    private Long termsAcceptedAt; // Milliseconds since epoch

    @Column
    private Long privacyAcceptedAt; // Milliseconds since epoch

    @Column
    private Long emailVerificationSentAt; // Milliseconds since epoch

    @Column
    private Long emailVerifiedAt; // Milliseconds since epoch

    @Column(length = 50)
    @Builder.Default
    private String registrationSource = "web"; // 'web', 'api', 'mobile', 'manual'

    @Column(columnDefinition = "text")
    private String notes;

    /**
     * Constructor from domain model - converts TenantRegistrationModel to schema
     */
    public TenantRegistrationSchema(TenantRegistrationModel model) {
        this.id = model.getId();
        this.tenantId = model.getTenantId();
        this.registrationType = model.getRegistrationType();
        this.adminUserId = model.getAdminUserId();
        this.emailUsed = model.getEmailUsed();
        this.ipAddress = model.getIpAddress();
        this.userAgent = model.getUserAgent();
        this.termsAcceptedAt = model.getTermsAcceptedAt();
        this.privacyAcceptedAt = model.getPrivacyAcceptedAt();
        this.emailVerificationSentAt = model.getEmailVerificationSentAt();
        this.emailVerifiedAt = model.getEmailVerifiedAt();
        this.registrationSource = model.getRegistrationSource();
        this.notes = model.getNotes();
    }

    /**
     * Converts schema back to domain model
     */
    public TenantRegistrationModel toModel() {
        return TenantRegistrationModel.builder()
                .id(this.id)
                .tenantId(this.tenantId)
                .registrationType(this.registrationType)
                .adminUserId(this.adminUserId)
                .emailUsed(this.emailUsed)
                .ipAddress(this.ipAddress)
                .userAgent(this.userAgent)
                .termsAcceptedAt(this.termsAcceptedAt)
                .privacyAcceptedAt(this.privacyAcceptedAt)
                .emailVerificationSentAt(this.emailVerificationSentAt)
                .emailVerifiedAt(this.emailVerifiedAt)
                .registrationSource(this.registrationSource)
                .notes(this.notes)
                .build();
    }
}
