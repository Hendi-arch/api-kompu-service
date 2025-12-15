package com.kompu.api.infrastructure.config.db.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.MemberSchema;

@Repository
public interface MemberRepository extends JpaRepository<MemberSchema, UUID> {

    List<MemberSchema> findByTenantId(UUID tenantId);

    Optional<MemberSchema> findByTenantIdAndMemberCode(UUID tenantId, String memberCode);

    List<MemberSchema> findByTenantIdAndStatus(UUID tenantId, String status);

    java.util.Optional<MemberSchema> findByTenantIdAndUserId(UUID tenantId, UUID userId);

    boolean existsByTenantIdAndMemberCode(UUID tenantId, String memberCode);
}
