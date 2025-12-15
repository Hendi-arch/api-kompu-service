package com.kompu.api.infrastructure.member.gateway;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kompu.api.entity.member.gateway.MemberGateway;
import com.kompu.api.entity.member.model.MemberModel;
import com.kompu.api.infrastructure.config.db.repository.MemberRepository;
import com.kompu.api.infrastructure.config.db.schema.MemberSchema;

@Service
public class MemberDatabaseGateway implements MemberGateway {

    private final MemberRepository memberRepository;

    public MemberDatabaseGateway(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public MemberModel create(MemberModel member) {
        MemberSchema schema = new MemberSchema(member);
        return memberRepository.save(schema).toModel();
    }

    @Override
    public MemberModel update(MemberModel member) {
        MemberSchema schema = new MemberSchema(member);
        return memberRepository.save(schema).toModel();
    }

    @Override
    public Optional<MemberModel> findById(UUID id) {
        return memberRepository.findById(id).map(MemberSchema::toModel);
    }

    @Override
    public Optional<MemberModel> findByUserIdAndTenantId(UUID userId, UUID tenantId) {
        return memberRepository.findByTenantIdAndUserId(tenantId, userId).map(MemberSchema::toModel);
    }

    @Override
    public Optional<MemberModel> findByTenantIdAndMemberCode(UUID tenantId, String memberCode) {
        return memberRepository.findByTenantIdAndMemberCode(tenantId, memberCode).map(MemberSchema::toModel);
    }

    @Override
    public boolean existsByTenantIdAndMemberCode(UUID tenantId, String memberCode) {
        return memberRepository.existsByTenantIdAndMemberCode(tenantId, memberCode);
    }

    @Override
    public void delete(UUID id) throws com.kompu.api.entity.member.exception.MemberNotFoundException {
        if (!memberRepository.existsById(id)) {
            throw new com.kompu.api.entity.member.exception.MemberNotFoundException();
        }
        memberRepository.deleteById(id);
    }

    @Override
    public java.util.List<MemberModel> findByTenantId(UUID tenantId) {
        return memberRepository.findByTenantId(tenantId).stream()
                .map(MemberSchema::toModel)
                .toList();
    }

    @Override
    public java.util.List<MemberModel> findActiveByTenantId(UUID tenantId) {
        return memberRepository.findByTenantIdAndStatus(tenantId, "active").stream()
                .map(MemberSchema::toModel)
                .toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return memberRepository.existsById(id);
    }
}
