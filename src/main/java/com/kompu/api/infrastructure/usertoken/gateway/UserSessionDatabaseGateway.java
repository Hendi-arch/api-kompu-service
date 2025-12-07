package com.kompu.api.infrastructure.usertoken.gateway;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.kompu.api.entity.usertoken.exception.UserSessionNotFoundException;
import com.kompu.api.entity.usertoken.gateway.UserSessionGateway;
import com.kompu.api.entity.usertoken.model.UserSessionModel;
import com.kompu.api.infrastructure.config.db.repository.UserSessionRepository;
import com.kompu.api.infrastructure.config.db.schema.UserSessionSchema;

@Component
public class UserSessionDatabaseGateway implements UserSessionGateway {

    private final UserSessionRepository repository;

    public UserSessionDatabaseGateway(UserSessionRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserSessionModel create(UserSessionModel userSessionModel) {
        UserSessionSchema schema = new UserSessionSchema(userSessionModel);
        if (schema.getLastActiveAt() == null) {
            schema.setLastActiveAt(LocalDateTime.now());
        }
        return repository.save(schema).toUserSessionModel();
    }

    @Override
    public UserSessionModel update(UserSessionModel userSessionModel) {
        UserSessionSchema schema = new UserSessionSchema(userSessionModel);
        schema.setLastActiveAt(LocalDateTime.now());
        return repository.save(schema).toUserSessionModel();
    }

    @Override
    public void delete(UUID id) throws UserSessionNotFoundException {
        if (!repository.existsById(id)) {
            throw new UserSessionNotFoundException();
        }
        repository.deleteById(id);
    }

    @Override
    public Optional<UserSessionModel> findById(UUID id) {
        return repository.findById(id).map(UserSessionSchema::toUserSessionModel);
    }

    @Override
    public List<UserSessionModel> findByUserId(UUID userId) {
        return repository.findByUserId(userId).stream()
                .map(UserSessionSchema::toUserSessionModel)
                .toList();
    }

    @Override
    public List<UserSessionModel> findActiveSessionsByUserId(UUID userId) {
        return repository.findByUserIdAndIsActiveTrue(userId).stream()
                .map(UserSessionSchema::toUserSessionModel)
                .toList();
    }

    @Override
    public List<UserSessionModel> findByTenantId(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(UserSessionSchema::toUserSessionModel)
                .toList();
    }

    @Override
    public void deactivateSession(UUID sessionId) {
        Optional<UserSessionSchema> session = repository.findById(sessionId);
        if (session.isPresent()) {
            UserSessionSchema schema = session.get();
            schema.setIsActive(false);
            repository.save(schema);
        }
    }

}
