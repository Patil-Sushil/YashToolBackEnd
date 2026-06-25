# YashTools Backend

Spring Boot backend for the YashTools ERP domain.

## Production-readiness improvements applied

- Externalized sensitive settings from the default config path.
- Added profile-based configuration for `dev` and `prod`.
- Added Spring Boot Actuator for health and operational monitoring.
- Restricted public actuator access to health/info endpoints.
- Improved security error responses to return proper JSON.
- Improved validation error reporting to include all field errors.
- Added production-safe logging defaults.

## Configuration profiles

### Default config
`src/main/resources/application.yaml` contains shared settings only.

### Development
Use the `dev` profile for local debugging:

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev"
.\mvnw.cmd spring-boot:run
```

### Production
Use the `prod` profile in deployed environments:

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"
.\mvnw.cmd spring-boot:run
```

## Required environment variables

Set these in your deployment environment:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
- `COMPANY_STATE`
- `DEFAULT_ADMIN_PASSWORD` (if bootstrap uses it)

## Recommended next production tasks

1. Replace the release-candidate Spring Boot parent with a stable GA version.
2. Move secrets to a dedicated secret manager or vault.
3. Add login rate limiting and account lockout protection.
4. Add request correlation IDs and structured logging.
5. Add integration tests for auth and role-based access control.
6. Restrict Swagger/OpenAPI exposure in production if not required.

## Health endpoint

Actuator health is available at:

- `/actuator/health`
- `/actuator/info`

Only these actuator endpoints are exposed by default.

