package com.kompu.api.infrastructure.subscription.gateway;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kompu.api.entity.subscription.gateway.TenantRegistrationGateway;
import com.kompu.api.entity.subscription.model.TenantRegistrationModel;
import com.kompu.api.infrastructure.config.db.repository.TenantRegistrationRepository;
import com.kompu.api.infrastructure.config.db.schema.TenantRegistrationSchema;

@Service
public class TenantRegistrationDatabaseGateway implements TenantRegistrationGateway {

    private final TenantRegistrationRepository tenantRegistrationRepository;

    public TenantRegistrationDatabaseGateway(TenantRegistrationRepository tenantRegistrationRepository) {
        this.tenantRegistrationRepository = tenantRegistrationRepository;
    }

    @Override
    public TenantRegistrationModel save(TenantRegistrationModel model) {
        TenantRegistrationSchema schema = new TenantRegistrationSchema(model);
        return tenantRegistrationRepository.save(schema).toModel();
    }

    @Override
    public TenantRegistrationModel update(TenantRegistrationModel model) {
        TenantRegistrationSchema schema = new TenantRegistrationSchema(model);
        return tenantRegistrationRepository.save(schema).toModel();
    }

    @Override
    public Optional<TenantRegistrationModel> findById(UUID id) {
        return tenantRegistrationRepository.findById(id)
                .map(TenantRegistrationSchema::toModel);
    }

    @Override
    public Optional<TenantRegistrationModel> findByTenantId(UUID tenantId) {
        return tenantRegistrationRepository.findByTenantId(tenantId)
                .map(TenantRegistrationSchema::toModel);
    }

    @Override
    public Optional<TenantRegistrationModel> findByEmail(String email) {
        return tenantRegistrationRepository.findByEmailUsed(email)
                .map(TenantRegistrationSchema::toModel);
    }

    @Override
    public void markEmailVerified(UUID tenantId) {
        tenantRegistrationRepository.findByTenantId(tenantId).ifPresent(schema -> {
            schema.setEmailVerifiedAt(System.currentTimeMillis());
            tenantRegistrationRepository.save(schema);
        });
    }

    @Override
    public void markTermsAndPrivacyAccepted(UUID tenantId) {
        tenantRegistrationRepository.findByTenantId(tenantId).ifPresent(schema -> {
            long now = System.currentTimeMillis();
            schema.setTermsAcceptedAt(now);
            schema.setPrivacyAcceptedAt(now);
            tenantRegistrationRepository.save(schema);
        });
    }

}
