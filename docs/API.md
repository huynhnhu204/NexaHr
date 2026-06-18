# NexaHR API

Base URL: `http://localhost:8080/api` — Swagger: http://localhost:8080/swagger-ui.html

## Response format

Success: `{ success, message, data, timestamp }`  
Error: `{ success: false, message, errors?, timestamp }`

## Auth

- `POST /auth/login` — email, password, companyId (optional)
- `POST /auth/refresh-token`
- `GET /auth/me`
- Header: `Authorization: Bearer <token>`

## Multi-tenant

- `GET /companies/my`
- `POST /companies/switch` — returns new JWT with companyId

## Key endpoints

| Prefix | Access |
|--------|--------|
| `/employees` | ADMIN, HR, MANAGER |
| `/attendance`, `/leaves` | Role-based |
| `/payrolls` | HR/Admin + `/my` |
| `/public/careers/{code}` | Public |

HTTP: 400 validation, 401 auth, 403 forbidden, 404 not found, 409 duplicate, 500 server.
