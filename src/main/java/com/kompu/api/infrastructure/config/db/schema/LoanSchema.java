package com.kompu.api.infrastructure.config.db.schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import com.kompu.api.entity.loan.model.LoanModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "loans", schema = "app", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "tenant_id", "loan_number" })
})
@Slf4j
public class LoanSchema {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "member_id")
    private UUID memberId;

    @Column(name = "loan_number", nullable = false)
    private String loanNumber;

    @Column(precision = 18, scale = 2)
    private BigDecimal principal;

    @Column(precision = 18, scale = 2)
    private BigDecimal outstanding;

    @Builder.Default
    private String status = "draft";

    @Column(columnDefinition = "jsonb")
    private String terms;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public LoanSchema(LoanModel model) {
        this.id = model.getId();
        this.tenantId = model.getTenantId();
        this.memberId = model.getMemberId();
        this.loanNumber = model.getLoanNumber();
        this.principal = model.getPrincipal();
        this.outstanding = model.getOutstanding();
        this.status = model.getStatus();
        if (model.getTerms() != null) {
            try {
                this.terms = mapper.writeValueAsString(model.getTerms());
            } catch (JsonProcessingException e) {
                log.error("Error serializing terms for loan", e);
                this.terms = "{}";
            }
        }
        this.createdAt = model.getCreatedAt();
        this.updatedAt = model.getUpdatedAt();
        this.deletedAt = model.getDeletedAt();
    }

    public LoanModel toModel() {
        JsonNode termsNode = null;
        if (this.terms != null) {
            try {
                termsNode = mapper.readTree(this.terms);
            } catch (JsonProcessingException e) {
                log.error("Error deserializing terms for loan", e);
            }
        }

        return LoanModel.builder()
                .id(this.id)
                .tenantId(this.tenantId)
                .memberId(this.memberId)
                .loanNumber(this.loanNumber)
                .principal(this.principal)
                .outstanding(this.outstanding)
                .status(this.status)
                .terms(termsNode)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .deletedAt(this.deletedAt)
                .build();
    }
}
