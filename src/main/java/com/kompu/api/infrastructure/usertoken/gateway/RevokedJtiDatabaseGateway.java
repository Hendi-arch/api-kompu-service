package com.kompu.api.infrastructure.usertoken.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.kompu.api.entity.usertoken.exception.RevokedJtiException;
import com.kompu.api.entity.usertoken.gateway.RevokedJtiGateway;
import com.kompu.api.entity.usertoken.model.RevokedJtiModel;
import com.kompu.api.infrastructure.config.db.repository.RevokedJtiRepository;
import com.kompu.api.infrastructure.config.db.schema.RevokedJtiSchema;

@Component
public class RevokedJtiDatabaseGateway implements RevokedJtiGateway {

    private final RevokedJtiRepository repository;

    public RevokedJtiDatabaseGateway(RevokedJtiRepository repository) {
        this.repository = repository;
    }

    @Override
    public RevokedJtiModel create(RevokedJtiModel revokedJtiModel) {
        return repository.save(new RevokedJtiSchema(revokedJtiModel)).toRevokedJtiModel();
    }

    @Override
    public void delete(UUID jti) {
        repository.deleteById(jti);
    }

    @Override
    public Optional<RevokedJtiModel> findByJti(UUID jti) {
        return repository.findByJti(jti).map(RevokedJtiSchema::toRevokedJtiModel);
    }

    @Override
    public List<RevokedJtiModel> findByUserId(UUID userId) {
        return repository.findByUserId(userId).stream()
                .map(RevokedJtiSchema::toRevokedJtiModel)
                .toList();
    }

    @Override
    public List<RevokedJtiModel> findAll() {
        return repository.findAll().stream()
                .map(RevokedJtiSchema::toRevokedJtiModel)
                .toList();
    }

    @Override
    public boolean isRevoked(UUID jti) throws RevokedJtiException {
        return repository.findByJti(jti).isPresent();
    }

}
