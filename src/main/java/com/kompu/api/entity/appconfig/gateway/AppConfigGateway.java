package com.kompu.api.entity.appconfig.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.kompu.api.entity.appconfig.exception.AppConfigNotFoundException;
import com.kompu.api.entity.appconfig.model.AppConfigModel;

public interface AppConfigGateway {

    AppConfigModel create(AppConfigModel appConfigModel);

    AppConfigModel update(AppConfigModel appConfigModel);

    void delete(UUID id) throws AppConfigNotFoundException;

    Optional<AppConfigModel> findById(UUID id);

    Optional<AppConfigModel> findByConfigKey(String configKey);

    List<AppConfigModel> findAll();

}
