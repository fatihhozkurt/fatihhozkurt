# Production Deployment Guide

## 1) Prepare environment variables

1. Copy `.env.production.example` to `.env.production`.
2. Replace all `CHANGE_ME...` values with strong secrets.
3. Keep `SPRING_PROFILES_ACTIVE=product`.
4. Use your real domain in `APP_CORS_ALLOWED_ORIGINS`.

## 2) Build and run

```bash
BACKEND_ENV_FILE=.env.production docker compose -f docker-compose.production.yml --env-file .env.production up -d --build
```

## 3) Verify health

```bash
curl -f http://localhost/health
curl -f http://localhost/actuator/health
curl -f http://localhost/api/v1/public/hero
```

## 4) Operational notes

- Public surface is served by Nginx and proxies `/api/*` to backend.
- Backend runs with `product` profile and strict startup checks:
  - strong JWT secret required
  - secure cookies required
  - localhost CORS origins rejected
- OpenSearch is started with security plugin disabled for local/self-managed stack compatibility.
- Internal services (PostgreSQL/Redis/MinIO/OpenSearch) are not published to host ports in production compose.
- Set up external TLS termination (recommended) and keep `APP_REQUIRE_HTTPS=true`.

## 5) Recommended post-deploy checks

- Login and logout from `/auth`.
- Forgot-password request flow.
- Admin dashboard data fetch.
- Contact form submission on public page.
- Security event logs and delivery logs.
