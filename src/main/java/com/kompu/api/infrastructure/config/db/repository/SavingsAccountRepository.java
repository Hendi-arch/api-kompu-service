package com.kompu.api.infrastructure.config.db.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.SavingsAccountSchema;

@Repository
public interface SavingsAccountRepository extends JpaRepository<SavingsAccountSchema, UUID> {

    List<SavingsAccountSchema> findByTenantId(UUID tenantId);

    Optional<SavingsAccountSchema> findByTenantIdAndAccountNumber(UUID tenantId, String accountNumber);

    List<SavingsAccountSchema> findByTenantIdAndMemberId(UUID tenantId, UUID memberId);
}
