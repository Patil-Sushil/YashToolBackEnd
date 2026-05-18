# Authentication & Authorization Documentation

This document outlines the security architecture and implementation details for the YashTools Backend.

## 1. Overview
The system uses **JWT (JSON Web Token)** for stateless authentication, complemented by a **Refresh Token** mechanism stored in the database for secure session management and long-lived sessions.

## 2. Authentication Flow

### Login (`POST /api/auth/login`)
1. User provides email and password.
2. System authenticates credentials via `AuthenticationManager`.
3. Upon success:
   - A short-lived **Access Token (JWT)** is generated.
   - A long-lived **Refresh Token (UUID)** is generated and stored in the database.
4. Response includes both tokens, user ID, email, and assigned roles.

### Token Refresh (`POST /api/auth/refresh`)
To maintain a session without re-entering credentials:
1. Client sends the `refreshToken`.
2. System validates the token exists in the DB, is not expired, and is not revoked.
3. **Token Rotation**: 
   - The old refresh token is marked as `revoked`.
   - A new access token and a **new** refresh token are generated and returned.
   - This prevents replay attacks if a refresh token is leaked.

### Logout (`POST /api/auth/logout`)
1. Client sends the `refreshToken`.
2. System marks the token as `revoked` in the database, effectively ending that session.

## 3. Security Features

### Password Management
- **BCrypt Hashing**: Passwords are never stored in plain text.
- **Complexity Validation**: Enforced via `PasswordValidator` (8-20 chars, upper/lower/digit/special).
- **Password Change Security**: Changing a password automatically deletes all active refresh tokens for that user, forcing a re-login on all devices.

### Role-Based Access Control (RBAC)
- Roles are persisted in the `role` table.
- Default roles: `ROLE_ADMIN`, `ROLE_SALES`, `ROLE_USER`.
- Endpoints are protected using `@PreAuthorize` annotations (e.g., `@PreAuthorize("hasRole('ADMIN')")`).

### JWT Structure
- **Issuer**: `yashtool-erp`
- **Claims**: `userId`, `roles`, `enabled`, `sub` (email).
- **Signing**: HMAC SHA-256 using a base64 encoded secret key.

## 4. Database Schema

### `users` table
Primary storage for user identity and status.

### `refresh_tokens` table
- `id`: UUID (Primary Key)
- `token`: Unique string (UUID)
- `user_id`: Reference to user
- `expiry_date`: Timestamp
- `revoked`: Boolean flag for manual logout or rotation
- `created_at`: Audit timestamp

## 5. Error Handling
Common security exceptions handled by `GlobalExceptionHandler`:
- `401 Unauthorized`: Invalid/Expired Access Token or expired Refresh Token.
- `403 Forbidden`: Insufficient role permissions.
- `409 Conflict`: Attempting to use a revoked or invalid token.

## 6. Configuration (application.yaml)
- `jwt.secret`: Secret key for signing JWTs.
- `jwt.expiration`: Access token lifetime (default: 1 hour).
- `jwt.refresh-expiration`: Refresh token lifetime (default: 7 days).
