package com.kompu.api.infrastructure.appconfig.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.kompu.api.entity.appconfig.exception.AppConfigNotFoundException;
import com.kompu.api.entity.appconfig.gateway.AppConfigGateway;
import com.kompu.api.entity.appconfig.model.AppConfigModel;
import com.kompu.api.infrastructure.config.db.repository.AppConfigRepository;
import com.kompu.api.infrastructure.config.db.schema.AppConfigSchema;

public class AppConfigDatabaseGateway implements AppConfigGateway {

    private final AppConfigRepository repository;

    public AppConfigDatabaseGateway(AppConfigRepository repository) {
        this.repository = repository;
    }

    @Override
    public AppConfigModel create(AppConfigModel appConfigModel) {
        return repository.save(new AppConfigSchema(appConfigModel)).toAppConfigModel();
    }

    @Override
    public AppConfigModel update(AppConfigModel appConfigModel) {
        return repository.save(new AppConfigSchema(appConfigModel)).toAppConfigModel();
    }

    @Override
    public void delete(UUID id) throws AppConfigNotFoundException {
        if (!repository.existsById(id)) {
            throw new AppConfigNotFoundException();
        }
        repository.deleteById(id);
    }

    @Override
    public Optional<AppConfigModel> findById(UUID id) {
        return repository.findById(id).map(AppConfigSchema::toAppConfigModel);
    }

    @Override
    public Optional<AppConfigModel> findByConfigKey(String configKey) {
        return repository.findByConfigKey(configKey).map(AppConfigSchema::toAppConfigModel);
    }

    @Override
    public List<AppConfigModel> findAll() {
        return repository.findAll().stream()
                .map(AppConfigSchema::toAppConfigModel)
                .toList();
    }

}
