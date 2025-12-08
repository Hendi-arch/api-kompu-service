package com.kompu.api.entity.appconfig.model;

import java.util.UUID;

import com.kompu.api.entity.AbstractEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AppConfigModel extends AbstractEntity<UUID> {

    private UUID id;

    private String configKey;

    private String configValue;

    private String description;

}
