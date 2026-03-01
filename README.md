# Spider Workspace

T0 skeleton for a full-stack, front-back separated workspace.

## Structure

- `frontend/web-console`: React + Vite + i18n
- `backend`: Maven multi-module (`common`, `api-gateway`)
- `contracts/openapi`: API contracts
- `infra/docker-compose/local.yml`: local infra (`PostgreSQL 15`, `Redis`, `Temporal`)
- `docs`: product and architecture documents

## Quick Start

1. Start infra
   - `pwsh scripts/dev/up-infra.ps1`
2. Start backend
   - `pwsh scripts/dev/start-backend.ps1`
3. Start frontend
   - `pwsh scripts/dev/start-frontend.ps1`

## First API

- `GET /api/v1/health`
- Supports `Accept-Language: zh-CN | en-US`
- `POST /api/v1/workspaces`
- `GET /api/v1/workspaces`
- `PUT /api/v1/workspaces/{workspaceId}`
- `DELETE /api/v1/workspaces/{workspaceId}`
- `GET /api/v1/audit-logs`
