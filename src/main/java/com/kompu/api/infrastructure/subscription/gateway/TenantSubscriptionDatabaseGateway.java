package com.kompu.api.infrastructure.subscription.gateway;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.kompu.api.entity.subscription.gateway.TenantSubscriptionGateway;
import com.kompu.api.entity.subscription.model.TenantSubscriptionModel;
import com.kompu.api.infrastructure.config.db.repository.TenantSubscriptionRepository;
import com.kompu.api.infrastructure.config.db.schema.TenantSubscriptionSchema;

import lombok.RequiredArgsConstructor;

/**
 * TenantSubscriptionDatabaseGateway - Database implementation of
 * TenantSubscriptionGateway
 */
@Component
@RequiredArgsConstructor
public class TenantSubscriptionDatabaseGateway implements TenantSubscriptionGateway {

    private final TenantSubscriptionRepository tenantSubscriptionRepository;

    @Override
    public Optional<TenantSubscriptionModel> findByTenantId(UUID tenantId) {
        return tenantSubscriptionRepository.findByTenantId(tenantId)
                .map(TenantSubscriptionSchema::toTenantSubscriptionModel);
    }

    @Override
    public Optional<TenantSubscriptionModel> findById(UUID id) {
        return tenantSubscriptionRepository.findById(id).map(TenantSubscriptionSchema::toTenantSubscriptionModel);
    }

    @Override
    public TenantSubscriptionModel save(TenantSubscriptionModel model) {
        return tenantSubscriptionRepository.save(new TenantSubscriptionSchema(model)).toTenantSubscriptionModel();
    }

    @Override
    public TenantSubscriptionModel update(TenantSubscriptionModel model) {
        TenantSubscriptionSchema schema = tenantSubscriptionRepository
                .findById(model.getId())
                .orElseThrow(() -> new IllegalArgumentException("Tenant subscription not found"));

        schema.setStatus(model.getStatus());
        schema.setSubscriptionEndDate(model.getSubscriptionEndDate());
        schema.setAutoRenew(model.getAutoRenew());
        schema.setTrialEndsAt(model.getTrialEndsAt());
        schema.setUpdatedBy(model.getUpdatedBy());

        TenantSubscriptionSchema updated = tenantSubscriptionRepository.save(schema);
        return updated.toTenantSubscriptionModel();
    }

    @Override
    public void delete(UUID id) {
        tenantSubscriptionRepository.deleteById(id);
    }

    @Override
    public TenantSubscriptionModel changePlan(UUID tenantId, UUID newPlanId) {
        TenantSubscriptionSchema schema = tenantSubscriptionRepository
                .findByTenantId(tenantId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Tenant subscription not found for tenant: " + tenantId));

        schema.setPlanId(newPlanId);
        TenantSubscriptionSchema updated = tenantSubscriptionRepository.save(schema);
        return updated.toTenantSubscriptionModel();
    }
}
