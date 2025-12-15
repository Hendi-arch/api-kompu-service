package com.kompu.api.infrastructure.system.gateway;

import org.springframework.stereotype.Service;

import com.kompu.api.entity.system.gateway.LoginLogGateway;
import com.kompu.api.entity.system.model.LoginLogModel;
import com.kompu.api.infrastructure.config.db.repository.LoginLogRepository;
import com.kompu.api.infrastructure.config.db.schema.LoginLogSchema;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LoginLogDatabaseGateway implements LoginLogGateway {

    private final LoginLogRepository loginLogRepository;

    public LoginLogDatabaseGateway(LoginLogRepository loginLogRepository) {
        this.loginLogRepository = loginLogRepository;
    }

    @Override
    public LoginLogModel save(LoginLogModel loginLog) {
        log.info("Saving login log for email: {}", loginLog.getEmail());
        LoginLogSchema schema = new LoginLogSchema(loginLog);
        return loginLogRepository.save(schema).toModel();
    }

}
