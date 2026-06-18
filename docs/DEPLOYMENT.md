# NexaHR Deployment

## Env vars (prod)

`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ORIGIN`

Profile: `SPRING_PROFILES_ACTIVE=mysql,prod`

## Docker

```bash
docker compose up --build -d
```

## Manual

Backend: `mvn package` → `java -jar target/*.jar`  
Frontend: `VITE_API_URL=/api npm run build` → deploy `dist/`
