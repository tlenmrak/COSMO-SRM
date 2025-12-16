# cosmo-srm (MVP skeleton)

Tech:
- Java 21
- Spring Boot 3 (WebFlux)
- Postgres (R2DBC)
- Liquibase (migrations via JDBC datasource)
- Keycloak (OAuth2 Resource Server, JWT)

## Run Postgres
```bash
docker compose up -d postgres
```

## Keycloak (optional)
```bash
docker compose up -d keycloak
```
Then create:
- realm: `cosmo`
- roles: `DIRECTOR`, `TECHNOLOGIST`, `PROMOTER`
- a client for your API, and get JWTs for requests.

Issuer URL is in `src/main/resources/application.yml`:
`http://localhost:8081/realms/cosmo`

## Run app
If you have Gradle installed:
```bash
gradle bootRun
```

## Endpoints (MVP)
- POST /api/raw-materials
- GET  /api/raw-materials
- POST /api/recipes
- POST /api/products
- POST /api/batch-templates
- POST /api/batches
- POST /api/batches/{id}/open
- GET  /api/batches/{id}/cost
