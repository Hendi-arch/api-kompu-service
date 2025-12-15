package com.kompu.api.infrastructure.tenantdomain.gateway;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kompu.api.entity.tenantdomain.gateway.TenantDomainGateway;
import com.kompu.api.entity.tenantdomain.model.TenantDomainModel;
import com.kompu.api.infrastructure.config.db.repository.TenantDomainRepository;
import com.kompu.api.infrastructure.config.db.schema.TenantDomainSchema;

@Service
public class TenantDomainDatabaseGateway implements TenantDomainGateway {

    private final TenantDomainRepository tenantDomainRepository;

    public TenantDomainDatabaseGateway(TenantDomainRepository tenantDomainRepository) {
        this.tenantDomainRepository = tenantDomainRepository;
    }

    @Override
    public TenantDomainModel create(TenantDomainModel domain) {
        TenantDomainSchema schema = new TenantDomainSchema(domain);
        return tenantDomainRepository.save(schema).toModel();
    }

    @Override
    public TenantDomainModel update(TenantDomainModel domain) {
        TenantDomainSchema schema = new TenantDomainSchema(domain);
        return tenantDomainRepository.save(schema).toModel();
    }

    @Override
    public Optional<TenantDomainModel> findByHost(String host) {
        return tenantDomainRepository.findByHost(host).map(TenantDomainSchema::toModel);
    }

    @Override
    public void delete(UUID id) {
        tenantDomainRepository.deleteById(id);
    }

    @Override
    public Optional<TenantDomainModel> findById(UUID id) {
        return tenantDomainRepository.findById(id).map(TenantDomainSchema::toModel);
    }

    @Override
    public Optional<TenantDomainModel> findPrimaryByTenantId(UUID tenantId) {
        return tenantDomainRepository.findByTenantIdAndIsPrimaryTrue(tenantId).map(TenantDomainSchema::toModel);
    }

    @Override
    public java.util.List<TenantDomainModel> findByTenantId(UUID tenantId) {
        return tenantDomainRepository.findByTenantId(tenantId).stream()
                .map(TenantDomainSchema::toModel)
                .toList();
    }

    @Override
    public java.util.List<TenantDomainModel> findActiveByTenantId(UUID tenantId) {
        // Domains don't have status, so all are considered active unless deleted
        return findByTenantId(tenantId);
    }

    @Override
    public java.util.List<TenantDomainModel> findCustomByTenantId(UUID tenantId) {
        return tenantDomainRepository.findByTenantIdAndIsCustomTrue(tenantId).stream()
                .map(TenantDomainSchema::toModel)
                .toList();
    }

    @Override
    public boolean existsByHost(String host) {
        return tenantDomainRepository.existsByHost(host);
    }

    @Override
    public boolean existsByTenantIdAndHost(UUID tenantId, String host) {
        return tenantDomainRepository.existsByTenantIdAndHost(tenantId, host);
    }
}
