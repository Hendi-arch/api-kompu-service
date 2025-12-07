package com.kompu.api.infrastructure.usertoken.gateway;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.kompu.api.entity.usertoken.exception.RefreshTokenNotFoundException;
import com.kompu.api.entity.usertoken.gateway.RefreshTokenGateway;
import com.kompu.api.entity.usertoken.model.RefreshTokenModel;
import com.kompu.api.infrastructure.config.db.repository.RefreshTokenRepository;
import com.kompu.api.infrastructure.config.db.schema.RefreshTokenSchema;

@Component
public class RefreshTokenDatabaseGateway implements RefreshTokenGateway {

    private final RefreshTokenRepository repository;

    public RefreshTokenDatabaseGateway(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public RefreshTokenModel create(RefreshTokenModel refreshTokenModel) {
        return repository.save(new RefreshTokenSchema(refreshTokenModel)).toRefreshTokenModel();
    }

    @Override
    public void delete(UUID id) throws RefreshTokenNotFoundException {
        if (!repository.existsById(id)) {
            throw new RefreshTokenNotFoundException();
        }
        repository.deleteById(id);
    }

    @Override
    public Optional<RefreshTokenModel> findById(UUID id) {
        return repository.findById(id).map(RefreshTokenSchema::toRefreshTokenModel);
    }

    @Override
    public List<RefreshTokenModel> findByUserId(UUID userId) {
        return repository.findByUserId(userId).stream()
                .map(RefreshTokenSchema::toRefreshTokenModel)
                .toList();
    }

    @Override
    public List<RefreshTokenModel> findBySessionId(UUID sessionId) {
        return repository.findBySessionId(sessionId).stream()
                .map(RefreshTokenSchema::toRefreshTokenModel)
                .toList();
    }

    @Override
    public Optional<RefreshTokenModel> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(RefreshTokenSchema::toRefreshTokenModel);
    }

    @Override
    public void revokeToken(UUID tokenId) {
        Optional<RefreshTokenSchema> token = repository.findById(tokenId);
        if (token.isPresent()) {
            RefreshTokenSchema schema = token.get();
            schema.setRevokedAt(LocalDateTime.now());
            repository.save(schema);
        }
    }

    @Override
    public void revokeAllTokensByUserId(UUID userId) {
        List<RefreshTokenSchema> tokens = repository.findByUserIdAndRevokedAtIsNull(userId);
        tokens.forEach(token -> {
            token.setRevokedAt(LocalDateTime.now());
            repository.save(token);
        });
    }

}
