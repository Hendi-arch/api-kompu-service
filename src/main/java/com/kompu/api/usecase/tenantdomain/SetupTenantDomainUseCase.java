package com.kompu.api.usecase.tenantdomain;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kompu.api.entity.tenantdomain.model.TenantDomainModel;
import com.kompu.api.entity.tenantdomain.gateway.TenantDomainGateway;

/**
 * SetupTenantDomainUseCase orchestrates the creation and configuration of
 * domains for a tenant.
 * 
 * This use case handles:
 * 1. Creating the initial primary domain for a tenant
 * 2. Generating domain names from tenant code
 * 3. Setting up HTTPS/TLS configuration
 * 4. Managing primary vs secondary domains
 * 5. Validating domain uniqueness across the system
 * 
 * Responsibilities:
 * - Generate platform-provided domains (e.g., "tenant-code.kompu.id")
 * - Create custom domain entries
 * - Configure TLS certificate providers (Cloudflare, Let's Encrypt, etc.)
 * - Ensure domain uniqueness globally
 * - Set primary domain for tenant
 * 
 * This class is completely framework-agnostic and contains pure business logic.
 */
public class SetupTenantDomainUseCase {

    private static final String PLATFORM_DOMAIN_SUFFIX = "kompu.id";
    private final TenantDomainGateway tenantDomainGateway;

    /**
     * Constructor with gateway dependency injection
     * 
     * @param tenantDomainGateway the gateway for tenant domain persistence
     */
    public SetupTenantDomainUseCase(TenantDomainGateway tenantDomainGateway) {
        this.tenantDomainGateway = tenantDomainGateway;
    }

    /**
     * Setup initial domain for a new tenant using the tenant code.
     * 
     * Creates a primary domain with format: "{tenantCode}.kompu.id"
     * 
     * @param tenantId   the tenant ID
     * @param tenantCode the tenant code (e.g., "koperasi1")
     * @return the created domain model
     * @throws IllegalArgumentException if tenant code is invalid
     */
    public TenantDomainModel setupInitialDomain(UUID tenantId, String tenantCode) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
        if (tenantCode == null || tenantCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant code cannot be empty");
        }

        // Generate platform domain
        String host = generatePlatformDomain(tenantCode);

        // Create the primary domain model
        TenantDomainModel domain = TenantDomainModel.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .host(host)
                .primary(true)
                .custom(false)
                .httpsEnabled(true)
                .tlsProvider("cloudflare") // Default to Cloudflare for managed HTTPS
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Persist via gateway
        return tenantDomainGateway.create(domain);
    }

    /**
     * Add a custom domain to a tenant.
     * 
     * @param tenantId    the tenant ID
     * @param customHost  the custom domain hostname (e.g., "www.mycompany.com")
     * @param makePrimary whether to make this the primary domain
     * @param tlsProvider the TLS certificate provider ("cloudflare", "letsencrypt",
     *                    etc.)
     * @return the created domain model
     * @throws IllegalArgumentException if domain parameters are invalid
     * @throws IllegalStateException    if host is already used by another tenant
     */
    public TenantDomainModel addCustomDomain(
            UUID tenantId,
            String customHost,
            boolean makePrimary,
            String tlsProvider) {

        // Validate input
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
        if (customHost == null || customHost.trim().isEmpty()) {
            throw new IllegalArgumentException("Custom host cannot be empty");
        }
        if (tlsProvider == null || tlsProvider.isEmpty()) {
            tlsProvider = "cloudflare";
        }

        // Normalize host
        String normalizedHost = customHost.toLowerCase().trim();

        // Check if host is already used
        if (tenantDomainGateway.existsByHost(normalizedHost)) {
            throw new IllegalStateException(
                    "Domain '" + normalizedHost + "' is already in use by another tenant");
        }

        // If making this primary, update existing primary domain to secondary
        if (makePrimary) {
            var primaryOpt = tenantDomainGateway.findPrimaryByTenantId(tenantId);
            if (primaryOpt.isPresent()) {
                TenantDomainModel oldPrimary = primaryOpt.get();
                oldPrimary.setSecondaryDomain();
                tenantDomainGateway.update(oldPrimary);
            }
        }

        // Create the custom domain model
        TenantDomainModel domain = TenantDomainModel.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .host(normalizedHost)
                .primary(makePrimary)
                .custom(true)
                .httpsEnabled(true)
                .tlsProvider(tlsProvider)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Persist via gateway
        return tenantDomainGateway.create(domain);
    }

    /**
     * Set a domain as the primary domain for a tenant.
     * 
     * @param domainId the domain ID to make primary
     * @return the updated domain
     * @throws IllegalStateException if domain not found or belongs to different
     *                               tenant
     */
    public TenantDomainModel setPrimaryDomain(UUID domainId) {
        var domainOpt = tenantDomainGateway.findById(domainId);
        if (domainOpt.isEmpty()) {
            throw new IllegalStateException("Domain not found");
        }

        TenantDomainModel domain = domainOpt.get();
        UUID tenantId = domain.getTenantId();

        // Remove primary status from current primary (if exists)
        var currentPrimary = tenantDomainGateway.findPrimaryByTenantId(tenantId);
        if (currentPrimary.isPresent() && !currentPrimary.get().getId().equals(domainId)) {
            TenantDomainModel oldPrimary = currentPrimary.get();
            oldPrimary.setSecondaryDomain();
            tenantDomainGateway.update(oldPrimary);
        }

        // Set new primary
        domain.setPrimaryDomain();
        return tenantDomainGateway.update(domain);
    }

    /**
     * Configure HTTPS/TLS for a domain.
     * 
     * @param domainId     the domain ID
     * @param httpsEnabled whether HTTPS should be enabled
     * @param tlsProvider  the TLS provider ("cloudflare", "letsencrypt", "custom")
     * @return the updated domain
     */
    public TenantDomainModel configureTls(UUID domainId, boolean httpsEnabled, String tlsProvider) {
        var domainOpt = tenantDomainGateway.findById(domainId);
        if (domainOpt.isEmpty()) {
            throw new IllegalStateException("Domain not found");
        }

        TenantDomainModel domain = domainOpt.get();

        if (httpsEnabled) {
            domain.enableHttps();
        } else {
            domain.disableHttps();
        }

        if (tlsProvider != null && !tlsProvider.isEmpty()) {
            domain.setTlsProvider(tlsProvider);
        }

        return tenantDomainGateway.update(domain);
    }

    /**
     * Generate a platform-provided domain name from tenant code.
     * 
     * Format: "{tenantCode}.kompu.id"
     * 
     * @param tenantCode the tenant code
     * @return the generated domain name
     */
    private String generatePlatformDomain(String tenantCode) {
        return tenantCode.toLowerCase().trim() + "." + PLATFORM_DOMAIN_SUFFIX;
    }
}
