package com.kompu.api.infrastructure.config.db.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.LoanSchema;

@Repository
public interface LoanRepository extends JpaRepository<LoanSchema, UUID> {

    List<LoanSchema> findByTenantId(UUID tenantId);

    Optional<LoanSchema> findByTenantIdAndLoanNumber(UUID tenantId, String loanNumber);

    List<LoanSchema> findByTenantIdAndMemberId(UUID tenantId, UUID memberId);
}
