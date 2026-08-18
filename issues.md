# Codebase Security and Issues Report

## Security Vulnerabilities

### 1. Hardcoded Secrets and Default Credentials (CWE-798)
**Location:** `src/main/resources/application.yaml`
**Issue:** The configuration file contains hardcoded default secrets and credentials, including:
- **JWT Secret:** `Q0tQeXJmOW9Jb2ZxV2F5cFhUeG5sQzFkQkZpQzZ3c0ZxS0p4cA==`
- **Database Credentials:** `devuser` / `devpassword`
- **Default Admin Password:** `Pass@123`
**Impact:** If this file is committed to version control or accessed by an unauthorized individual, the entire application's authentication and data layer are compromised.
**Remediation:** Remove default secret values from the source code. Use environment variables exclusively without fallback defaults for sensitive information in production configurations.

### 2. Information Exposure Through Error Messages (CWE-209)
**Location:** `src/main/java/com/kalibyte/YashTools/common/exception/GlobalExceptionHandler.java` (Line 72)
**Issue:** The `handleGeneralException(Exception ex)` method returns the raw exception message directly to the client: `ApiResponse.failure("An internal server error occurred: " + ex.getMessage())`.
**Impact:** This can inadvertently expose sensitive internal infrastructure details, SQL syntax errors, or stack trace elements to end-users and attackers.
**Remediation:** Log the full exception on the server side (`log.error(...)`) but return a generic, static error message to the client (e.g., "An unexpected error occurred. Please contact support.").

### 3. Lack of Rate Limiting (CWE-779)
**Location:** Global Architecture / `SecurityConfig.java`
**Issue:** The application currently lacks rate limiting mechanisms. 
**Impact:** The API is highly vulnerable to brute-force attacks against the login endpoint (`/api/auth/login`) and Denial of Service (DoS) attacks on expensive operations or large queries.
**Remediation:** Implement rate limiting (e.g., using Bucket4j, Spring Cloud Gateway RateLimiter, or API Gateway solutions) globally and with stricter limits on authentication endpoints.

### 4. Potential Insecure Direct Object Reference (IDOR) (CWE-639)
**Location:** Controllers (e.g., `CustomerController.java`)
**Issue:** The API relies heavily on Role-Based Access Control (RBAC) via `@PreAuthorize` but does not appear to enforce ownership-based checks on resource access. While resources are identified by UUIDs (which are difficult to enumerate), a user with a specific role (like 'SALES') could theoretically access or modify any other customer's data if they obtain the UUID.
**Impact:** Unauthorized data access or modification within the same privilege level across different users or tenants.
**Remediation:** Ensure that domain-level authorization checks are performed (e.g., verifying that the currently authenticated user owns or is explicitly authorized to interact with the requested resource ID).

## General Observations & Best Practices

- **CSRF Protection:** CSRF is correctly disabled since the application is utilizing stateless JWT authentication. However, it is vital to ensure that front-end clients store these tokens securely (e.g., in memory or sessionStorage) and not in vulnerable auto-sent structures unless properly protected.
- **CORS Configuration:** The `SecurityConfig` permits access from local development URLs (`http://localhost:3000`, `http://localhost:5173`). Ensure that proper environment-based CORS profiles are set up so that production environments only permit intended origins.
- **SQL Injection:** No immediate SQL injection risks were identified as the application correctly utilizes Spring Data JPA repositories which natively prevent standard SQL injections.

## Next Steps
- Implement environment-specific `.yaml` overrides for secrets and credentials.
- Refactor `GlobalExceptionHandler` to sanitize client-facing generic errors.
- Conduct a deeper review of all business logic controllers to enforce strict ownership checks.