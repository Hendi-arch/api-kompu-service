package com.kompu.api.infrastructure.subscription.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.kompu.api.entity.subscription.gateway.SubscriptionPlanGateway;
import com.kompu.api.entity.subscription.model.SubscriptionPlanModel;
import com.kompu.api.infrastructure.config.db.repository.SubscriptionPlanRepository;
import com.kompu.api.infrastructure.config.db.schema.SubscriptionPlanSchema;

import lombok.RequiredArgsConstructor;

/**
 * SubscriptionPlanDatabaseGateway - Database implementation of
 * SubscriptionPlanGateway
 * Converts between domain models and database schemas
 */
@Component
@RequiredArgsConstructor
public class SubscriptionPlanDatabaseGateway implements SubscriptionPlanGateway {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Override
    public Optional<SubscriptionPlanModel> findById(UUID id) {
        return subscriptionPlanRepository.findById(id).map(this::toModel);
    }

    @Override
    public Optional<SubscriptionPlanModel> findByName(String name) {
        return subscriptionPlanRepository.findByNameIgnoreCase(name).map(this::toModel);
    }

    @Override
    public List<SubscriptionPlanModel> findAllActive() {
        return subscriptionPlanRepository.findAllActive().stream().map(this::toModel).toList();
    }

    @Override
    public List<SubscriptionPlanModel> findAll() {
        return subscriptionPlanRepository.findAll().stream().map(this::toModel).toList();
    }

    @Override
    public SubscriptionPlanModel save(SubscriptionPlanModel model) {
        SubscriptionPlanSchema schema = new SubscriptionPlanSchema(model);
        SubscriptionPlanSchema saved = subscriptionPlanRepository.save(schema);
        return toModel(saved);
    }

    @Override
    public SubscriptionPlanModel update(SubscriptionPlanModel model) {
        SubscriptionPlanSchema schema = subscriptionPlanRepository
                .findById(model.getId())
                .orElseThrow(() -> new IllegalArgumentException("Subscription plan not found"));

        // Update fields from model
        schema.setName(model.getName());
        schema.setPrice(model.getPrice());
        schema.setBillingPeriod(model.getBillingPeriod());
        schema.setMaxUsers(model.getMaxUsers());
        schema.setMaxUnitUsaha(model.getMaxUnitUsaha());
        schema.setDescription(model.getDescription());
        schema.setIsActive(model.getIsActive());

        SubscriptionPlanSchema updated = subscriptionPlanRepository.save(schema);
        return toModel(updated);
    }

    @Override
    public void delete(UUID id) {
        subscriptionPlanRepository.deleteById(id);
    }

    private SubscriptionPlanModel toModel(SubscriptionPlanSchema schema) {
        return schema.toSubscriptionPlanModel();
    }
}
