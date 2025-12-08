package com.kompu.api.infrastructure.config.web.security.config;

import java.security.KeyPair;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.kompu.api.usecase.appconfig.RsaKeyPairUseCase;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AppSecurityConfig {

	private final RsaKeyPairUseCase rsaKeyPairUseCase;

	public AppSecurityConfig(RsaKeyPairUseCase rsaKeyPairUseCase) {
		this.rsaKeyPairUseCase = rsaKeyPairUseCase;
	}

	@Bean
	public AuditorAware<String> auditorProvider() {
		return new AppSecurityAuditorConfig();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Provides a persistent RSA key pair for JWT signing.
	 * 
	 * The key pair is either reconstructed from database storage or
	 * generated and persisted for future application restarts.
	 *
	 * @return KeyPair with public and private RSA keys
	 * @throws RuntimeException if key pair cannot be created or loaded
	 */
	@Bean
	public KeyPair rsaKeyPair() {
		try {
			log.info("Initializing RSA key pair from persistent storage or generating new pair");
			return rsaKeyPairUseCase.getOrGenerateKeyPair();
		} catch (Exception e) {
			log.error("Failed to initialize RSA key pair", e);
			throw new RuntimeException("Failed to initialize RSA key pair for JWT signing", e);
		}
	}

}
