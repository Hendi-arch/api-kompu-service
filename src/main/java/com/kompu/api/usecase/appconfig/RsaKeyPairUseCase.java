package com.kompu.api.usecase.appconfig;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;

import com.kompu.api.entity.appconfig.gateway.AppConfigGateway;
import com.kompu.api.entity.appconfig.model.AppConfigModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RsaKeyPairUseCase {

    private static final String RSA_PUBLIC_KEY_CONFIG = "rsa_public_key";
    private static final String RSA_PRIVATE_KEY_CONFIG = "rsa_private_key";
    private static final String RSA_ALGORITHM = "RSA";
    private static final int RSA_KEY_SIZE = 2048;

    private final AppConfigGateway appConfigGateway;

    public RsaKeyPairUseCase(AppConfigGateway appConfigGateway) {
        this.appConfigGateway = appConfigGateway;
    }

    /**
     * Get or generate RSA key pair.
     * If keys already exist in the database, they are reconstructed and returned.
     * If no keys exist, a new pair is generated and persisted.
     *
     * @return KeyPair with public and private keys
     * @throws Exception if key generation or retrieval fails
     */
    public KeyPair getOrGenerateKeyPair() throws Exception {
        // Try to load existing keys from database
        Optional<AppConfigModel> publicKeyConfig = appConfigGateway.findByConfigKey(RSA_PUBLIC_KEY_CONFIG);
        Optional<AppConfigModel> privateKeyConfig = appConfigGateway.findByConfigKey(RSA_PRIVATE_KEY_CONFIG);

        if (publicKeyConfig.isPresent() && privateKeyConfig.isPresent()) {
            log.info("Loading existing RSA key pair from database");
            return reconstructKeyPairFromStorage(
                    publicKeyConfig.get().getConfigValue(),
                    privateKeyConfig.get().getConfigValue());
        }

        log.info("No RSA keys found in database. Generating new key pair");
        return generateAndPersistKeyPair();
    }

    /**
     * Generate a new RSA key pair and persist it to the database.
     *
     * @return newly generated KeyPair
     * @throws Exception if key generation fails
     */
    public KeyPair generateAndPersistKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyPairGenerator.initialize(RSA_KEY_SIZE);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // Encode keys to Base64 and store in database
        String publicKeyString = encodePublicKey(keyPair.getPublic());
        String privateKeyString = encodePrivateKey(keyPair.getPrivate());

        // Persist public key
        AppConfigModel publicKeyConfig = AppConfigModel.builder()
                .configKey(RSA_PUBLIC_KEY_CONFIG)
                .configValue(publicKeyString)
                .description("RSA-2048 Public Key for JWT token signing")
                .build();
        appConfigGateway.create(publicKeyConfig);

        // Persist private key
        AppConfigModel privateKeyConfig = AppConfigModel.builder()
                .configKey(RSA_PRIVATE_KEY_CONFIG)
                .configValue(privateKeyString)
                .description("RSA-2048 Private Key for JWT token signing (KEEP SECURE)")
                .build();
        appConfigGateway.create(privateKeyConfig);

        log.info("New RSA key pair generated and persisted to database");
        return keyPair;
    }

    /**
     * Reconstruct a KeyPair from Base64-encoded public and private key strings.
     *
     * @param publicKeyString  Base64-encoded public key
     * @param privateKeyString Base64-encoded private key
     * @return reconstructed KeyPair
     * @throws Exception if key reconstruction fails
     */
    private KeyPair reconstructKeyPairFromStorage(String publicKeyString, String privateKeyString) throws Exception {
        PublicKey publicKey = decodePublicKey(publicKeyString);
        PrivateKey privateKey = decodePrivateKey(privateKeyString);
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * Encode a public key to Base64 string.
     *
     * @param publicKey the public key to encode
     * @return Base64-encoded public key
     */
    private String encodePublicKey(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Encode a private key to Base64 string.
     *
     * @param privateKey the private key to encode
     * @return Base64-encoded private key
     */
    private String encodePrivateKey(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * Decode a Base64-encoded public key string to PublicKey object.
     *
     * @param publicKeyString Base64-encoded public key
     * @return PublicKey object
     * @throws Exception if decoding fails
     */
    private PublicKey decodePublicKey(String publicKeyString) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(publicKeyString);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePublic(spec);
    }

    /**
     * Decode a Base64-encoded private key string to PrivateKey object.
     *
     * @param privateKeyString Base64-encoded private key
     * @return PrivateKey object
     * @throws Exception if decoding fails
     */
    private PrivateKey decodePrivateKey(String privateKeyString) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(privateKeyString);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePrivate(spec);
    }

}
