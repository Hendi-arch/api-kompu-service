package com.kompu.api.infrastructure.tenant.gateway;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kompu.api.entity.tenant.gateway.TenantGateway;
import com.kompu.api.entity.tenant.model.TenantModel;
import com.kompu.api.infrastructure.config.db.repository.TenantRepository;
import com.kompu.api.infrastructure.config.db.schema.TenantSchema;

@Service
public class TenantDatabaseGateway implements TenantGateway {

    private final TenantRepository tenantRepository;

    public TenantDatabaseGateway(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public TenantModel create(TenantModel tenant) {
        TenantSchema schema = new TenantSchema(tenant);
        return tenantRepository.save(schema).toModel();
    }

    @Override
    public TenantModel update(TenantModel tenant) {
        TenantSchema schema = new TenantSchema(tenant);
        return tenantRepository.save(schema).toModel();
    }

    @Override
    public Optional<TenantModel> findById(UUID id) {
        return tenantRepository.findById(id).map(TenantSchema::toModel);
    }

    @Override
    public Optional<TenantModel> findByCode(String code) {
        return tenantRepository.findByCode(code).map(TenantSchema::toModel);
    }

    @Override
    public void delete(UUID id) throws com.kompu.api.entity.tenant.exception.TenantNotFoundException {
        if (!tenantRepository.existsById(id)) {
            throw new com.kompu.api.entity.tenant.exception.TenantNotFoundException();
        }
        tenantRepository.deleteById(id);
    }

    @Override
    public Optional<TenantModel> findByName(String name) {
        return tenantRepository.findByName(name).map(TenantSchema::toModel);
    }

    @Override
    public java.util.List<TenantModel> findAllActive() {
        return tenantRepository.findByStatus("active").stream()
                .map(TenantSchema::toModel)
                .toList();
    }

    @Override
    public java.util.List<TenantModel> findAll() {
        return tenantRepository.findAll().stream()
                .map(TenantSchema::toModel)
                .toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return tenantRepository.existsById(id);
    }
}
