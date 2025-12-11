# DTO and Model Architecture Patterns

## Overview

This document describes the architectural patterns adopted for **Data Transfer Objects (DTOs)** and **domain models** across the api-kompu-service application, following clean architecture principles and Spring Boot best practices.

---

## Architecture Philosophy

### Layered Separation

```
┌─────────────────────────────────────────┐
│         HTTP Layer (Controllers)        │
│  ├─ Request DTOs (Immutable Records)   │
│  └─ Response DTOs (Immutable Records)  │
├─────────────────────────────────────────┤
│      Use Case Layer (Business Logic)    │
│  ├─ Domain Models (*Model classes)     │
│  ├─ Gateway Interfaces                 │
│  └─ Use Case Classes                   │
├─────────────────────────────────────────┤
│     Infrastructure Layer (Database)    │
│  ├─ Schema Classes (*Schema/@Entity)   │
│  ├─ Repository Interfaces (JPA)        │
│  └─ Gateway Implementations            │
└─────────────────────────────────────────┘
```

### Core Principles

1. **Immutability** - DTOs and models prevent accidental state changes
2. **Separation of Concerns** - Request, response, domain, and persistence models are distinct
3. **Validation** - Input validation at the HTTP boundary
4. **No Framework Leakage** - Domain models are Spring-agnostic POJOs
5. **Type Safety** - Compile-time checks prevent mapping errors

---

## Request DTOs

### Definition

**Request DTOs** are immutable records that represent incoming HTTP request payloads. They are located in `infrastructure/{domain}/dto/` packages.

### Characteristics

- ✅ **Immutable** - Implemented as Java records (immutable by definition)
- ✅ **Validated** - Use Jakarta validation annotations
- ✅ **Documentation** - Include Javadoc explaining purpose
- ✅ **No Setters** - Records are inherently immutable
- ✅ **No Framework Annotations** - Focus on domain concepts, not frameworks
- ✅ **Accessor Syntax** - Use record field names as accessors (e.g., `request.username()`)

### Example: SignUpRequest

```java
/**
 * DTO for user registration request
 *
 * Immutable request object for sign-up operations.
 * Uses validation annotations for input validation.
 */
public record SignUpRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password,

        @NotBlank(message = "Password confirmation is required")
        String confirmPassword,

        @NotBlank(message = "Full name is required")
        @Size(min = 1, max = 100, message = "Full name must be between 1 and 100 characters")
        String fullName) {
}
```

### Validation Annotations

| Annotation        | Purpose                              | Example              |
| ----------------- | ------------------------------------ | -------------------- |
| `@NotBlank`       | Validates field is not null or empty | Username required    |
| `@Email`          | Validates email format               | Valid email address  |
| `@Size(min, max)` | Validates string length              | Password 8-100 chars |
| `@NotNull`        | Validates field is not null          | Object references    |
| `@Positive`       | Validates numeric > 0                | Amounts, quantities  |
| `@Min/@Max`       | Validates numeric range              | Age 0-150            |

### Usage in Controllers

```java
// Record accessors use field names (not getter methods)
@PostMapping("/signup")
public ResponseEntity<WebHttpResponse<AuthTokenResponse>> signUp(
        @RequestBody SignUpRequest request) {

    // Access record fields using method-style notation
    String username = request.username();      // Not getUsername()
    String email = request.email();           // Not getEmail()
    String password = request.password();     // Not getPassword()
}
```

---

## Response DTOs

### Definition

**Response DTOs** are immutable records that represent outgoing HTTP response payloads. They control what data is exposed to API clients.

### Characteristics

- ✅ **Immutable** - Implemented as Java records
- ✅ **Serialization Control** - Use `@JsonProperty` for JSON field names
- ✅ **Data Hiding** - Omit sensitive fields (passwords, internal IDs)
- ✅ **API Consistency** - Use snake_case for JSON property names
- ✅ **Documentation** - Document all fields with Javadoc
- ✅ **Nested Records** - Support complex hierarchies (record within record)

### Example: AuthTokenResponse

```java
/**
 * DTO for authentication token response
 *
 * Immutable response object for authentication operations.
 * Serialized with snake_case JSON property names for API consistency.
 *
 * @param accessToken JWT access token
 * @param refreshToken Refresh token for token renewal
 * @param tokenType Bearer token type
 * @param expiresIn Token expiration time in seconds (7 days)
 * @param user Authenticated user information
 */
public record AuthTokenResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        Long expiresIn,

        @JsonProperty("user")
        UserAuthResponse user) {

    /**
     * Nested DTO for user authentication information
     *
     * Immutable response object containing minimal user details.
     *
     * @param id User unique identifier
     * @param username Username for login
     * @param email Email address
     * @param fullName User's full name
     * @param phone Phone number
     * @param avatarUrl Avatar/profile picture URL
     * @param isEmailVerified Email verification status
     * @param createdAt Account creation timestamp
     */
    public record UserAuthResponse(
            String id,
            String username,
            String email,
            String fullName,
            String phone,
            String avatarUrl,
            @JsonProperty("is_email_verified")
            boolean isEmailVerified,
            @JsonProperty("created_at")
            LocalDateTime createdAt) {
    }
}
```

### JSON Serialization Example

**Java Record:**

```java
new AuthTokenResponse(
    "eyJhbGciOiJSUzI1NiJ9...",
    "550e8400-e29b...",
    "Bearer",
    604800L,
    new UserAuthResponse(...)
)
```

**JSON Output:**

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiJ9...",
  "refresh_token": "550e8400-e29b...",
  "token_type": "Bearer",
  "expires_in": 604800,
  "user": {
    "id": "123e4567-e89b...",
    "username": "johndoe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "phone": "+1234567890",
    "avatarUrl": "https://...",
    "is_email_verified": false,
    "created_at": "2025-12-09T10:00:00"
  }
}
```

### Construction in Controllers

```java
// Use record constructor (clean and type-safe)
AuthTokenResponse.UserAuthResponse userAuthResponse =
    new AuthTokenResponse.UserAuthResponse(
        user.getId().toString(),
        user.getUsername(),
        user.getEmail(),
        user.getFullName(),
        user.getPhone(),
        user.getAvatarUrl(),
        user.isEmailVerified(),
        user.getCreatedAt());

AuthTokenResponse response = new AuthTokenResponse(
    accessToken,
    refreshToken,
    "Bearer",
    7 * 24 * 60 * 60L,
    userAuthResponse);
```

---

## Domain Models

### Definition

**Domain Models** represent core business entities and are located in `entity/{domain}/model/` packages. They contain pure business logic with **no Spring framework dependencies**.

### Characteristics

- ✅ **Pure Java** - No Spring annotations or imports
- ✅ **Immutable** - Use `@Builder` with Lombok for construction
- ✅ **Business Logic** - Encapsulate domain rules
- ✅ **Gateway Integration** - Use gateway interfaces for persistence
- ✅ **Equals/HashCode** - Implement proper equality
- ✅ **Type-Safe IDs** - Use generics for ID type (e.g., `AbstractEntity<UUID>`)

### Example: UserAccountModel

```java
/**
 * Domain model representing a user account.
 *
 * Pure Java POJO with no Spring framework dependencies.
 * Business logic encapsulated within the model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserAccountModel extends AbstractEntity<UUID> {

    private UUID id;

    private UUID tenantId;

    private String username;

    private String email;

    private String passwordHash;

    private String fullName;

    private String phone;

    private String avatarUrl;

    private boolean isActive;

    private boolean isEmailVerified;

    private boolean isSystem;

    @Builder.Default
    private Set<RoleModel> roles = new HashSet<>();

    // Audit fields
    private String createdBy;

    private String updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    /**
     * Business logic: Check if user is deleted (soft delete)
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Business logic: Check if user has specific role
     */
    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(r -> r.getName().equals(roleName));
    }
}
```

### Model Lifecycle

```
1. Creation (Use Case)
   └─ UserAccountModel.builder()
                .id(UUID.randomUUID())
                .username("johndoe")
                .email("john@example.com")
                .passwordHash(bcryptHash)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build()

2. Validation (Use Case)
   └─ if (userModel.isDeleted()) throw UserNotFoundException()

3. Persistence (Gateway)
   └─ new UserSchema(userModel)  // Convert to schema for JPA

4. Retrieval (Gateway)
   └─ userSchema.toUserAccountModel()  // Convert schema back to model

5. Response (Controller)
   └─ buildAuthTokenResponse(userModel)  // Map model to response DTO
```

---

## Schema Classes (Database Layer)

### Definition

**Schema Classes** are JPA `@Entity` classes located in `infrastructure/config/db/schema/`. They bridge domain models and the database.

### Characteristics

- ✅ **JPA Annotations** - `@Entity`, `@Column`, `@Table`
- ✅ **Converter Methods** - `toModel()` and constructor `Schema(model)`
- ✅ **Audit Support** - Use `@EntityListeners(AuditingEntityListener.class)`
- ✅ **Lombok Integration** - `@Data`, `@Builder` for boilerplate
- ✅ **Database Constraints** - Uniqueness, foreign keys, non-null rules

### Example: UserSchema

```java
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users", schema = "app", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "tenant_id", "email" }),
    @UniqueConstraint(columnNames = { "tenant_id", "username" })
})
public class UserSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String fullName;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor for converting domain model to schema
     */
    public UserSchema(UserAccountModel userAccountModel) {
        this.id = userAccountModel.getId();
        this.tenantId = userAccountModel.getTenantId();
        this.username = userAccountModel.getUsername();
        this.email = userAccountModel.getEmail();
        this.passwordHash = userAccountModel.getPasswordHash();
        this.fullName = userAccountModel.getFullName();
        // ... more fields
    }

    /**
     * Converter method for converting schema back to domain model
     */
    public UserAccountModel toUserAccountModel() {
        return UserAccountModel.builder()
                .id(this.id)
                .tenantId(this.tenantId)
                .username(this.username)
                .email(this.email)
                .passwordHash(this.passwordHash)
                .fullName(this.fullName)
                // ... more fields
                .build();
    }
}
```

---

## Data Flow Patterns

### Create User Flow

```
1. HTTP Request
   ├─ POST /api/v1/auth/signup
   ├─ Body: { "username": "johndoe", "email": "john@example.com", ... }
   └─ Type: SignUpRequest (record, immutable, validated)

2. Controller
   ├─ Extract fields from request record
   ├─ Call createUserUseCase.createUser()
   └─ Receive UserAccountModel (domain model)

3. Use Case
   ├─ Validate business rules
   ├─ Hash password with BCrypt
   ├─ Create UserAccountModel with builder
   ├─ Call userGateway.create(userAccountModel)
   └─ Return UserAccountModel

4. Gateway (Infrastructure)
   ├─ Convert UserAccountModel → UserSchema
   ├─ Call userRepository.save(userSchema)
   ├─ Persist to database
   ├─ Convert UserSchema → UserAccountModel
   └─ Return UserAccountModel

5. Controller Response
   ├─ Build AuthTokenResponse from UserAccountModel
   ├─ Construct record with nested UserAuthResponse
   └─ Return ResponseEntity with 201 Created

6. HTTP Response
   ├─ Serialize AuthTokenResponse to JSON
   ├─ Apply @JsonProperty for snake_case naming
   └─ Send to client
```

### Get User Flow

```
1. Database
   └─ UserSchema persisted with all fields

2. Gateway (Infrastructure)
   ├─ userRepository.findByUsername("johndoe")
   ├─ Receive UserSchema
   ├─ Call userSchema.toUserAccountModel()
   └─ Return UserAccountModel

3. Use Case
   ├─ Receive UserAccountModel from gateway
   ├─ Apply business logic (check isActive, etc.)
   └─ Return UserAccountModel

4. Controller
   ├─ Receive UserAccountModel
   ├─ Map to response DTO
   └─ Construct AuthTokenResponse record
```

---

## Best Practices

### ✅ DO

- ✅ **Use Records for DTOs** - Immutability and conciseness
- ✅ **Validate at Boundaries** - Validation annotations on request DTOs
- ✅ **Document with Javadoc** - Explain DTO purpose and field meanings
- ✅ **Hide Sensitive Data** - Exclude passwords, tokens from response DTOs
- ✅ **Use Builder Pattern for Models** - Clear construction, readable code
- ✅ **Separate Request/Response DTOs** - One DTO per responsibility
- ✅ **Implement Converter Methods** - Explicit schema ↔ model conversions
- ✅ **Use Nested Records** - Complex hierarchies via nested records
- ✅ **Consistent Naming** - CamelCase for Java, snake_case for JSON

### ❌ DON'T

- ❌ **Don't Share DTOs Between Layers** - Each layer owns its DTOs
- ❌ **Don't Expose Database IDs** - Use domain IDs in responses
- ❌ **Don't Include Passwords in Responses** - Even hashed passwords
- ❌ **Don't Use Lombok for Responses** - Keep responses clean and explicit
- ❌ **Don't Mix Request/Response** - Separate concerns
- ❌ **Don't Add Business Logic to DTOs** - Logic belongs in models/use cases
- ❌ **Don't Skip Validation** - Validate all inputs at HTTP boundary
- ❌ **Don't Mutate Records** - If needed, use models instead

---

## Directory Structure

```
infrastructure/
├── auth/
│   ├── controller/
│   │   └── AuthController.java
│   ├── dto/
│   │   ├── SignUpRequest.java           (request DTO)
│   │   ├── SignInRequest.java           (request DTO)
│   │   ├── ChangePasswordRequest.java   (request DTO)
│   │   ├── RefreshTokenRequest.java     (request DTO)
│   │   └── AuthTokenResponse.java       (response DTO)
│   └── gateway/
│       ├── UserDatabaseGateway.java
│       └── RefreshTokenDatabaseGateway.java
├── config/
│   └── db/
│       ├── schema/
│       │   ├── UserSchema.java
│       │   ├── RefreshTokenSchema.java
│       │   └── UserSessionSchema.java
│       └── repository/
│           ├── UserRepository.java
│           └── RefreshTokenRepository.java

entity/
├── user/
│   ├── model/
│   │   └── UserAccountModel.java        (domain model)
│   ├── gateway/
│   │   └── UserGateway.java
│   └── exception/
│       └── UserNotFoundException.java
├── usertoken/
│   ├── model/
│   │   ├── RefreshTokenModel.java       (domain model)
│   │   └── UserSessionModel.java        (domain model)
│   ├── gateway/
│   │   ├── RefreshTokenGateway.java
│   │   └── UserSessionGateway.java
│   └── exception/
│       ├── RefreshTokenNotFoundException.java
│       └── UserSessionNotFoundException.java

usecase/
├── user/
│   ├── CreateUserUseCase.java
│   ├── ValidateUserCredentialsUseCase.java
│   └── ChangePasswordUseCase.java
└── usertoken/
    ├── GenerateAccessTokenUseCase.java
    ├── GenerateRefreshTokenUseCase.java
    ├── ValidateRefreshTokenUseCase.java
    └── CreateUserSessionUseCase.java
```

---

## Summary

The DTO and model architecture follows clean architecture principles:

| Layer        | Class Type     | Mutability         | Validation              | Framework             |
| ------------ | -------------- | ------------------ | ----------------------- | --------------------- |
| **HTTP**     | Request DTOs   | Immutable (Record) | @NotBlank, @Email, etc. | Spring Framework      |
| **HTTP**     | Response DTOs  | Immutable (Record) | None                    | Jackson @JsonProperty |
| **Business** | Domain Models  | Builder (Mutable)  | Business logic methods  | Pure Java             |
| **Database** | Schema Classes | Mutable (@Entity)  | Constraints             | JPA/Hibernate         |

This separation ensures:

- **Type Safety** - Compile-time verification
- **Flexibility** - Each layer evolves independently
- **Testability** - Models can be tested without frameworks
- **Maintainability** - Clear responsibilities per class
- **Security** - Sensitive data hidden from API responses
- **Scalability** - Easy to add new features without breaking existing code
