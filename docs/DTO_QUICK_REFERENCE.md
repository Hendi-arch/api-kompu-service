# DTO & Model Architecture - Quick Reference

## Record vs Lombok Class Comparison

### Java Records (DTOs)

```java
// IMMUTABLE - PREFERRED FOR DTOs
public record SignUpRequest(
    @NotBlank String username,
    @Email String email,
    @NotBlank String password) {
}

// Accessor syntax: request.username() (NOT request.getUsername())
// No setters (immutable)
// Auto-generated equals/hashCode/toString
// Lightweight, optimized by compiler
```

### Lombok Classes (Domain Models)

```java
// MUTABLE - USED FOR DOMAIN MODELS
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountModel extends AbstractEntity<UUID> {
    private UUID id;
    private String username;
    private String email;
    // Has setters for builder pattern
}

// Accessor syntax: user.getUsername() OR with Lombok project: user.username()
// Has setters
// Manually generated equals/hashCode
// Full control via annotations
```

---

## Layer Responsibilities

| Layer             | Class Type      | Example           | Mutable? | Framework         |
| ----------------- | --------------- | ----------------- | -------- | ----------------- |
| **HTTP (Input)**  | Request Record  | SignUpRequest     | ❌ No    | Spring Validation |
| **HTTP (Output)** | Response Record | AuthTokenResponse | ❌ No    | Jackson JSON      |
| **Business**      | Domain Model    | UserAccountModel  | ✅ Yes\* | Pure Java         |
| **Database**      | Schema Entity   | UserSchema        | ✅ Yes   | JPA/Hibernate     |

\*Built with builder, then used immutably in use cases

---

## Quick Conversion Guide

### Accessing Record Fields

```java
// ✅ CORRECT - Record accessor (field name)
SignUpRequest req = new SignUpRequest("john", "john@example.com", "pass123");
String username = req.username();
String email = req.email();

// ❌ WRONG - No getters on records
String username = req.getUsername();  // Compilation error!
```

### Creating Records

```java
// ✅ CORRECT - Direct constructor
AuthTokenResponse response = new AuthTokenResponse(
    accessToken,
    refreshToken,
    "Bearer",
    604800L,
    userAuthResponse);

// ❌ WRONG - Records don't have builders
AuthTokenResponse response = AuthTokenResponse.builder()
    .accessToken(accessToken)
    .build();  // Compilation error!
```

### Nested Records

```java
// ✅ CORRECT - Access nested record via parameter
public record AuthTokenResponse(
    String accessToken,
    UserAuthResponse user) {

    public record UserAuthResponse(
        String id,
        String username) {
    }
}

// Create nested record
UserAuthResponse userAuth = new UserAuthResponse("123", "john");
AuthTokenResponse auth = new AuthTokenResponse(
    "token123",
    userAuth);

// Access nested data
String userId = auth.user().id();
String username = auth.user().username();
```

---

## Validation Annotations

### Common Annotations on DTOs

```java
public record SignUpRequest(
    @NotBlank(message = "Username is required")              // Not null, not empty
    @Size(min = 3, max = 50, message = "...")               // String length
    String username,

    @NotBlank(message = "Email is required")
    @Email(message = "Email format invalid")                 // Valid email
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "...")              // Min/max length
    String password,

    @NotNull(message = "Age is required")                    // Not null (objects)
    @Positive(message = "Age must be positive")              // > 0
    Integer age,

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "...")                         // >= value
    @Max(value = 1000000, message = "...")                   // <= value
    Double amount) {
}
```

### Validation Error Response

```json
{
  "status": 400,
  "message": "Bad Request",
  "data": [
    {
      "field": "email",
      "message": "Email format invalid"
    },
    {
      "field": "password",
      "message": "Password must be between 8 and 100 characters"
    }
  ]
}
```

---

## JSON Serialization

### Request DTO (Input)

```java
// Java Record
public record SignUpRequest(
    String username,
    String email,
    String password) {
}

// JSON Input (auto-mapped by Spring)
{
  "username": "john",
  "email": "john@example.com",
  "password": "pass123"
}
```

### Response DTO (Output)

```java
// Java Record with @JsonProperty
public record AuthTokenResponse(
    @JsonProperty("access_token")
    String accessToken,

    @JsonProperty("refresh_token")
    String refreshToken,

    @JsonProperty("token_type")
    String tokenType) {
}

// JSON Output (mapped by @JsonProperty)
{
  "access_token": "eyJhbGc...",
  "refresh_token": "550e84...",
  "token_type": "Bearer"
}
```

---

## Controller Examples

### Accept Request DTO

```java
@PostMapping("/signup")
public ResponseEntity<WebHttpResponse<AuthTokenResponse>> signUp(
        @RequestBody SignUpRequest request,      // Validated request record
        HttpServletRequest httpRequest) {

    // Access record fields directly (field name as method)
    log.info("Sign up: {}", request.username());

    // Pass to use case
    UserAccountModel user = createUserUseCase.createUser(
        request.username(),
        request.email(),
        request.password(),
        request.fullName(),
        defaultTenantId);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(WebHttpResponse.created(buildResponse(user)));
}
```

### Build Response DTO

```java
private AuthTokenResponse buildResponse(UserAccountModel user) {
    // Create nested record
    var userAuth = new AuthTokenResponse.UserAuthResponse(
        user.getId().toString(),
        user.getUsername(),
        user.getEmail(),
        user.getFullName(),
        user.getPhone(),
        user.getAvatarUrl(),
        user.isEmailVerified(),
        user.getCreatedAt());

    // Create main response record
    return new AuthTokenResponse(
        accessToken,
        refreshToken,
        "Bearer",
        604800L,
        userAuth);
}
```

---

## Data Flow Example

### Sign Up Flow

```
1. HTTP Request → SignUpRequest (record, immutable, validated)
   {
     "username": "john",
     "email": "john@example.com",
     "password": "SecurePass123!"
   }

2. Controller receives SignUpRequest
   ✓ Validation happens automatically (NotBlank, Email, Size)
   ✓ If validation fails → 400 Bad Request

3. Controller extracts fields using record accessors
   username = request.username()
   email = request.email()
   password = request.password()

4. Pass to CreateUserUseCase
   UserAccountModel user = createUserUseCase.createUser(...)

5. CreateUserUseCase returns UserAccountModel (domain model)

6. Controller maps UserAccountModel to AuthTokenResponse (response record)
   ✓ Creates nested UserAuthResponse record
   ✓ Wraps sensitive fields (no passwords)

7. Spring serializes response record to JSON
   ✓ @JsonProperty converts field names to snake_case
   ✓ Sends 201 Created with AuthTokenResponse payload

8. Client receives JSON response
   {
     "access_token": "eyJhbGc...",
     "refresh_token": "550e84...",
     "token_type": "Bearer",
     "expires_in": 604800,
     "user": {
       "id": "550e8400...",
       "username": "john",
       "email": "john@example.com",
       ...
     }
   }
```

---

## Common Patterns

### Pattern 1: Simple Request

```java
public record LoginRequest(
    @NotBlank String username,
    @NotBlank String password) {
}

// Controller usage
UserAccountModel user = validateUserCredentialsUseCase.validateCredentials(
    request.username(),
    request.password());
```

### Pattern 2: Request with Nested Validation

```java
public record CreatePostRequest(
    @NotBlank @Size(min = 5) String title,
    @NotBlank @Size(min = 20) String content,
    @NotNull List<String> tags) {
}
```

### Pattern 3: Response with Nested Record

```java
public record PostResponse(
    @JsonProperty("post_id") String id,
    String title,
    String content,
    AuthorResponse author) {

    public record AuthorResponse(
        String id,
        String name) {
    }
}
```

### Pattern 4: Pagination Response

```java
public record PaginatedResponse<T>(
    List<T> items,
    long total,
    int page,
    int size) {
}

// Usage
PaginatedResponse<UserResponse> users =
    new PaginatedResponse<>(userList, totalCount, 0, 20);
```

---

## Migration Checklist

When adding new DTOs:

- [ ] Use `public record` for request/response DTOs
- [ ] Add `@NotBlank`, `@NotNull`, `@Email`, etc. for validation
- [ ] Add Javadoc comments explaining DTO purpose
- [ ] Add `@JsonProperty` for snake_case JSON fields
- [ ] Document all record parameters with @param
- [ ] Use nested records for complex structures
- [ ] Keep response DTOs minimal (hide sensitive data)
- [ ] Test record field access using field names (not getters)
- [ ] Verify JSON serialization with expected snake_case naming

---

## Troubleshooting

### ❌ "method getUsername() is undefined"

**Solution:** Records use field names, not getters

```java
// ❌ Wrong
request.getUsername()

// ✅ Right
request.username()
```

### ❌ "method builder() is undefined"

**Solution:** Records don't have builders, use constructors

```java
// ❌ Wrong
new AuthTokenResponse.builder()
    .accessToken("token")
    .build()

// ✅ Right
new AuthTokenResponse(
    "token",
    "refresh",
    "Bearer",
    604800L,
    userAuth)
```

### ❌ Validation not working on request

**Ensure:** Request class is marked as `@RequestBody` and request DTO is properly defined

```java
@PostMapping("/signup")
public ResponseEntity<...> signUp(
    @RequestBody SignUpRequest request) {  // ✅ @RequestBody required
    // Validation happens automatically
}
```

### ❌ JSON field names wrong in response

**Add:** `@JsonProperty` annotations for snake_case mapping

```java
public record AuthTokenResponse(
    @JsonProperty("access_token")  // ✅ Maps Java camelCase to JSON snake_case
    String accessToken) {
}
```

---

## References

- **Java Records:** https://docs.oracle.com/en/java/javase/21/language/records.html
- **Jakarta Validation:** https://jakarta.ee/specifications/validation/3.0/
- **Spring Data:** https://spring.io/projects/spring-data
- **Clean Architecture:** https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html
