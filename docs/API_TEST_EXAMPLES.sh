#!/bin/bash

# Authentication API Test Scripts
# Base URL: http://localhost:3333/api/v1/auth

# ============================= SIGN UP =============================
# Create a new user account

curl -X POST http://localhost:3333/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "SecurePassword123!",
    "confirmPassword": "SecurePassword123!",
    "fullName": "John Doe"
  }'

# Expected Response (201 Created):
# {
#   "status": 201,
#   "message": "Created",
#   "data": {
#     "access_token": "eyJhbGciOiJSUzI1NiJ9...",
#     "refresh_token": "550e8400-e29b-41d4-a716-446655440000",
#     "token_type": "Bearer",
#     "expires_in": 604800,
#     "user": {
#       "id": "550e8400-e29b-41d4-a716-446655440001",
#       "username": "johndoe",
#       "email": "john@example.com",
#       "fullName": "John Doe",
#       "phone": null,
#       "avatarUrl": null,
#       "isEmailVerified": false,
#       "created_at": "2025-12-09T10:00:00"
#     }
#   }
# }

# ============================= SIGN IN =============================
# Authenticate existing user

curl -X POST http://localhost:3333/api/v1/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "SecurePassword123!"
  }'

# Expected Response (200 OK):
# Same structure as Sign Up response

# ============================= REFRESH TOKEN =============================
# Get new access token using refresh token

curl -X POST http://localhost:3333/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refresh_token": "550e8400-e29b-41d4-a716-446655440000"
  }'

# Expected Response (200 OK):
# {
#   "status": 200,
#   "message": "Ok",
#   "data": {
#     "access_token": "eyJhbGciOiJSUzI1NiJ9...(new token)...",
#     "refresh_token": "550e8400-e29b-41d4-a716-446655440000",
#     "token_type": "Bearer",
#     "expires_in": 604800,
#     "user": { ... }
#   }
# }

# ============================= CHANGE PASSWORD =============================
# Update user password

curl -X PUT http://localhost:3333/api/v1/auth/change-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiJ9..." \
  -d '{
    "old_password": "SecurePassword123!",
    "new_password": "NewSecurePassword456!",
    "confirm_password": "NewSecurePassword456!"
  }'

# Expected Response (200 OK):
# {
#   "status": 200,
#   "message": "Ok",
#   "data": "Password changed successfully"
# }

# ============================= ERROR CASES =============================

# 1. Invalid Sign Up - Passwords don't match
curl -X POST http://localhost:3333/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "janedoe",
    "email": "jane@example.com",
    "password": "SecurePassword123!",
    "confirmPassword": "DifferentPassword456!",
    "fullName": "Jane Doe"
  }'
# Response (400 Bad Request):
# {
#   "status": 400,
#   "message": "Bad Request",
#   "data": null
# }

# 2. Invalid Sign In - User not found
curl -X POST http://localhost:3333/api/v1/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "nonexistent",
    "password": "SomePassword123!"
  }'
# Response (401 Unauthorized): UserNotFoundException

# 3. Invalid Sign In - Wrong password
curl -X POST http://localhost:3333/api/v1/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "WrongPassword123!"
  }'
# Response (401 Unauthorized): PasswordNotMatchException

# 4. Invalid Refresh Token
curl -X POST http://localhost:3333/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refresh_token": "invalid-token-uuid"
  }'
# Response (404 Not Found): RefreshTokenNotFoundException

# 5. Expired Refresh Token - automatically rejected

# 6. Change Password - Wrong old password
curl -X PUT http://localhost:3333/api/v1/auth/change-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiJ9..." \
  -d '{
    "old_password": "WrongOldPassword!",
    "new_password": "NewPassword456!",
    "confirm_password": "NewPassword456!"
  }'
# Response: PasswordNotMatchException

# ============================= USING ACCESS TOKEN =============================
# Include in protected endpoints:

curl -H "Authorization: Bearer eyJhbGciOiJSUzI1NiJ9..." \
  http://localhost:3333/api/v1/protected-resource

# Token format: "Bearer <JWT_TOKEN>"
# Token validity: 7 days from issuedAt
# Signature: RSA-256 with application's private key

# ============================= SCRIPTING EXAMPLES =============================

# Store tokens in variables (bash)
RESPONSE=$(curl -s -X POST http://localhost:3333/api/v1/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","password":"SecurePassword123!"}')

ACCESS_TOKEN=$(echo $RESPONSE | jq -r '.data.access_token')
REFRESH_TOKEN=$(echo $RESPONSE | jq -r '.data.refresh_token')

# Use stored token
curl -H "Authorization: Bearer $ACCESS_TOKEN" \
  http://localhost:3333/api/v1/protected-endpoint

# Refresh when access token expires
NEW_RESPONSE=$(curl -s -X POST http://localhost:3333/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refresh_token\":\"$REFRESH_TOKEN\"}")

NEW_ACCESS_TOKEN=$(echo $NEW_RESPONSE | jq -r '.data.access_token')

# ============================= POSTMAN COLLECTION =============================
# Import this environment and collection into Postman:

# Environment Variables:
# - base_url: http://localhost:3333/api/v1/auth
# - access_token: (auto-populated from responses)
# - refresh_token: (auto-populated from responses)

# In Postman Tests tab, add:
# if (pm.response.code === 200 || pm.response.code === 201) {
#   var jsonData = pm.response.json();
#   pm.environment.set("access_token", jsonData.data.access_token);
#   pm.environment.set("refresh_token", jsonData.data.refresh_token);
# }

# Then use {{access_token}} and {{refresh_token}} in subsequent requests

# ============================= NOTES =============================
# 1. All passwords must match (password = confirmPassword)
# 2. Username and email must be unique per tenant
# 3. Access tokens expire after 7 days (604800 seconds)
# 4. Refresh tokens expire after 30 days
# 5. Tokens are signed with RSA-2048 algorithm
# 6. Token hashes are Base64-encoded in database
# 7. Refresh tokens can be revoked by admin
# 8. Multiple sessions allowed per user (concurrent logins)
# 9. Each session tracks IP address and User-Agent
# 10. Password hashing uses BCrypt with work factor 10

