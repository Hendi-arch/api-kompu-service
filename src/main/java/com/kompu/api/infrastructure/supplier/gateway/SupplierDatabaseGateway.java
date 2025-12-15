package com.kompu.api.infrastructure.supplier.gateway;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.kompu.api.entity.supplier.gateway.SupplierGateway;
import com.kompu.api.entity.supplier.model.SupplierModel;
import com.kompu.api.infrastructure.config.db.repository.SupplierRepository;
import com.kompu.api.infrastructure.config.db.schema.SupplierSchema;

import lombok.RequiredArgsConstructor;

/**
 * SupplierDatabaseGateway - Database implementation of SupplierGateway
 * Converts between domain models and database schemas
 */
@Component
@RequiredArgsConstructor
public class SupplierDatabaseGateway implements SupplierGateway {

    private final SupplierRepository supplierRepository;

    @Override
    public Optional<SupplierModel> findById(UUID id) {
        return supplierRepository.findById(id).map(SupplierSchema::toSupplierModel);
    }

    @Override
    public Optional<SupplierModel> findByTenantAndCode(UUID tenantId, String supplierCode) {
        return supplierRepository.findByTenantIdAndSupplierCode(tenantId, supplierCode)
                .map(SupplierSchema::toSupplierModel);
    }

    @Override
    public Optional<SupplierModel> findByEmail(String email) {
        return supplierRepository.findByEmail(email).map(SupplierSchema::toSupplierModel);
    }

    @Override
    public List<SupplierModel> findAllActiveByTenant(UUID tenantId) {
        return supplierRepository.findAllActiveByTenant(tenantId).stream().map(SupplierSchema::toSupplierModel)
                .toList();
    }

    @Override
    public List<SupplierModel> findAllByTenant(UUID tenantId) {
        return supplierRepository.findAllByTenant(tenantId).stream().map(SupplierSchema::toSupplierModel).toList();
    }

    @Override
    public List<SupplierModel> findByType(UUID tenantId, String supplierType) {
        return supplierRepository.findByTypeAndTenant(tenantId, supplierType).stream()
                .map(SupplierSchema::toSupplierModel).toList();
    }

    @Override
    public SupplierModel save(SupplierModel model) {
        return supplierRepository.save(new SupplierSchema(model)).toSupplierModel();
    }

    @Override
    public SupplierModel update(SupplierModel model) {
        SupplierSchema schema = supplierRepository
                .findById(model.getId())
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));

        schema.setSupplierName(model.getSupplierName());
        schema.setSupplierType(model.getSupplierType());
        schema.setContactPerson(model.getContactPerson());
        schema.setEmail(model.getEmail());
        schema.setPhone(model.getPhone());
        schema.setAddress(model.getAddress());
        schema.setCity(model.getCity());
        schema.setProvince(model.getProvince());
        schema.setPostalCode(model.getPostalCode());
        schema.setBankAccount(model.getBankAccount());
        schema.setBankName(model.getBankName());
        schema.setTaxId(model.getTaxId());
        schema.setStatus(model.getStatus());
        schema.setRating(model.getRating());
        schema.setPaymentTerms(model.getPaymentTerms());
        schema.setNotes(model.getNotes());
        schema.setUpdatedBy(model.getUpdatedBy());

        SupplierSchema updated = supplierRepository.save(schema);
        return updated.toSupplierModel();
    }

    @Override
    public void delete(UUID id) {
        supplierRepository
                .findById(id)
                .ifPresent(
                        supplier -> {
                            supplier.setDeletedAt(Instant.now());
                            supplierRepository.save(supplier);
                        });
    }

    @Override
    public List<SupplierModel> searchByName(UUID tenantId, String searchTerm) {
        return supplierRepository.searchByName(tenantId, searchTerm).stream().map(SupplierSchema::toSupplierModel)
                .toList();
    }
}
